package ru.gadjini.telegram.smart.bot.commons.service.concurrent.pool;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.TimeUnit;

public interface ThreadPool {

    int getCorePoolSize();

    <T> Future<T> submit(Callable<T> task);

    void shutdown();

    boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException;

    List<Runnable> shutdownNow();

    void setRejectedExecutionHandler(RejectedExecutionHandler handler);

    int getActiveCount();
}
