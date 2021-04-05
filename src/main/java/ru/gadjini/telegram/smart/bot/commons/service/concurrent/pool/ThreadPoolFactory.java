package ru.gadjini.telegram.smart.bot.commons.service.concurrent.pool;

import org.springframework.stereotype.Component;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Component
public class ThreadPoolFactory {

    public ThreadPool createPool(int threads, Consumer<Runnable> afterExecute) {
        if (threads > 0) {
            return new SmartThreadPoolExecutor(threads, threads, 0, TimeUnit.SECONDS, new SynchronousQueue<>()) {
                @Override
                protected void afterExecute(Runnable r, Throwable t) {
                    super.afterExecute(r, t);
                    afterExecute.accept(r);
                }
            };
        } else {
            return new NullThreadPoolExecutor();
        }
    }
}
