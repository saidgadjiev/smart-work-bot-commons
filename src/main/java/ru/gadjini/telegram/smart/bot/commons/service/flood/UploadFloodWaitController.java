package ru.gadjini.telegram.smart.bot.commons.service.flood;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.exception.FloodControlException;
import ru.gadjini.telegram.smart.bot.commons.property.UploadFloodControlProperties;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class UploadFloodWaitController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadFloodWaitController.class);

    private int finishedUploadingCounter = 0;

    private Set<String> currentUploads = new LinkedHashSet<>();

    private SleepTime sleep = new SleepTime(LocalDateTime.now(), 0);

    private UploadFloodControlProperties floodWaitProperties;

    @Autowired
    public UploadFloodWaitController(UploadFloodControlProperties uploadFloodControlProperties) {
        this.floodWaitProperties = uploadFloodControlProperties;
    }

    @PostConstruct
    public void init() {
        LOGGER.debug("Upload flood wait properties({}, {}, {})", floodWaitProperties.getSleepAfterXUploads(),
                floodWaitProperties.getSleepTime(), floodWaitProperties.isEnableLogging());
    }

    public synchronized void startUploading(String key) {
        AtomicLong sleepTime = new AtomicLong(floodWaitProperties.getSleepTime());
        if (isSleeping(sleepTime)) {
            if (floodWaitProperties.isEnableLogging()) {
                LOGGER.debug(Thread.currentThread().getName() + " flood wait " + key);
            }
            throw new FloodControlException(sleepTime.get());
        } else {
            acquireUploadingChannel(key);
        }
    }

    public synchronized void finishUploading(String key) {
        if (currentUploads.contains(key)) {
            try {
                ++finishedUploadingCounter;
                if (finishedUploadingCounter % floodWaitProperties.getSleepAfterXUploads() == 0) {
                    finishedUploadingCounter = 0;
                    sleep(floodWaitProperties.getSleepTime());
                }
            } finally {
                releaseUploadingChannel(key);
            }
        }
    }

    public synchronized void cancelUploading(String key) {
        finishUploading(key);
    }

    private synchronized void acquireUploadingChannel(String key) {
        currentUploads.add(key);
    }

    private synchronized void releaseUploadingChannel(String key) {
        currentUploads.remove(key);
    }

    private synchronized boolean isSleeping(AtomicLong sleepTime) {
        long seconds = Duration.between(sleep.getStartedAt(), LocalDateTime.now()).toSeconds();
        long secondsLeft = sleep.getSleepTime() - seconds;

        if (secondsLeft > 0) {
            sleepTime.set(secondsLeft);
        }

        return secondsLeft > 0;
    }

    private synchronized long sleep(long sleep) {
        AtomicLong sleeping = new AtomicLong();

        if (isSleeping(sleeping)) {
            sleep = Math.max(sleep, sleeping.get());
            this.sleep = new SleepTime(LocalDateTime.now(), sleep);
        } else {
            this.sleep = new SleepTime(LocalDateTime.now(), sleep);
        }

        return sleep;
    }
}
