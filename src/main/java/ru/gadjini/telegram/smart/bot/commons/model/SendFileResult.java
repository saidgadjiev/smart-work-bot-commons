package ru.gadjini.telegram.smart.bot.commons.model;

public class SendFileResult {

    private int messageId;

    private String fileId;

    public SendFileResult(int messageId, String fileId) {
        this.messageId = messageId;
        this.fileId = fileId;
    }

    public int getMessageId() {
        return messageId;
    }

    public String getFileId() {
        return fileId;
    }
}
