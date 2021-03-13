package ru.gadjini.telegram.smart.bot.commons.exception.botapi;

public class TelegramApiRequestException extends TelegramApiException {
    private final String chatId;
    private String apiResponse;
    private Integer errorCode;

    public TelegramApiRequestException(String chatId, String message, Integer errorCode, String response, Throwable ex) {
        super(buildMessage(chatId, message, errorCode, response), ex);
        this.chatId = chatId;
        this.apiResponse = response;
        this.errorCode = errorCode;
    }

    public String getApiResponse() {
        return apiResponse;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public String getChatId() {
        return chatId;
    }

    private static String buildMessage(String chatId, String message, Integer errorCode, String description) {
        StringBuilder msg = new StringBuilder();
        msg.append("(").append(chatId).append(") ").append(message).append("\n").append(errorCode).append(" ")
                .append(description);

        return msg.toString();
    }

    @Override
    public String toString() {
        if (apiResponse == null) {
            return super.toString();
        } else if (errorCode == null) {
            return super.toString() + ": " + apiResponse;
        } else {
            return super.toString() + ": [" + errorCode + "] " + apiResponse;
        }
    }
}
