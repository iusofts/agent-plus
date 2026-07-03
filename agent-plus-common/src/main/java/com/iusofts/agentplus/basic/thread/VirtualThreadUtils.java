package com.iusofts.agentplus.basic.thread;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

/**
 * 虚拟线程工具类 - 提供便捷的虚拟线程操作方法
 *
 * @author
 */
public class VirtualThreadUtils {

    /**
     * 获取虚拟线程执行器
     */
    private static final Executor VIRTUAL_THREAD_EXECUTOR =
        CompletableFuture.delayedExecutor(0, java.util.concurrent.TimeUnit.MILLISECONDS);

    /**
     * 在虚拟线程中执行任务
     *
     * @param runnable 要执行的任务
     */
    public static void runInVirtualThread(Runnable runnable) {
        Thread.startVirtualThread(runnable);
    }

    /**
     * 在虚拟线程中执行任务并返回结果
     *
     * @param supplier 提供结果的函数
     * @param <T> 结果类型
     * @return CompletableFuture 包装的结果
     */
    public static <T> CompletableFuture<T> supplyInVirtualThread(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, runnable ->
            Thread.startVirtualThread(runnable));
    }

    /**
     * 在虚拟线程中执行任务
     *
     * @param runnable 要执行的任务
     * @return CompletableFuture 表示任务的完成状态
     */
    public static CompletableFuture<Void> runInVirtualThreadAsync(Runnable runnable) {
        return CompletableFuture.runAsync(runnable, task ->
            Thread.startVirtualThread(task));
    }

    /**
     * 将现有任务包装为虚拟线程任务
     *
     * @param task 要包装的任务
     * @return 包装后的虚拟线程任务
     */
    public static Runnable wrapToVirtualThread(Runnable task) {
        return () -> Thread.startVirtualThread(task);
    }
}