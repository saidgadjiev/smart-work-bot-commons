package ru.gadjini.telegram.smart.bot.commons.service.queue.event;

import ru.gadjini.telegram.smart.bot.commons.domain.QueueItem;

public class TaskCanceled {

    private QueueItem queueItem;

    public TaskCanceled(QueueItem queueItem) {
        this.queueItem = queueItem;
    }

    public QueueItem getQueueItem() {
        return queueItem;
    }
}
