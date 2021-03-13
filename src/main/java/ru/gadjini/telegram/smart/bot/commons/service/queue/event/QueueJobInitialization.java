package ru.gadjini.telegram.smart.bot.commons.service.queue.event;

import org.springframework.context.ApplicationEvent;

public class QueueJobInitialization extends ApplicationEvent {
    /**
     * Create a new {@code ApplicationEvent}.
     *
     * @param source the object on which the event initially occurred or with
     *               which the event is associated (never {@code null})
     */
    public QueueJobInitialization(Object source) {
        super(source);
    }
}
