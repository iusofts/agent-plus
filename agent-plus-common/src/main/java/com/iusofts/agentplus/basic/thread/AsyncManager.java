package com.iusofts.agentplus.basic.thread;

import com.iusofts.agentplus.basic.enums.AsyncTaskGroup;
import com.iusofts.agentplus.basic.enums.ExecutionStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 异步任务管理器 - 使用 JDK21 虚拟线程，支持按分组设置最大并发数、在途上限和执行策略。
 *
 * <h3>并发模型（两级限流）</h3>
 * <ol>
 *   <li><b>准入闸门(admissionGate)</b>：在<em>提交端非阻塞</em> {@code tryAcquire}，限制单个分组
 *       「等待中+执行中」的在途任务总数。闸门满则立即拒绝(记日志、返回 false)，避免瞬时洪峰创建
 *       无上限的虚拟线程而 OOM。这是真正的准入/背压控制。</li>
 *   <li><b>并发信号量(concurrencyControls)</b>：在虚拟线程内按策略获取，限制<em>同时执行</em>的任务数。
 *       未抢到的任务在虚拟线程上廉价 park(数量已被准入闸门约束)。</li>
 * </ol>
 *
 * <p>提交动作本身不阻塞调用方(如 Web 请求线程),保持 fire-and-forget 语义;拒绝与执行异常均通过
 * SLF4J 记录,不再静默丢弃。信号量使用公平模式,避免高压下饥饿。</p>
 *
 * @author
 */
public class AsyncManager {

    private static final Logger log = LoggerFactory.getLogger(AsyncManager.class);

    /**
     * 操作延迟10毫秒
     */
    private final int OPERATE_DELAY_TIME = 10;

    /**
     * 定时触发调度器(平台守护线程,仅负责计时);实际任务派发到虚拟线程执行。
     */
    private final ScheduledExecutorService scheduler;

    /**
     * 不同任务类型的并发控制映射表(限制同时执行数)
     */
    private final Map<String, Semaphore> concurrencyControls;

    /**
     * 不同任务类型的准入闸门映射表(限制在途任务总数 = 等待中 + 执行中)
     */
    private final Map<String, Semaphore> admissionGates;

    /**
     * 不同任务类型的执行策略映射表
     */
    private final Map<String, ExecutionStrategy> executionStrategies;

    /**
     * 不同任务类型的超时时间映射表（秒）
     */
    private final Map<String, Integer> timeoutSeconds;

    /**
     * 默认任务类型的并发数限制
     */
    private static final int DEFAULT_CONCURRENT_LIMIT = 50;

    /**
     * 默认在途任务上限(准入闸门大小)。虚拟线程 park 成本低,故给出较大默认值以容纳突发,
     * 同时对无限堆积形成硬上限,防止 OOM。可按分组覆盖。
     */
    private static final int DEFAULT_MAX_IN_FLIGHT = 10_000;

    /**
     * 默认执行策略
     */
    private static final ExecutionStrategy DEFAULT_EXECUTION_STRATEGY = ExecutionStrategy.TRY_ACQUIRE_TIMEOUT;

    /**
     * 默认超时时间（秒）
     */
    private static final int DEFAULT_TIMEOUT_SECONDS = 5;

    /**
     * 单例模式
     */
    private AsyncManager() {
        // 定时调度用平台守护线程(仅计时),避免用虚拟线程做池化 worker
        ThreadFactory schedulerFactory = r -> {
            Thread t = new Thread(r, "async-scheduler");
            t.setDaemon(true);
            return t;
        };
        this.scheduler = Executors.newScheduledThreadPool(1, schedulerFactory);

        // 初始化映射表
        this.concurrencyControls = new java.util.concurrent.ConcurrentHashMap<>();
        this.admissionGates = new java.util.concurrent.ConcurrentHashMap<>();
        this.executionStrategies = new java.util.concurrent.ConcurrentHashMap<>();
        this.timeoutSeconds = new java.util.concurrent.ConcurrentHashMap<>();

        // 根据枚举初始化默认的并发限制和执行策略
        for (AsyncTaskGroup group : AsyncTaskGroup.values()) {
            if (group != AsyncTaskGroup.CUSTOM) {  // CUSTOM是预留的，不预先设置
                setConcurrentLimit(group.getGroupName(), group.getDefaultLimit());
                setExecutionStrategy(group.getGroupName(), group.getExecutionStrategy());
                setTimeoutSeconds(group.getGroupName(), group.getTimeoutSeconds());
            }
        }
    }

    private static AsyncManager me = new AsyncManager();

    public static AsyncManager me() {
        return me;
    }

