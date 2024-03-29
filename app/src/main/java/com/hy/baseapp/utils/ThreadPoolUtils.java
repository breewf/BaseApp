package com.hy.baseapp.utils;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import androidx.annotation.NonNull;

/**
 * Created by GHY on 2017/3/22.
 * Desc: 线程池工具类
 */
public class ThreadPoolUtils {

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    /**
     * 线程池核心线程数
     */
    private static final int CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4));
    /**
     * 线程池最大线程数
     */
    private static final int MAX_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static int KEEP_ALIVE_TIME = 30;
    private static BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(10);
    private static ThreadPoolExecutor threadPool;

    private static ThreadFactory threadFactory = new ThreadFactory() {
        private final AtomicInteger integer = new AtomicInteger();

        @Override
        public Thread newThread(@NonNull Runnable runnable) {
            return new Thread(runnable, "myThreadPoolThread:" + integer.getAndIncrement());
        }
    };

    static {
        threadPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME,
                TimeUnit.SECONDS, workQueue, threadFactory);
    }

    /**
     * 从线程池中抽取线程，执行指定的Runnable对象
     *
     * @param runnable
     */
    public static void execute(Runnable runnable) {
        threadPool.execute(runnable);
    }

    /**
     * 关闭线程
     */
    public static void cancel() {
        if (!threadPool.isShutdown()) {
            threadPool.shutdownNow();
        }
    }

}

