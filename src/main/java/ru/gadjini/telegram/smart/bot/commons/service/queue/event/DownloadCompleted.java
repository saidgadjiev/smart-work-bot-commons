package ru.gadjini.telegram.smart.bot.commons.service.queue.event;

import ru.gadjini.telegram.smart.bot.commons.domain.DownloadQueueItem;

public class DownloadCompleted {

    private DownloadQueueItem downloadQueueItem;

    public DownloadCompleted(DownloadQueueItem downloadQueueItem) {
        this.downloadQueueItem = downloadQueueItem;
    }

    public DownloadQueueItem getDownloadQueueItem() {
        return downloadQueueItem;
    }
}

