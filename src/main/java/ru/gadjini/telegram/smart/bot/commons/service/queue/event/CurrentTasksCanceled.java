package ru.gadjini.telegram.smart.bot.commons.service.queue.event;

public class CurrentTasksCanceled {

    private int userId;

    public CurrentTasksCanceled(int userId) {
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }
}
