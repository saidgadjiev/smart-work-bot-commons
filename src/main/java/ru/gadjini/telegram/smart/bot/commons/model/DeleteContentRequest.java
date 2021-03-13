package ru.gadjini.telegram.smart.bot.commons.model;

public class DeleteContentRequest {

    private String filePath;

    private boolean deleteParentDir;

    public DeleteContentRequest() {

    }

    public DeleteContentRequest(String filePath, boolean deleteParentDir) {
        this.filePath = filePath;
        this.deleteParentDir = deleteParentDir;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public boolean isDeleteParentDir() {
        return deleteParentDir;
    }

    public void setDeleteParentDir(boolean deleteParentDir) {
        this.deleteParentDir = deleteParentDir;
    }
}
