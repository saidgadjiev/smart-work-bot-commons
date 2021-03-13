package ru.gadjini.telegram.smart.bot.commons.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TgMethodExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(TgMethodExecutor.class);

    //private BlockingQueue<Runnable> jobsQueue = new LinkedBlockingQueue<>();

    public TgMethodExecutor() {
        LOGGER.debug("Tg method executor initialized");
    }

    /*@Scheduled(fixedDelay = 40)
    public void send() {
        try {
            Runnable job = jobsQueue.take();

            job.run();
        } catch (InterruptedException e) {
            LOGGER.error("Method executor interrupted");
        }
    }*/

    public void push(Runnable job) {
        job.run();
    }
}
