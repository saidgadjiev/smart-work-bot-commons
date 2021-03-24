package ru.gadjini.telegram.smart.bot.commons.domain;

import ru.gadjini.telegram.smart.bot.commons.model.Progress;

import java.time.ZonedDateTime;

public class DownloadQueueItem extends QueueItem {

    public static final String NAME = "downloading_queue";

    public static final String FILE = "file";

    public static final String PRODUCER_TABLE = "producer_table";

    public static final String PRODUCER = "producer";

    public static final String PRODUCER_ID = "producer_id";

    public static final String PROGRESS = "progress";

    public static final String FILE_PATH = "file_path";

    public static final String DELETE_PARENT_DIR = "delete_parent_dir";

    public static final String EXTRA = "extra";

    public static final String NEXT_RUN_AT = "next_run_at";

    public static final String SYNCED = "synced_";

    private TgFile file;

    private String producerTable;

    private String producer;

    private Progress progress;

    private int producerId;

    private String filePath;

    private boolean deleteParentDir;

    private Object extra;

    private ZonedDateTime nextRunAt;

    private boolean synced;

    public TgFile getFile() {
        return file;
    }

    public void setFile(TgFile file) {
        this.file = file;
    }

    public String getProducerTable() {
        return producerTable;
    }

    public void setProducerTable(String producerTable) {
        this.producerTable = producerTable;
    }

    public Progress getProgress() {
        return progress;
    }

    public void setProgress(Progress progress) {
        this.progress = progress;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public boolean isDeleteParentDir() {
        return deleteParentDir;
    }

    public void setDeleteParentDir(boolean deleteParentDir) {
        this.deleteParentDir = deleteParentDir;
    }

    public int getProducerId() {
        return producerId;
    }

    public void setProducerId(int producerId) {
        this.producerId = producerId;
    }

    public Object getExtra() {
        return extra;
    }

    public void setExtra(Object extra) {
        this.extra = extra;
    }

    public String getProducer() {
        return producer;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    public ZonedDateTime getNextRunAt() {
        return nextRunAt;
    }

    public void setNextRunAt(ZonedDateTime nextRunAt) {
        this.nextRunAt = nextRunAt;
    }

    public boolean isSynced() {
        return synced;
    }

    public void setSynced(boolean synced) {
        this.synced = synced;
    }

    public static String getSynchronizationColumn(int serverNumber) {
        return SYNCED + serverNumber;
    }
}
