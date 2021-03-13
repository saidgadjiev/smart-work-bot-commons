package ru.gadjini.telegram.smart.bot.commons.service.flood;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.common.TgConstants;
import ru.gadjini.telegram.smart.bot.commons.exception.FloodControlException;
import ru.gadjini.telegram.smart.bot.commons.exception.FloodWaitException;
import ru.gadjini.telegram.smart.bot.commons.property.DownloadFloodControlProperties;
import ru.gadjini.telegram.smart.bot.commons.service.file.FileDownloadService;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class DownloadFloodWaitController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileDownloadService.class);

    private int finishedDownloadingCounter = 0;

    private Set<String> currentDownloads = new LinkedHashSet<>();

    private SleepTime sleep = new SleepTime(LocalDateTime.now(), 0);

    private DownloadFloodControlProperties floodWaitProperties;

    @Autowired
    public DownloadFloodWaitController(DownloadFloodControlProperties floodWaitProperties) {
        this.floodWaitProperties = floodWaitProperties;
    }

    @PostConstruct
    public void init() {
        LOGGER.debug("Download flood wait properties({}, {}, {}, {})", floodWaitProperties.getFileDownloadingConcurrencyLevel(),
                floodWaitProperties.getSleepAfterXDownloads(), floodWaitProperties.getMaxSleepTime(), floodWaitProperties.isEnableLogging());
    }

    public synchronized void startDownloading(String fileId) {
        AtomicLong sleepTime = new AtomicLong(10);
        if (!isThereAnyFreeDownloadingChannel() || isSleeping(sleepTime)) {
            if (floodWaitProperties.isEnableLogging()) {
                LOGGER.debug(Thread.currentThread().getName() + " flood wait " + fileId);
            }
            throw new FloodControlException(sleepTime.get());
        } else {
            acquireDownloadingChannel(fileId);
        }
    }

    public synchronized void cancelDownloading(String fileId, long fileSize) {
        finishDownloading(fileId, fileSize);
    }

    public synchronized void finishDownloading(String fileId, long fileSize) {
        if (currentDownloads.contains(fileId)) {
            try {
                ++finishedDownloadingCounter;
                if (finishedDownloadingCounter % floodWaitProperties.getSleepAfterXDownloads() == 0) {
                    finishedDownloadingCounter = 0;
                    sleep(getSleepTime(fileSize));
                }
            } finally {
                releaseDownloadingChannel(fileId);
            }
        }
    }

    public synchronized void downloadingFloodWait() {
        long sleep = sleep(floodWaitProperties.getSleepOnDownloadingFloodWait());

        throw new FloodWaitException(sleep);
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

    private synchronized void releaseDownloadingChannel(String fileId) {
        currentDownloads.remove(fileId);
    }

    private synchronized void acquireDownloadingChannel(String fileId) {
        if (currentDownloads.size() < floodWaitProperties.getFileDownloadingConcurrencyLevel()) {
            currentDownloads.add(fileId);
        }
    }

    private synchronized boolean isThereAnyFreeDownloadingChannel() {
        return currentDownloads.size() < floodWaitProperties.getFileDownloadingConcurrencyLevel();
    }

    private synchronized boolean isSleeping(AtomicLong sleepTime) {
        long seconds = Duration.between(sleep.getStartedAt(), LocalDateTime.now()).toSeconds();
        long secondsLeft = sleep.getSleepTime() - seconds;

        if (secondsLeft > 0) {
            sleepTime.set(secondsLeft);
        }

        return secondsLeft > 0;
    }

    private long getSleepTime(long fileSize) {
        long sleepOnEverySize = TgConstants.LARGE_FILE_SIZE / floodWaitProperties.getMaxSleepTime();

        return floodWaitProperties.getMinSleepTime() + fileSize / sleepOnEverySize;
    }
}
