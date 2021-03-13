package ru.gadjini.telegram.smart.bot.commons.service.update;

import ru.gadjini.telegram.smart.bot.commons.domain.QueueItem;

import java.util.Locale;

public interface UpdateQueryStatusCommandMessageProvider {

    String getUpdateStatusMessage(QueueItem queueItem, Locale locale);
}
