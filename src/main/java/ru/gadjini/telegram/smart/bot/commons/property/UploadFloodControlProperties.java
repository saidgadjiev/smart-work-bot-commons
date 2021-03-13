package ru.gadjini.telegram.smart.bot.commons.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("upload.flood.control")
public class UploadFloodControlProperties {

    private int sleepAfterXUploads = 1;

    //In seconds
    private int sleepTime = 4;

    private boolean enableLogging = false;

    public int getSleepTime() {
        return sleepTime;
    }

    public void setSleepTime(int sleepTime) {
        this.sleepTime = sleepTime;
    }

    public int getSleepAfterXUploads() {
        return sleepAfterXUploads;
    }

    public void setSleepAfterXUploads(int sleepAfterXUploads) {
        this.sleepAfterXUploads = sleepAfterXUploads;
    }

    public boolean isEnableLogging() {
        return enableLogging;
    }

    public void setEnableLogging(boolean enableLogging) {
        this.enableLogging = enableLogging;
    }
}
