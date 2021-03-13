package ru.gadjini.telegram.smart.bot.commons.exception;

public class ProcessException extends RuntimeException {

    private int exitCode;

    public ProcessException(int exitCode, String message) {
        super(message);
        this.exitCode = exitCode;
    }

    public ProcessException(Throwable cause) {
        super(cause);
    }

    public int getExitCode() {
        return exitCode;
    }
}
