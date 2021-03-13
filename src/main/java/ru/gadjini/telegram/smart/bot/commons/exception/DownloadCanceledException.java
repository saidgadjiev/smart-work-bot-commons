package ru.gadjini.telegram.smart.bot.commons.exception;

public class DownloadCanceledException extends RuntimeException {

    public DownloadCanceledException(String message) {
        super(message);
    }
}
