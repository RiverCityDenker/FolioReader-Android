package com.sap_press.rheinwerk_reader.download;

import android.os.AsyncTask;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class ParallelExecutorTask<Pa, Pr, Re> extends AsyncTask<Pa, Pr, Re> {

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final int KEEP_ALIVE = 1;
    public static final int LONG_TIME_WAITING_SHUTDOWN = 6;
    public static final int SHORT_TIME_WAITING_SHUTDOWN = 2;
    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "Unbounded Task #" + mCount.getAndIncrement());
        }
    };

    private static final BlockingQueue<Runnable> sPoolWorkQueue = new LinkedBlockingQueue<>();

    private final ThreadPoolExecutor poolExecutor;

    ParallelExecutorTask(ThreadPoolExecutor poolExecutor) {
        this.poolExecutor = poolExecutor;
    }

    ThreadPoolExecutor getPoolExecutor() {
        return poolExecutor;
    }

    public AsyncTask<Pa, Pr, Re> executeParallel(Pa... params) {
        return super.executeOnExecutor(poolExecutor, params);
    }

    public static ThreadPoolExecutor createPool() {
        return new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE,
                TimeUnit.SECONDS, sPoolWorkQueue, sThreadFactory);
    }

    public boolean isStop() {
        return poolExecutor.isShutdown();
    }
}