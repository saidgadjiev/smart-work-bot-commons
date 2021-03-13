package ru.gadjini.telegram.smart.bot.commons.service.queue.event;

import ru.gadjini.telegram.smart.bot.commons.domain.UploadQueueItem;
import ru.gadjini.telegram.smart.bot.commons.model.SendFileResult;

public class UploadCompleted {

    private SendFileResult sendFileResult;

    private UploadQueueItem uploadQueueItem;

    public UploadCompleted(SendFileResult sendFileResult, UploadQueueItem uploadQueueItem) {
        this.sendFileResult = sendFileResult;
        this.uploadQueueItem = uploadQueueItem;
    }

    public SendFileResult getSendFileResult() {
        return sendFileResult;
    }

    public UploadQueueItem getUploadQueueItem() {
        return uploadQueueItem;
    }
}
