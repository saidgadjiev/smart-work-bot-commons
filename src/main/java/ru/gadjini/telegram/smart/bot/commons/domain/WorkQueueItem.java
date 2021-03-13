package ru.gadjini.telegram.smart.bot.commons.domain;

public abstract class WorkQueueItem extends QueueItem {

    public abstract long getSize();
}
