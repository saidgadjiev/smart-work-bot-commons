package ru.gadjini.telegram.smart.bot.commons.exception;

public class DownloadingException extends RuntimeException {
    public DownloadingException(String message, Throwable cause) {
        super(message, cause);
    }

    public DownloadingException(Throwable cause) {
        super(cause);
    }

    public DownloadingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public DownloadingException() {
    }
}
