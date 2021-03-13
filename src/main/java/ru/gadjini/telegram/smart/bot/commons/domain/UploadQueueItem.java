package ru.gadjini.telegram.smart.bot.commons.domain;

import ru.gadjini.telegram.smart.bot.commons.model.Progress;
import ru.gadjini.telegram.smart.bot.commons.model.UploadType;
import ru.gadjini.telegram.smart.bot.commons.service.format.Format;

import java.time.ZonedDateTime;

public class UploadQueueItem extends QueueItem {

    public static final String NAME = "upload_queue";

    public static final String PRODUCER_TABLE = "producer_table";

    public static final String PRODUCER = "producer";

    public static final String PRODUCER_ID = "producer_id";

    public static final String PROGRESS = "progress";

    public static final String METHOD = "method";

    public static final String BODY = "body";

    public static final String EXTRA = "extra";

    public static final String FILE_SIZE = "file_size";

    public static final String UPLOAD_TYPE = "upload_type";

    public static final String FILE_FORMAT = "file_format";

    public static final String NEXT_RUN_AT = "next_run_at";

    public static final String SYNCED = "synced";

    private String producerTable;

    private String producer;

    private Progress progress;

    private int producerId;

    private String method;

    private Object body;

    private Object extra;

    private long fileSize;

    private UploadType uploadType = UploadType.DOCUMENT;

    private Format fileFormat;

    private ZonedDateTime nextRunAt;

    private boolean synced;

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

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public int getProducerId() {
        return producerId;
    }

    public void setProducerId(int producerId) {
        this.producerId = producerId;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public Object getExtra() {
        return extra;
    }

    public void setExtra(Object extra) {
        this.extra = extra;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getProducer() {
        return producer;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    public UploadType getUploadType() {
        return uploadType;
    }

    public void setUploadType(UploadType uploadType) {
        this.uploadType = uploadType;
    }

    public Format getFileFormat() {
        return fileFormat;
    }

    public void setFileFormat(Format fileFormat) {
        this.fileFormat = fileFormat;
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
}
