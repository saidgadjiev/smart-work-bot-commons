package ru.gadjini.telegram.smart.bot.commons.service.localisation;

public class ErrorCode {

    public static final ErrorCode EMPTY = new ErrorCode();

    private String code;

    private Object[] args;

    public ErrorCode(String code, Object[] args) {
        this.code = code;
        this.args = args;
    }

    public ErrorCode(String code) {
        this.code = code;
    }

    private ErrorCode() {}

    public String getCode() {
        return code;
    }

    public Object[] getArgs() {
        return args;
    }
}
