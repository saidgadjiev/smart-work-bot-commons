package ru.gadjini.telegram.smart.bot.commons.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("file.manager")
public class FileManagerProperties {

    private long sleepTimeBeforeDownloadAttempt = 60;

    private long sleepTimeBeforeUploadAttempt = 60;

    private long floodWaitPenalty = 20;

    public long getSleepTimeBeforeDownloadAttempt() {
        return sleepTimeBeforeDownloadAttempt;
    }

    public void setSleepTimeBeforeDownloadAttempt(long sleepTimeBeforeDownloadAttempt) {
        this.sleepTimeBeforeDownloadAttempt = sleepTimeBeforeDownloadAttempt;
    }

    public long getSleepTimeBeforeUploadAttempt() {
        return sleepTimeBeforeUploadAttempt;
    }

    public void setSleepTimeBeforeUploadAttempt(long sleepTimeBeforeUploadAttempt) {
        this.sleepTimeBeforeUploadAttempt = sleepTimeBeforeUploadAttempt;
    }

    public long getFloodWaitPenalty() {
        return floodWaitPenalty;
    }

    public void setFloodWaitPenalty(long floodWaitPenalty) {
        this.floodWaitPenalty = floodWaitPenalty;
    }
}