    /**
     * 为指定任务类型设置最大并发数限制(同时执行数)。使用公平信号量。
     *
     * @param taskType 任务类型标识
     * @param limit    并发数限制
     */
    public void setConcurrentLimit(String taskType, int limit) {
        concurrencyControls.put(taskType, new Semaphore(limit, true));
    }

    /**
     * 为指定任务枚举设置最大并发数限制
     *
     * @param taskGroup 任务分组枚举
     * @param limit 并发数限制
     */
    public void setConcurrentLimit(AsyncTaskGroup taskGroup, int limit) {
        setConcurrentLimit(taskGroup.getGroupName(), limit);
    }

    /**
     * 为指定任务类型设置在途任务上限(准入闸门大小)。
     *
     * @param taskType    任务类型标识
     * @param maxInFlight 在途任务上限(等待中 + 执行中)
     */
    public void setMaxInFlight(String taskType, int maxInFlight) {
        admissionGates.put(taskType, new Semaphore(maxInFlight, true));
    }

    /**
     * 为指定任务枚举设置在途任务上限
     *
     * @param taskGroup   任务分组枚举
     * @param maxInFlight 在途任务上限
     */
    public void setMaxInFlight(AsyncTaskGroup taskGroup, int maxInFlight) {
        setMaxInFlight(taskGroup.getGroupName(), maxInFlight);
    }

    /**
     * 为指定任务类型设置执行策略
     *
     * @param taskType 任务类型标识
     * @param strategy 执行策略
     */
    public void setExecutionStrategy(String taskType, ExecutionStrategy strategy) {
        executionStrategies.put(taskType, strategy);
    }

    /**
     * 为指定任务枚举设置执行策略
     *
     * @param taskGroup 任务分组枚举
     * @param strategy 执行策略
     */
    public void setExecutionStrategy(AsyncTaskGroup taskGroup, ExecutionStrategy strategy) {
        executionStrategies.put(taskGroup.getGroupName(), strategy);
    }

    /**
     * 为指定任务类型设置超时时间
     *
     * @param taskType 任务类型标识
     * @param timeoutSeconds 超时时间（秒）
     */
    public void setTimeoutSeconds(String taskType, int timeoutSeconds) {
        this.timeoutSeconds.put(taskType, timeoutSeconds);
    }

    /**
     * 为指定任务枚举设置超时时间
     *
     * @param taskGroup 任务分组枚举
     * @param timeoutSeconds 超时时间（秒）
     */
    public void setTimeoutSeconds(AsyncTaskGroup taskGroup, int timeoutSeconds) {
        this.timeoutSeconds.put(taskGroup.getGroupName(), timeoutSeconds);
    }

    /**
     * 获取当前任务类型的并发信号量。未定义则按默认限制惰性创建(每种类型独立,互不影响)。
     */
    private Semaphore getSemaphore(String taskType) {
        return concurrencyControls.computeIfAbsent(taskType,
                k -> new Semaphore(DEFAULT_CONCURRENT_LIMIT, true));
    }

    /**
     * 获取当前任务类型的准入闸门。未定义则按默认在途上限惰性创建。
     */
    private Semaphore getAdmissionGate(String taskType) {
        return admissionGates.computeIfAbsent(taskType,
                k -> new Semaphore(DEFAULT_MAX_IN_FLIGHT, true));
    }

    /**
     * 获取当前任务类型的执行策略
     *
     * @param taskType 任务类型
     * @return 执行策略，如果未定义则返回默认策略
     */
    private ExecutionStrategy getExecutionStrategy(String taskType) {
        return executionStrategies.getOrDefault(taskType, DEFAULT_EXECUTION_STRATEGY);
    }

    /**
     * 获取当前任务类型的超时时间
     *
     * @param taskType 任务类型
     * @return 超时时间（秒），如果未定义则返回默认超时时间
     */
    private int getTimeoutSeconds(String taskType) {
        return timeoutSeconds.getOrDefault(taskType, DEFAULT_TIMEOUT_SECONDS);
    }

    /**
     * 使用虚拟线程执行任务（带分组枚举并发控制和策略）
     *
     * @param taskGroup 任务分组枚举
     * @param task 任务
     * @return 是否被受理(false 表示在途上限已满而被拒绝)
     */
    public boolean executeVirtualTask(AsyncTaskGroup taskGroup, Runnable task) {
        return executeVirtualTask(taskGroup.getGroupName(), task,
                getExecutionStrategy(taskGroup.getGroupName()),
                getTimeoutSeconds(taskGroup.getGroupName()));
    }

