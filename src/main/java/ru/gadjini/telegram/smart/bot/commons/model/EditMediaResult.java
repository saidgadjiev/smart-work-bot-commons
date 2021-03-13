package ru.gadjini.telegram.smart.bot.commons.model;

public class EditMediaResult {

    private String fileId;

    public EditMediaResult(String fileId) {
        this.fileId = fileId;
    }

    public String getFileId() {
        return fileId;
    }
}
