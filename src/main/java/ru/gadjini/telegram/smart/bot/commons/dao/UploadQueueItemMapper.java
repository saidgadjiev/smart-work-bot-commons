package ru.gadjini.telegram.smart.bot.commons.dao;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.*;
import ru.gadjini.telegram.smart.bot.commons.domain.QueueItem;
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
        item.setProducerId(rs.getInt(UploadQueueItem.PRODUCER_ID));
        item.setMethod(rs.getString(UploadQueueItem.METHOD));
        item.setBody(deserializeBody(item.getMethod(), rs.getString(UploadQueueItem.BODY)));

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
        Set<String> columns = JdbcUtils.getColumnNames(rs.getMetaData());
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