    /**
     * 使用虚拟线程执行任务（带分组字符串并发控制和策略）
     *
     * @param taskType 任务类型标识
     * @param task 任务
     * @return 是否被受理
     */
    public boolean executeVirtualTask(String taskType, Runnable task) {
        return executeVirtualTask(taskType, task, getExecutionStrategy(taskType), getTimeoutSeconds(taskType));
    }

    /**
     * 使用虚拟线程执行任务（带分组枚举、执行策略和超时时间）
     *
     * @param taskGroup 任务分组枚举
     * @param task 任务
     * @param strategy 执行策略
     * @param timeoutSeconds 超时时间（秒）
     * @return 是否被受理
     */
    public boolean executeVirtualTask(AsyncTaskGroup taskGroup, Runnable task, ExecutionStrategy strategy, int timeoutSeconds) {
        return executeVirtualTask(taskGroup.getGroupName(), task, strategy, timeoutSeconds);
    }

    /**
     * 使用虚拟线程执行任务（带分组字符串、执行策略和超时时间）。
     *
     * <p>提交端非阻塞:先尝试占用准入名额,占不到即拒绝(返回 false 并记日志),不会创建虚拟线程;
     * 占到名额后交由虚拟线程按策略获取并发许可并执行。</p>
     *
     * @param taskType 任务类型标识
     * @param task 任务
     * @param strategy 执行策略
     * @param timeoutSeconds 超时时间（秒）
     * @return 是否被受理
     */
    public boolean executeVirtualTask(String taskType, Runnable task, ExecutionStrategy strategy, int timeoutSeconds) {
        return dispatch(taskType, task, strategy, timeoutSeconds, null);
    }

