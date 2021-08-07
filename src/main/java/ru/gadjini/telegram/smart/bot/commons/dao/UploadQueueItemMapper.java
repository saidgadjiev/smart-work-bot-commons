package ru.gadjini.telegram.smart.bot.commons.dao;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.*;
import ru.gadjini.telegram.smart.bot.commons.domain.FileSource;
import ru.gadjini.telegram.smart.bot.commons.domain.QueueItem;
import ru.gadjini.telegram.smart.bot.commons.domain.TgFile;
import ru.gadjini.telegram.smart.bot.commons.domain.UploadQueueItem;
import ru.gadjini.telegram.smart.bot.commons.model.Progress;
import ru.gadjini.telegram.smart.bot.commons.model.UploadType;
import ru.gadjini.telegram.smart.bot.commons.service.Jackson;
import ru.gadjini.telegram.smart.bot.commons.service.format.Format;
import ru.gadjini.telegram.smart.bot.commons.utils.JdbcUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Set;

@Component
public class UploadQueueItemMapper {

    private Jackson jackson;

    @Autowired
    public UploadQueueItemMapper(Jackson jackson) {
        this.jackson = jackson;
    }

    public UploadQueueItem map(ResultSet rs) throws SQLException {
        UploadQueueItem item = new UploadQueueItem();
        item.setId(rs.getInt(UploadQueueItem.ID));

        item.setUserId(rs.getInt(UploadQueueItem.USER_ID));
        item.setProducerTable(rs.getString(UploadQueueItem.PRODUCER_TABLE));
        item.setProducer(rs.getString(UploadQueueItem.PRODUCER));
        item.setFileSize(rs.getLong(UploadQueueItem.FILE_SIZE));
        item.setProducerId(rs.getInt(UploadQueueItem.PRODUCER_ID));
        item.setMethod(rs.getString(UploadQueueItem.METHOD));
        item.setBody(deserializeBody(item.getMethod(), rs.getString(UploadQueueItem.BODY)));

        Set<String> columns = JdbcUtils.getColumnNames(rs.getMetaData());
        if (columns.contains(UploadQueueItem.THUMB_FILE_ID)) {
            item.setThumbFileId(rs.getString(UploadQueueItem.THUMB_FILE_ID));
        }
        if (columns.contains(TgFile.FILE_ID)) {
            String customThumbFileId = rs.getString(TgFile.FILE_ID);
            if (!rs.wasNull()) {
                TgFile customThumb = new TgFile();
                customThumb.setFileId(customThumbFileId);
                customThumb.setFileName(rs.getString(TgFile.FILE_NAME));
                customThumb.setSize(rs.getLong(TgFile.SIZE));
                String source = rs.getString(TgFile.SOURCE);
                if (StringUtils.isNotBlank(source)) {
                    customThumb.setSource(FileSource.valueOf(source));
                }
                String format = rs.getString(TgFile.FORMAT);
                if (StringUtils.isNotBlank(format)) {
                    customThumb.setFormat(Format.valueOf(format));
                }
                item.setCustomThumb(customThumb);
            }
        }
        if (columns.contains(UploadQueueItem.CUSTOM_CAPTION)) {
            item.setCustomCaption(rs.getString(UploadQueueItem.CUSTOM_CAPTION));
        }
        if (columns.contains(UploadQueueItem.CUSTOM_FILE_NAME)) {
            item.setCustomFileName(rs.getString(UploadQueueItem.CUSTOM_FILE_NAME));
        }

        Timestamp nextRunAt = rs.getTimestamp(UploadQueueItem.NEXT_RUN_AT);
        if (nextRunAt != null) {
            item.setNextRunAt(ZonedDateTime.of(nextRunAt.toLocalDateTime(), ZoneOffset.UTC));
        }
        String progress = rs.getString(UploadQueueItem.PROGRESS);
        if (StringUtils.isNotBlank(progress)) {
            item.setProgress(jackson.readValue(progress, Progress.class));
        }
        String extra = rs.getString(UploadQueueItem.EXTRA);
        if (StringUtils.isNotBlank(extra)) {
            item.setExtra(jackson.readValue(extra, JsonNode.class));
        }
        if (columns.contains(UploadQueueItem.UPLOAD_TYPE)) {
            String uploadType = rs.getString(UploadQueueItem.UPLOAD_TYPE);
            if (StringUtils.isNotBlank(uploadType)) {
                item.setUploadType(UploadType.valueOf(uploadType));
            }
        }
        if (columns.contains(UploadQueueItem.FILE_FORMAT)) {
            String fileFormat = rs.getString(UploadQueueItem.FILE_FORMAT);
            if (StringUtils.isNotBlank(fileFormat)) {
                item.setFileFormat(Format.valueOf(fileFormat));
            }
        }
        if (columns.contains(QueueItem.ATTEMPTS)) {
            item.setAttempts(rs.getInt(QueueItem.ATTEMPTS));
        }

        return item;
    }

    public Object deserializeBody(String method, String body) {
        switch (method) {
            case SendDocument.PATH:
                return jackson.readValue(body, SendDocument.class);
            case SendAudio.PATH:
                return jackson.readValue(body, SendAudio.class);
            case SendVideo.PATH:
                return jackson.readValue(body, SendVideo.class);
            case SendVoice.PATH:
                return jackson.readValue(body, SendVoice.class);
            case SendSticker.PATH:
                return jackson.readValue(body, SendSticker.class);
            case SendVideoNote.PATH:
                return jackson.readValue(body, SendVideoNote.class);
        }

        throw new IllegalArgumentException("Unsupported method " + method);
    }
}
