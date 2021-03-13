package ru.gadjini.telegram.smart.bot.commons.dao;

import ru.gadjini.telegram.smart.bot.commons.domain.QueueItem;

public interface QueueDaoDelegate<T extends QueueItem> {

    String getQueueName();

    default String getBaseAdditionalClause() {
        return "";
    }
}
