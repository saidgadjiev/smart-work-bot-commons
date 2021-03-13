package ru.gadjini.telegram.smart.bot.commons.exception;

public class FloodControlException extends RuntimeException {

    private long sleepTime;

    public FloodControlException(long sleepTime) {
        super("Flood control " + sleepTime);
        this.sleepTime = sleepTime;
    }

    public long getSleepTime() {
        return sleepTime;
    }
}
