package ru.gadjini.telegram.smart.bot.commons.exception;

public class FloodWaitException extends RuntimeException {

    private long sleepTime;

    public FloodWaitException(long sleepTime) {
        this("Flood wait", sleepTime);
    }

    public FloodWaitException(String message, long sleepTime) {
        super(message + " " + sleepTime);
        this.sleepTime = sleepTime;
    }

    public FloodWaitException(String message, Throwable cause) {
        super(message, cause);
    }

    public FloodWaitException(Throwable cause) {
        super(cause);
    }

    public FloodWaitException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public long getSleepTime() {
        return sleepTime;
    }
}
