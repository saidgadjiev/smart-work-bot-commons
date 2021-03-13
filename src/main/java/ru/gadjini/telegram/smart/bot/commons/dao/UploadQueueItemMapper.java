package ru.gadjini.telegram.smart.bot.commons.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.*;
import ru.gadjini.telegram.smart.bot.commons.domain.UploadQueueItem;
import ru.gadjini.telegram.smart.bot.commons.model.Progress;
import ru.gadjini.telegram.smart.bot.commons.model.UploadType;
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

    private Gson gson;

    private ObjectMapper objectMapper;

    @Autowired
    public UploadQueueItemMapper(Gson gson, ObjectMapper objectMapper) {
        this.gson = gson;
        this.objectMapper = objectMapper;
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
            try {
                item.setProgress(objectMapper.readValue(progress, Progress.class));
            } catch (JsonProcessingException e) {
                throw new SQLException(e);
            }
        }
        String extra = rs.getString(UploadQueueItem.EXTRA);
        if (StringUtils.isNotBlank(extra)) {
            item.setExtra(gson.fromJson(extra, JsonElement.class));
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

        return item;
    }

    public Object deserializeBody(String method, String body) {
        switch (method) {
            case SendDocument.PATH:
                return gson.fromJson(body, SendDocument.class);
            case SendAudio.PATH:
                return gson.fromJson(body, SendAudio.class);
            case SendVideo.PATH:
                return gson.fromJson(body, SendVideo.class);
            case SendVoice.PATH:
                return gson.fromJson(body, SendVoice.class);
            case SendSticker.PATH:
                return gson.fromJson(body, SendSticker.class);
        }

        throw new IllegalArgumentException("Unsupported method " + method);
    }
}
