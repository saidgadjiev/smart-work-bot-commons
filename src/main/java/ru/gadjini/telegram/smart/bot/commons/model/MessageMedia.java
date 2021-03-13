package ru.gadjini.telegram.smart.bot.commons.model;

import org.apache.commons.lang3.StringUtils;
import ru.gadjini.telegram.smart.bot.commons.domain.FileSource;
import ru.gadjini.telegram.smart.bot.commons.domain.TgFile;
import ru.gadjini.telegram.smart.bot.commons.service.format.Format;
import ru.gadjini.telegram.smart.bot.commons.utils.MemoryUtils;

public class MessageMedia {

    private String fileId;

    private String fileName;

    private String mimeType;

    private Format format;

    private long fileSize;

    private String thumb;

    private long thumbFileSize;

    private String cachedFileId;

    private FileSource source;

    private String audioPerformer;

    private String audioTitle;

    private Integer duration;

    private Integer width;

    private Integer height;

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public Format getFormat() {
        return format;
    }

    public void setFormat(Format format) {
        this.format = format;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = getFixedFileName(fileName);
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public String getCachedFileId() {
        return cachedFileId;
    }

    public void setCachedFileId(String cachedFileId) {
        this.cachedFileId = cachedFileId;
    }

    public FileSource getSource() {
        return source;
    }

    public void setSource(FileSource source) {
        this.source = source;
    }

    public String getAudioPerformer() {
        return audioPerformer;
    }

    public void setAudioPerformer(String audioPerformer) {
        this.audioPerformer = audioPerformer;
    }

    public String getAudioTitle() {
        return audioTitle;
    }

    public void setAudioTitle(String audioTitle) {
        this.audioTitle = audioTitle;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public long getThumbFileSize() {
        return thumbFileSize;
    }

    public void setThumbFileSize(long thumbFileSize) {
        this.thumbFileSize = thumbFileSize;
    }

    public TgFile toTgFile() {
        TgFile tgFile = new TgFile();
        tgFile.setFileId(fileId);
        tgFile.setFormat(format);
        tgFile.setFileName(fileName);
        tgFile.setMimeType(mimeType);
        tgFile.setSize(fileSize);
        tgFile.setThumb(thumb);
        tgFile.setSource(source);
        tgFile.setAudioTitle(audioTitle);
        tgFile.setAudioPerformer(audioPerformer);
        tgFile.setThumbSize(thumbFileSize);

        return tgFile;
    }

    private String getFixedFileName(String fileName) {
        return StringUtils.defaultString(fileName, "").replace("\"", "").replace(";", "");
    }

    @Override
    public String toString() {
        return "MessageMedia{" +
                "fileId='" + fileId + '\'' +
                ", fileName='" + fileName + '\'' +
                ", mimeType='" + mimeType + '\'' +
                ", format=" + format +
                ", fileSize=" + MemoryUtils.humanReadableByteCount(fileSize) +
                ", thumb='" + thumb + '\'' +
                ", cachedFileId='" + cachedFileId + '\'' +
                '}';
    }
}
