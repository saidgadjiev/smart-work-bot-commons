package ru.gadjini.telegram.smart.bot.commons.domain;

import ru.gadjini.telegram.smart.bot.commons.service.concurrent.SmartExecutorService;

import java.util.List;
import java.util.Map;

public class ThreadsStats {

    private int heavyCorePoolSize;
    private int lightCorePoolSize;
    private int heavyActiveCount;
    private int lightActiveCount;
    private long processingHeavy;
    private long processingLight;
    private long readyToCompleteHeavy;
    private long readToCompleteLight;
    private Map<SmartExecutorService.JobWeight, List<Integer>> processing;

    public ThreadsStats() {}

    public ThreadsStats(int heavyCorePoolSize, int lightCorePoolSize, int heavyActiveCount,
                        int lightActiveCount, long processingHeavy, long processingLight, long readyToCompleteHeavy,
                        long readToCompleteLight, Map<SmartExecutorService.JobWeight, List<Integer>> processing) {
        this.heavyCorePoolSize = heavyCorePoolSize;
        this.lightCorePoolSize = lightCorePoolSize;
        this.heavyActiveCount = heavyActiveCount;
        this.lightActiveCount = lightActiveCount;
        this.processingHeavy = processingHeavy;
        this.processingLight = processingLight;
        this.readyToCompleteHeavy = readyToCompleteHeavy;
        this.readToCompleteLight = readToCompleteLight;
        this.processing = processing;
    }

    public int getHeavyCorePoolSize() {
        return heavyCorePoolSize;
    }

    public int getLightCorePoolSize() {
        return lightCorePoolSize;
    }

    public int getHeavyActiveCount() {
        return heavyActiveCount;
    }

    public int getLightActiveCount() {
        return lightActiveCount;
    }

    public long getProcessingHeavy() {
        return processingHeavy;
    }

    public long getProcessingLight() {
        return processingLight;
    }

    public long getReadyToCompleteHeavy() {
        return readyToCompleteHeavy;
    }

    public long getReadToCompleteLight() {
        return readToCompleteLight;
    }

    public void setHeavyCorePoolSize(int heavyCorePoolSize) {
        this.heavyCorePoolSize = heavyCorePoolSize;
    }

    public void setLightCorePoolSize(int lightCorePoolSize) {
        this.lightCorePoolSize = lightCorePoolSize;
    }

    public void setHeavyActiveCount(int heavyActiveCount) {
        this.heavyActiveCount = heavyActiveCount;
    }

    public void setLightActiveCount(int lightActiveCount) {
        this.lightActiveCount = lightActiveCount;
    }

    public void setProcessingHeavy(long processingHeavy) {
        this.processingHeavy = processingHeavy;
    }

    public void setProcessingLight(long processingLight) {
        this.processingLight = processingLight;
    }

    public void setReadyToCompleteHeavy(long readyToCompleteHeavy) {
        this.readyToCompleteHeavy = readyToCompleteHeavy;
    }

    public void setReadToCompleteLight(long readToCompleteLight) {
        this.readToCompleteLight = readToCompleteLight;
    }

    public Map<SmartExecutorService.JobWeight, List<Integer>> getProcessing() {
        return processing;
    }

    public void setProcessing(Map<SmartExecutorService.JobWeight, List<Integer>> processing) {
        this.processing = processing;
    }
}
