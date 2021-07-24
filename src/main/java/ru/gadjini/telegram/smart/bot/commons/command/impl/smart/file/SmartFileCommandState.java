package ru.gadjini.telegram.smart.bot.commons.command.impl.smart.file;

import ru.gadjini.telegram.smart.bot.commons.command.impl.smart.file.state.SmartFileStateName;

public class SmartFileCommandState {

    private int messageId;

    private SmartFileStateName stateName;

    private String caption;

    private String thumb;

    private int uploadId;

    public SmartFileStateName getStateName() {
        return stateName;
    }

    public void setStateName(SmartFileStateName stateName) {
        this.stateName = stateName;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public int getUploadId() {
        return uploadId;
    }

    public void setUploadId(int uploadId) {
        this.uploadId = uploadId;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }
}
