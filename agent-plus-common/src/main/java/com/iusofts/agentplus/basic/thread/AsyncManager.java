package com.iusofts.agentplus.basic.thread;

import com.iusofts.agentplus.basic.enums.AsyncTaskGroup;
import com.iusofts.agentplus.basic.enums.ExecutionStrategy;

import java.util.Map;
import java.util.concurrent.*;

/**
 * 异步任务管理器 - 使用JDK21虚拟线程，支持按分组设置最大并发数和执行策略
 *
 * @author
 */
public class AsyncManager {
    /**
     * 操作延迟10毫秒
     */
    private final int OPERATE_DELAY_TIME = 10;

    /**
     * 异步操作任务调度线程池（虚拟线程版本）
     */
    private final ScheduledExecutorService virtualExecutor;

    /**
     * 不同任务类型的并发控制映射表
     */
    private final Map<String, Semaphore> concurrencyControls;

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
        // 创建使用虚拟线程的调度执行器
        ThreadFactory virtualThreadFactory = Thread.ofVirtual()
            .name("virtual-async-task-")
            .factory();
        this.virtualExecutor = Executors.newScheduledThreadPool(0, virtualThreadFactory);

        // 初始化并发控制映射表
        this.concurrencyControls = new ConcurrentHashMap<>();
        this.executionStrategies = new ConcurrentHashMap<>();
        this.timeoutSeconds = new ConcurrentHashMap<>();

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
     * 为指定任务类型设置最大并发数限制
     *
     * @param taskType 任务类型标识
     * @param limit 并发数限制
     */
    public void setConcurrentLimit(String taskType, int limit) {
        concurrencyControls.put(taskType, new Semaphore(limit));
    }

    /**
     * 为指定任务枚举设置最大并发数限制
     *
     * @param taskGroup 任务分组枚举
     * @param limit 并发数限制
     */
    public void setConcurrentLimit(AsyncTaskGroup taskGroup, int limit) {
        concurrencyControls.put(taskGroup.getGroupName(), new Semaphore(limit));
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
     * 获取当前任务类型的信号量
     *
     * @param taskType 任务类型
     * @return 信号量，如果未定义则返回默认限制的信号量
     */
    private Semaphore getSemaphore(String taskType) {
        return concurrencyControls.getOrDefault(taskType,
            concurrencyControls.computeIfAbsent("DEFAULT", k -> new Semaphore(DEFAULT_CONCURRENT_LIMIT)));
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
     */
    public void executeVirtualTask(AsyncTaskGroup taskGroup, Runnable task) {
        executeVirtualTask(taskGroup.getGroupName(), task,
                         getExecutionStrategy(taskGroup.getGroupName()),
                         getTimeoutSeconds(taskGroup.getGroupName()));
    }

    /**
     * 使用虚拟线程执行任务（带分组字符串并发控制和策略）
     *
     * @param taskType 任务类型标识
     * @param task 任务
     */
    public void executeVirtualTask(String taskType, Runnable task) {
        executeVirtualTask(taskType, task, getExecutionStrategy(taskType), getTimeoutSeconds(taskType));
    }

    /**
     * 使用虚拟线程执行任务（带分组枚举、执行策略和超时时间）
     *
     * @param taskGroup 任务分组枚举
     * @param task 任务
     * @param strategy 执行策略
     * @param timeoutSeconds 超时时间（秒）
     */
    public void executeVirtualTask(AsyncTaskGroup taskGroup, Runnable task, ExecutionStrategy strategy, int timeoutSeconds) {
        executeVirtualTask(taskGroup.getGroupName(), task, strategy, timeoutSeconds);
    }

    /**
     * 使用虚拟线程执行任务（带分组字符串、执行策略和超时时间）
     *
     * @param taskType 任务类型标识
     * @param task 任务
     * @param strategy 执行策略
     * @param timeoutSeconds 超时时间（秒）
     */
    public void executeVirtualTask(String taskType, Runnable task, ExecutionStrategy strategy, int timeoutSeconds) {
        Semaphore semaphore = getSemaphore(taskType);

        Thread.startVirtualThread(() -> {
            try {
                if (strategy == ExecutionStrategy.BLOCKING) {
                    // 阻塞模式：等待获取许可（无限等待）
                    semaphore.acquire();
                    try {
                        task.run();
                    } finally {
                        semaphore.release();
                    }
                } else if (strategy == ExecutionStrategy.TRY_ACQUIRE_TIMEOUT) {
                    // 超时获取模式：在指定时间内尝试获取许可
                    if (semaphore.tryAcquire(timeoutSeconds, TimeUnit.SECONDS)) {
                        try {
                            task.run();
                        } finally {
                            semaphore.release();
                        }
                    } else {
                        // 如果获取许可超时，跳过任务并记录日志
                        System.err.println("Task " + taskType + " execution skipped due to concurrency limit exceeded after " + timeoutSeconds + " seconds.");
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Task " + taskType + " execution interrupted: " + e.getMessage());
            }
        });
    }

    /**
     * 使用虚拟线程执行任务（无分组限制，保持向后兼容）
     *
     * @param task 任务
     */
    public void executeVirtualTask(Runnable task) {
        // 在虚拟线程中直接运行任务
        Thread.startVirtualThread(task);
    }

    /**
     * 使用虚拟线程执行任务
     *
     * @param task 任务
     */
    public void executeVirtual(java.util.TimerTask task) {
        virtualExecutor.schedule(task, OPERATE_DELAY_TIME, TimeUnit.MILLISECONDS);
    }

    /**
     * 执行延迟的虚拟线程任务 (按分组字符串限制和策略)
     *
     * @param taskType 任务类型
     * @param task 任务
     * @param delay 延迟时间
     * @param unit 时间单位
     */
    public ScheduledFuture<?> scheduleVirtualTask(String taskType, Runnable task, long delay, TimeUnit unit) {
        return virtualExecutor.schedule(() -> executeVirtualTask(taskType, task), delay, unit);
    }

    /**
     * 执行延迟的虚拟线程任务 (按分组字符串、执行策略和超时时间限制)
     *
     * @param taskType 任务类型
     * @param task 任务
     * @param delay 延迟时间
     * @param unit 时间单位
     * @param strategy 执行策略
     * @param timeoutSeconds 超时时间（秒）
     */
    public ScheduledFuture<?> scheduleVirtualTask(String taskType, Runnable task, long delay, TimeUnit unit,
                                               ExecutionStrategy strategy, int timeoutSeconds) {
        return virtualExecutor.schedule(() -> executeVirtualTask(taskType, task, strategy, timeoutSeconds), delay, unit);
    }

    /**
     * 执行延迟的虚拟线程任务 (新增方法)
     *
     * @param task 任务
     * @param delay 延迟时间
     * @param unit 时间单位
     */
    public ScheduledFuture<?> scheduleVirtualTask(Runnable task, long delay, TimeUnit unit) {
        return virtualExecutor.schedule(() -> Thread.startVirtualThread(task), delay, unit);
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
     * 停止任务线程池
     */
    public void shutdown() {
        Threads.shutdownAndAwaitTermination(virtualExecutor);
    }
}