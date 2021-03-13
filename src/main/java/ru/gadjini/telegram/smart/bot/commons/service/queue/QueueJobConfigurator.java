package ru.gadjini.telegram.smart.bot.commons.service.queue;

import ru.gadjini.telegram.smart.bot.commons.domain.QueueItem;

public interface QueueJobConfigurator<T extends QueueItem> {

    default boolean isNeedUpdateMessageAfterCancel(T queueItem) {
        return true;
    }
}