    /**
     * 派发核心。准入闸门在提交端把关,并发信号量在虚拟线程内把关。
     *
     * @param completion 可选的完成回调 future:任务成功 complete,失败/拒绝/超时 completeExceptionally;
     *                   fire-and-forget 场景传 null。
     * @return 是否通过准入(被受理)
     */
    private boolean dispatch(String taskType, Runnable task, ExecutionStrategy strategy,
                             int timeoutSeconds, CompletableFuture<Void> completion) {
        if (task == null) {
            throw new IllegalArgumentException("task 不能为空");
        }
        Semaphore gate = getAdmissionGate(taskType);
        // 提交端非阻塞占用在途名额;占不到即拒绝,避免无上限创建虚拟线程
        if (!gate.tryAcquire()) {
            log.warn("异步任务[{}]被拒绝:在途任务已达上限,请调大 maxInFlight 或降低提交速率", taskType);
            if (completion != null) {
                completion.completeExceptionally(
                        new RejectedExecutionException("Task [" + taskType + "] rejected: in-flight limit reached"));
            }
            return false;
        }

        Semaphore concurrency = getSemaphore(taskType);
        Thread.startVirtualThread(() -> {
            boolean concurrencyAcquired = false;
            try {
                if (strategy == ExecutionStrategy.BLOCKING) {
                    concurrency.acquire();
                    concurrencyAcquired = true;
                } else {
                    concurrencyAcquired = concurrency.tryAcquire(timeoutSeconds, TimeUnit.SECONDS);
                }

                if (!concurrencyAcquired) {
                    log.warn("异步任务[{}]跳过:{}s 内未获得并发许可(策略 TRY_ACQUIRE_TIMEOUT)",
                            taskType, timeoutSeconds);
                    if (completion != null) {
                        completion.completeExceptionally(new TimeoutException(
                                "Task [" + taskType + "] skipped: no permit within " + timeoutSeconds + "s"));
                    }
                    return;
                }

                try {
                    task.run();
                    if (completion != null) {
                        completion.complete(null);
                    }
                } catch (Throwable t) {
                    log.error("异步任务[{}]执行异常", taskType, t);
                    if (completion != null) {
                        completion.completeExceptionally(t);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("异步任务[{}]等待并发许可时被中断", taskType, e);
                if (completion != null) {
                    completion.completeExceptionally(e);
                }
            } finally {
                if (concurrencyAcquired) {
                    concurrency.release();
                }
                gate.release();
            }
        });
        return true;
    }

    /**
     * 使用虚拟线程执行任务（无分组限制，保持向后兼容）
     *
     * @param task 任务
     */
    public void executeVirtualTask(Runnable task) {
        // 无分组:直接在虚拟线程运行,并兜底记录异常
        Thread.startVirtualThread(() -> runSafely(task, "no-group"));
    }

    /**
     * 使用虚拟线程执行任务
     *
     * @param task 任务
     */
    public void executeVirtual(java.util.TimerTask task) {
        scheduler.schedule(() -> Thread.startVirtualThread(() -> runSafely(task, "timer")),
                OPERATE_DELAY_TIME, TimeUnit.MILLISECONDS);
    }

    /**
     * 执行延迟的虚拟线程任务 (按分组字符串限制和策略)。
     *
     * @param taskType 任务类型
     * @param task 任务
     * @param delay 延迟时间
     * @param unit 时间单位
     * @return 代表<em>任务本身</em>执行结果的 future:任务完成时 complete,拒绝/超时/异常时 completeExceptionally
     */
    public CompletableFuture<Void> scheduleVirtualTask(String taskType, Runnable task, long delay, TimeUnit unit) {
        return scheduleVirtualTask(taskType, task, delay, unit,
                getExecutionStrategy(taskType), getTimeoutSeconds(taskType));
    }

    /**
     * 执行延迟的虚拟线程任务 (按分组字符串、执行策略和超时时间限制)。
     *
     * @param taskType 任务类型
     * @param task 任务
     * @param delay 延迟时间
     * @param unit 时间单位
     * @param strategy 执行策略
     * @param timeoutSeconds 超时时间（秒）
     * @return 代表任务执行结果的 future
     */
    public CompletableFuture<Void> scheduleVirtualTask(String taskType, Runnable task, long delay, TimeUnit unit,
                                                       ExecutionStrategy strategy, int timeoutSeconds) {
        CompletableFuture<Void> result = new CompletableFuture<>();
        scheduler.schedule(() -> dispatch(taskType, task, strategy, timeoutSeconds, result),
                delay, unit);
        return result;
    }

    /**
     * 执行延迟的虚拟线程任务 (新增方法)
     *
     * @param task 任务
     * @param delay 延迟时间
     * @param unit 时间单位
     * @return 代表任务执行结果的 future
     */
    public CompletableFuture<Void> scheduleVirtualTask(Runnable task, long delay, TimeUnit unit) {
        CompletableFuture<Void> result = new CompletableFuture<>();
        scheduler.schedule(() -> Thread.startVirtualThread(() -> {
            try {
                task.run();
                result.complete(null);
            } catch (Throwable t) {
                log.error("延迟异步任务执行异常", t);
                result.completeExceptionally(t);
            }
        }), delay, unit);
        return result;
    }

    /**
     * 在虚拟线程内安全执行任务,兜底记录异常。
     */
    private void runSafely(Runnable task, String tag) {
        try {
            task.run();
        } catch (Throwable t) {
            log.error("异步任务[{}]执行异常", tag, t);
        }
    }

    /**
     * 获取当前任务类型的可用许可数
     *
     * @param taskType 任务类型
     * @return 可用许可数
     */
    public int getAvailablePermits(String taskType) {
        Semaphore semaphore = concurrencyControls.get(taskType);
        return semaphore != null ? semaphore.availablePermits() : -1;
    }

    /**
     * 获取当前任务分组的可用许可数
     *
     * @param taskGroup 任务分组枚举
     * @return 可用许可数
     */
    public int getAvailablePermits(AsyncTaskGroup taskGroup) {
        return getAvailablePermits(taskGroup.getGroupName());
    }

    /**
     * 获取当前任务类型可用的在途名额(准入闸门剩余许可)。
     *
     * @param taskType 任务类型
     * @return 剩余在途名额,未定义返回 -1
     */
    public int getAvailableInFlight(String taskType) {
        Semaphore gate = admissionGates.get(taskType);
        return gate != null ? gate.availablePermits() : -1;
    }

    /**
     * 获取当前任务类型的执行策略
     *
     * @param taskType 任务类型
     * @return 执行策略
     */
    public ExecutionStrategy getCurrentExecutionStrategy(String taskType) {
        return getExecutionStrategy(taskType);
    }

    /**
     * 获取当前任务分组的执行策略
     *
     * @param taskGroup 任务分组枚举
     * @return 执行策略
     */
    public ExecutionStrategy getCurrentExecutionStrategy(AsyncTaskGroup taskGroup) {
        return getCurrentExecutionStrategy(taskGroup.getGroupName());
    }

    /**
     * 获取当前任务类型的超时时间
     *
     * @param taskType 任务类型
     * @return 超时时间（秒）
     */
    public int getCurrentTimeoutSeconds(String taskType) {
        return getTimeoutSeconds(taskType);
    }

    /**
     * 获取当前任务分组的超时时间
     *
     * @param taskGroup 任务分组枚举
     * @return 超时时间（秒）
     */
    public int getCurrentTimeoutSeconds(AsyncTaskGroup taskGroup) {
        return getCurrentTimeoutSeconds(taskGroup.getGroupName());
    }

    /**
     * 停止调度线程池。在途虚拟线程不属于池化资源,随任务自然结束。
     */
    public void shutdown() {
        Threads.shutdownAndAwaitTermination(scheduler);
    }
}
