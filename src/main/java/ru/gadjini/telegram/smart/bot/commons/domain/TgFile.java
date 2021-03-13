package ru.gadjini.telegram.smart.bot.commons.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import org.postgresql.util.PGobject;
import ru.gadjini.telegram.smart.bot.commons.model.Progress;
import ru.gadjini.telegram.smart.bot.commons.service.format.Format;

import java.sql.SQLException;

public class TgFile {

    public static final String TYPE = "tg_file";

    public static final String FILE_ID = "file_id";

    public static final String FILE_NAME = "file_name";

    public static final String MIME_TYPE = "mime_type";

    public static final String SIZE = "size";

    public static final String THUMB_SIZE = "thumb_size";

    public static final String THUMB = "thumb";

    public static final String FORMAT = "format";

    public static final String AUDIO_PERFORMER = "audio_performer";

    public static final String AUDIO_TITLE = "audio_title";

    @JsonProperty(FILE_ID)
    private String fileId;

    @JsonProperty(FILE_NAME)
    private String fileName;

    @JsonProperty(MIME_TYPE)
    private String mimeType;

    @JsonProperty(FORMAT)
    private Format format;

    @JsonProperty(SIZE)
    private long size;

    @JsonProperty(THUMB)
    private String thumb;

    @JsonProperty(THUMB_SIZE)
    private long thumbSize;

    private FileSource source;

    @JsonProperty(AUDIO_PERFORMER)
    private String audioPerformer;

    @JsonProperty(AUDIO_TITLE)
    private String audioTitle;

    private Integer duration;

    @JsonIgnore
    private Progress progress;

    @JsonIgnore
    private String filePath;

    @JsonIgnore
    private boolean deleteParentDir;

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public Format getFormat() {
        return format;
    }

    public void setFormat(Format format) {
        this.format = format;
    }

    public FileSource getSource() {
        return source;
    }

    public void setSource(FileSource source) {
        this.source = source;
    }

    public String getAudioTitle() {
        return audioTitle;
    }

    public void setAudioTitle(String audioTitle) {
        this.audioTitle = audioTitle;
    }

    public String getAudioPerformer() {
        return audioPerformer;
    }

    public void setAudioPerformer(String audioPerformer) {
        this.audioPerformer = audioPerformer;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Progress getProgress() {
        return progress;
    }

    public void setProgress(Progress progress) {
        this.progress = progress;
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

    public void setThumbSize(long thumbSize) {
        this.thumbSize = thumbSize;
    }

    public long getThumbSize() {
        return thumbSize;
    }

    public String sql() {
        StringBuilder sql = new StringBuilder("(\"");

        sql.append(escapeDoubleQuotes(fileId)).append("\",");
        if (StringUtils.isNotBlank(mimeType)) {
            sql.append("\"").append(mimeType).append("\",");
        } else {
            sql.append(",");
        }
        if (StringUtils.isNotBlank(fileName)) {
            sql.append("\"").append(escapeDoubleQuotes(fileName)).append("\",");
        } else {
            sql.append(",");
        }
        sql.append(size).append(",");
        if (StringUtils.isNotBlank(thumb)) {
            sql.append("\"").append(thumb).append("\",");
        } else {
            sql.append(",");
        }
        if (format != null) {
            sql.append("\"").append(format.name()).append("\",");
        } else {
            sql.append(",");
        }
        if (StringUtils.isNotBlank(audioPerformer)) {
            sql.append("\"").append(escapeDoubleQuotes(audioPerformer)).append("\",");
        } else {
            sql.append(",");
        }
        if (StringUtils.isNotBlank(audioTitle)) {
            sql.append("\"").append(escapeDoubleQuotes(audioTitle)).append("\",");
        } else {
            sql.append(",");
        }
        if (source != null) {
            sql.append("\"").append(source.name()).append("\",");
        } else {
            sql.append(",");
        }
        if (duration != null) {
            sql.append(duration).append(",");
        } else {
            sql.append(",");
        }
        sql.append(thumbSize);

        sql.append(")");

        return sql.toString();
    }

    public PGobject sqlObject() {
        PGobject pGobject = new PGobject();
        pGobject.setType(TYPE);
        try {
            pGobject.setValue(sql());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return pGobject;
    }

    private String escapeDoubleQuotes(String value) {
        return value.replace("\"", "\"\"");
    }
}
