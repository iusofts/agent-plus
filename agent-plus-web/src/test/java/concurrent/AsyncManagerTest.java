package concurrent;

import com.iusofts.agentplus.basic.enums.AsyncTaskGroup;
import com.iusofts.agentplus.basic.enums.ExecutionStrategy;
import com.iusofts.agentplus.basic.thread.AsyncManager;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * AsyncManager并发控制测试
 */
public class AsyncManagerTest {

    @Test
    public void testConcurrentControl() throws InterruptedException {
        AsyncManager manager = AsyncManager.me();

        // 设置操作日志任务的最大并发数为5
        manager.setConcurrentLimit(AsyncTaskGroup.OPERATION_LOG, 5);

        int totalTasks = 20;
        CountDownLatch latch = new CountDownLatch(totalTasks);

        long startTime = System.currentTimeMillis();

        // 提交20个任务，但只有5个会同时执行
        for (int i = 0; i < totalTasks; i++) {
            final int taskId = i;
            Thread.sleep(10); // 确保任务顺序提交

            Thread thread = new Thread(() -> {
                manager.executeVirtualTask(AsyncTaskGroup.OPERATION_LOG, () -> {
                    try {
                        System.out.println("Task " + taskId + " is running, available permits: " +
                            manager.getAvailablePermits(AsyncTaskGroup.OPERATION_LOG));

                        // 模拟耗时操作
                        Thread.sleep(1000);

                        System.out.println("Task " + taskId + " completed");
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        latch.countDown();
                    }
                });
            });

            thread.start();
        }

        // 等待所有任务完成
        latch.await(60, TimeUnit.SECONDS);
        long endTime = System.currentTimeMillis();

        System.out.println("All tasks completed in: " + (endTime - startTime) + " ms");
        System.out.println("Available permits for OPERATION_LOG: " +
            manager.getAvailablePermits(AsyncTaskGroup.OPERATION_LOG));
    }

    @Test
    public void testDifferentTaskTypes() throws InterruptedException {
        AsyncManager manager = AsyncManager.me();

        // 设置不同类型任务的并发限制
        manager.setConcurrentLimit(AsyncTaskGroup.DB_WRITE, 3);
        manager.setConcurrentLimit(AsyncTaskGroup.FILE_UPLOAD, 2);

        int dbTasks = 10;
        int fileTasks = 8;
        CountDownLatch latch = new CountDownLatch(dbTasks + fileTasks);

        long startTime = System.currentTimeMillis();

        // 提交数据库写入任务
        for (int i = 0; i < dbTasks; i++) {
            final int taskId = i;
            new Thread(() -> {
                manager.executeVirtualTask(AsyncTaskGroup.DB_WRITE, () -> {
                    try {
                        System.out.println("DB Task " + taskId + " running, DB permits: " +
                            manager.getAvailablePermits(AsyncTaskGroup.DB_WRITE));
                        Thread.sleep(1000);
                        System.out.println("DB Task " + taskId + " completed");
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        latch.countDown();
                    }
                });
            }).start();
        }

        // 提交文件上传任务
        for (int i = 0; i < fileTasks; i++) {
            final int taskId = i;
            new Thread(() -> {
                manager.executeVirtualTask(AsyncTaskGroup.FILE_UPLOAD, () -> {
                    try {
                        System.out.println("File Task " + taskId + " running, File permits: " +
                            manager.getAvailablePermits(AsyncTaskGroup.FILE_UPLOAD));
                        Thread.sleep(1500);
                        System.out.println("File Task " + taskId + " completed");
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        latch.countDown();
                    }
                });
            }).start();
        }

        latch.await(60, TimeUnit.SECONDS);
        long endTime = System.currentTimeMillis();

        System.out.println("All mixed tasks completed in: " + (endTime - startTime) + " ms");
        System.out.println("Remaining DB permits: " + manager.getAvailablePermits(AsyncTaskGroup.DB_WRITE));
        System.out.println("Remaining File permits: " + manager.getAvailablePermits(AsyncTaskGroup.FILE_UPLOAD));
    }

