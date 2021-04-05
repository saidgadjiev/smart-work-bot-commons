package ru.gadjini.telegram.smart.bot.commons.service.concurrent.pool;

import java.util.List;
import java.util.concurrent.*;

public class NullThreadPoolExecutor implements ThreadPool {

    private RejectedExecutionHandler rejectedExecutionHandler;

    @Override
    public int getCorePoolSize() {
        return 0;
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        if (rejectedExecutionHandler != null) {
            rejectedExecutionHandler.rejectedExecution(new FutureTask<T>(task), null);
        }

        throw new RejectedExecutionException("Null executor");
    }

    @Override
    public void shutdown() {

    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) {
        return true;
    }

    @Override
    public List<Runnable> shutdownNow() {
        return List.of();
    }

    @Override
    public void setRejectedExecutionHandler(RejectedExecutionHandler handler) {
        this.rejectedExecutionHandler = handler;
    }

    @Override
    public int getActiveCount() {
        return 0;
    }
}