    @Test
    public void testCustomTaskGroup() throws InterruptedException {
        AsyncManager manager = AsyncManager.me();

        // 测试自定义任务组
        manager.setConcurrentLimit("CUSTOM_TASK_GROUP", 3);

        int totalTasks = 12;
        CountDownLatch latch = new CountDownLatch(totalTasks);

        long startTime = System.currentTimeMillis();

        // 提交12个任务，但只有3个会同时执行
        for (int i = 0; i < totalTasks; i++) {
            final int taskId = i;
            Thread.sleep(5); // 确保任务顺序提交

            Thread thread = new Thread(() -> {
                manager.executeVirtualTask("CUSTOM_TASK_GROUP", () -> {
                    try {
                        System.out.println("Custom Task " + taskId + " is running, available permits: " +
                            manager.getAvailablePermits("CUSTOM_TASK_GROUP"));

                        // 模拟耗时操作
                        Thread.sleep(800);

                        System.out.println("Custom Task " + taskId + " completed");
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        latch.countDown();
                    }
                });
            });

            thread.start();
        }

        // 等待所有任务完成
        latch.await(60, TimeUnit.SECONDS);
        long endTime = System.currentTimeMillis();

        System.out.println("All custom tasks completed in: " + (endTime - startTime) + " ms");
        System.out.println("Available permits for CUSTOM_TASK_GROUP: " +
            manager.getAvailablePermits("CUSTOM_TASK_GROUP"));
    }

    @Test
    public void testExecutionStrategies() throws InterruptedException {
        AsyncManager manager = AsyncManager.me();

        int totalTasks = 10;
        CountDownLatch latch = new CountDownLatch(totalTasks);

        // 使用TRY_ACQUIRE_TIMEOUT策略执行任务（超时跳过）
        System.out.println("Testing TRY_ACQUIRE_TIMEOUT strategy...");

        // 设置低并发限制和短超时时间来测试跳过机制
        manager.setConcurrentLimit("TEST_TIMEOUT", 1);
        manager.setExecutionStrategy("TEST_TIMEOUT", ExecutionStrategy.TRY_ACQUIRE_TIMEOUT);
        manager.setTimeoutSeconds("TEST_TIMEOUT", 2); // 2秒超时

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < totalTasks; i++) {
            final int taskId = i;
            Thread.sleep(50); // 稍微间隔提交任务

            Thread thread = new Thread(() -> {
                manager.executeVirtualTask("TEST_TIMEOUT", () -> {
                    try {
                        System.out.println("Test Timeout Task " + taskId + " is running...");

                        // 模拟较长时间的操作，比超时时间长
                        Thread.sleep(3000);

                        System.out.println("Test Timeout Task " + taskId + " completed");
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        latch.countDown();
                    }
                });
            });

            thread.start();
        }

        // 等待所有任务（或超时）完成
        latch.await(40, TimeUnit.SECONDS);
        long endTime = System.currentTimeMillis();

        System.out.println("Execution strategy test completed in: " + (endTime - startTime) + " ms");
    }

    @Test
    public void testBlockingStrategy() throws InterruptedException {
        AsyncManager manager = AsyncManager.me();

        int totalTasks = 8;
        CountDownLatch latch = new CountDownLatch(totalTasks);

        System.out.println("Testing BLOCKING strategy...");

        // 设置较低的并发限制来观察阻塞行为
        manager.setConcurrentLimit("TEST_BLOCKING", 2);
        manager.setExecutionStrategy("TEST_BLOCKING", ExecutionStrategy.BLOCKING);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < totalTasks; i++) {
            final int taskId = i;
            Thread.sleep(50); // 稍微间隔提交任务

            Thread thread = new Thread(() -> {
                manager.executeVirtualTask("TEST_BLOCKING", () -> {
                    try {
                        System.out.println("Test Blocking Task " + taskId + " is running...");

                        // 模拟中等长度的操作
                        Thread.sleep(1000);

                        System.out.println("Test Blocking Task " + taskId + " completed");
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        latch.countDown();
                    }
                }, ExecutionStrategy.BLOCKING, 10); // 使用BLOCKING策略
            });

            thread.start();
        }

        // 等待所有任务完成
        latch.await(60, TimeUnit.SECONDS);
        long endTime = System.currentTimeMillis();

        System.out.println("Blocking strategy test completed in: " + (endTime - startTime) + " ms");
    }
}