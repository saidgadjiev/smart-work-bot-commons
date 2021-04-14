package ru.gadjini.telegram.smart.bot.commons.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.gadjini.telegram.smart.bot.commons.domain.QueueItem;
import ru.gadjini.telegram.smart.bot.commons.domain.UploadQueueItem;
import ru.gadjini.telegram.smart.bot.commons.model.UploadType;
import ru.gadjini.telegram.smart.bot.commons.property.MediaLimitProperties;
import ru.gadjini.telegram.smart.bot.commons.property.ServerProperties;
import ru.gadjini.telegram.smart.bot.commons.service.concurrent.SmartExecutorService;
import ru.gadjini.telegram.smart.bot.commons.service.format.Format;

import java.sql.*;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class UploadQueueDao extends QueueDao {

    private JdbcTemplate jdbcTemplate;

    private Gson gson;

    private ObjectMapper objectMapper;

    private MediaLimitProperties mediaLimitProperties;

    private WorkQueueDao workQueueDao;

    private UploadQueueItemMapper queueItemMapper;

    private ServerProperties serverProperties;

    @Autowired
    public UploadQueueDao(JdbcTemplate jdbcTemplate, @Qualifier("botapi") Gson gson, ObjectMapper objectMapper,
                          MediaLimitProperties mediaLimitProperties, WorkQueueDao workQueueDao,
                          UploadQueueItemMapper queueItemMapper, ServerProperties serverProperties) {
        this.jdbcTemplate = jdbcTemplate;
        this.gson = gson;
        this.objectMapper = objectMapper;
        this.mediaLimitProperties = mediaLimitProperties;
        this.workQueueDao = workQueueDao;
        this.queueItemMapper = queueItemMapper;
        this.serverProperties = serverProperties;
    }

    public void create(UploadQueueItem queueItem) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                con -> {
                    PreparedStatement ps = con.prepareStatement("INSERT INTO upload_queue " +
                            "(user_id, method, body, producer_table, progress, status, " +
                            "producer_id, extra, file_size, producer, file_format, upload_type, synced)\n" +
                            "    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
                    ps.setInt(1, queueItem.getUserId());
                    ps.setString(2, queueItem.getMethod());
                    ps.setString(3, gson.toJson(queueItem.getBody()));
                    ps.setString(4, queueItem.getProducerTable());
                    try {
                        ps.setString(5, objectMapper.writeValueAsString(queueItem.getProgress()));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                    ps.setInt(6, queueItem.getStatus().getCode());
                    ps.setInt(7, queueItem.getProducerId());
                    if (queueItem.getExtra() != null) {
                        ps.setString(8, gson.toJson(queueItem.getExtra()));
                    } else {
                        ps.setNull(8, Types.VARCHAR);
                    }
                    ps.setLong(9, queueItem.getFileSize());
                    ps.setString(10, queueItem.getProducer());
                    if (queueItem.getFileFormat() == null) {
                        ps.setNull(11, Types.VARCHAR);
                    } else {
                        ps.setString(11, queueItem.getFileFormat().name());
                    }
                    ps.setString(12, queueItem.getUploadType().name());
                    ps.setBoolean(13, queueItem.isSynced());

                    return ps;
                },
                keyHolder
        );

        int id = ((Number) keyHolder.getKeys().get(UploadQueueItem.ID)).intValue();
        queueItem.setId(id);
    }

    public List<UploadQueueItem> deleteByProducerIdsWithReturning(String producer, Set<Integer> producerIds) {
        if (producerIds.isEmpty()) {
            return Collections.emptyList();
        }
        return jdbcTemplate.query(
                "WITH del AS(DELETE FROM " + UploadQueueItem.NAME + " WHERE producer = ? AND producer_id IN("
                        + producerIds.stream().map(String::valueOf).collect(Collectors.joining(",")) + ") RETURNING *) " +
                        "SELECT * FROM del",
                ps -> {
                    ps.setString(1, producer);
                },
                (rs, rowNum) -> map(rs)
        );
    }

    public List<UploadQueueItem> deleteAndGetProcessingOrWaitingByUserId(String producer, int userId) {
        return jdbcTemplate.query(
                "WITH del AS(DELETE FROM " + UploadQueueItem.NAME + " WHERE producer = ? AND user_id = ? AND status IN(0,1) RETURNING *) " +
                        "SELECT * FROM del",
                ps -> {
                    ps.setString(1, producer);
                    ps.setInt(2, userId);
                },
                (rs, rowNum) -> map(rs)
        );
    }

    public List<UploadQueueItem> poll(String producer, SmartExecutorService.JobWeight jobWeight, int limit) {
        return jdbcTemplate.query(
                "UPDATE upload_queue SET " + QueueDao.getUpdateList(serverProperties.getNumber()) +
                        "WHERE id IN(SELECT id FROM upload_queue qu WHERE qu.status = 0 AND qu.next_run_at <= now() " +
                        "and qu.producer = ? and synced = true " +
                        "AND file_size " + (jobWeight.equals(SmartExecutorService.JobWeight.LIGHT) ? "<=" : ">") + " ?\n" +
                        QueueDao.POLL_ORDER_BY + " LIMIT " + limit + ")\n" +
                        "RETURNING *",
                ps -> {
                    ps.setString(1, producer);
                    ps.setLong(2, mediaLimitProperties.getLightFileMaxWeight());
                },
                (rs, rowNum) -> map(rs)
        );
    }

    public List<UploadQueueItem> deleteOrphan(String producer, String producerTable) {
        return jdbcTemplate.query(
                "WITH del AS(delete\n" +
                        "from upload_queue dq\n" +
                        "where producer = ?\n" +
                        "  and not exists(select 1 from " + producerTable + " uq where uq.id = dq.producer_id) RETURNING *) " +
                        "SELECT * FROM del",
                ps -> ps.setString(1, producer),
                (rs, rowNum) -> map(rs)
        );
    }

    public void updateStatus(int id, QueueItem.Status newStatus, QueueItem.Status oldStatus) {
        jdbcTemplate.update(
                "UPDATE upload_queue SET status = ? WHERE id = ? AND status = ?",
                ps -> {
                    ps.setInt(1, newStatus.getCode());
                    ps.setInt(2, id);
                    ps.setInt(3, oldStatus.getCode());
                }
        );
    }

    public UploadQueueItem updateUploadType(int id, UploadType uploadType) {
        return jdbcTemplate.query(
                "WITH upd AS (UPDATE upload_queue SET upload_type = ? WHERE id = ? AND status = ? RETURNING id, user_id, upload_type, file_format)\n" +
                        "SELECT * FROM upd",
                ps -> {
                    ps.setString(1, uploadType.name());
                    ps.setInt(2, id);
                    ps.setInt(3, QueueItem.Status.BLOCKED.getCode());
                },
                rs -> {
                    if (rs.next()) {
                        UploadQueueItem queueItem = new UploadQueueItem();
                        queueItem.setUploadType(UploadType.valueOf(rs.getString(UploadQueueItem.UPLOAD_TYPE)));
                        queueItem.setId(rs.getInt(UploadQueueItem.ID));
                        queueItem.setUserId(rs.getInt(UploadQueueItem.USER_ID));
                        String fileFormat = rs.getString(UploadQueueItem.FILE_FORMAT);
                        if (StringUtils.isNotBlank(fileFormat)) {
                            queueItem.setFileFormat(Format.valueOf(fileFormat));
                        }

                        return queueItem;
                    }

                    return null;
                }
        );
    }

    public void setWaitingExpiredSmartUploads(long expirationInSeconds) {
        jdbcTemplate.update(
                "UPDATE upload_queue SET status = 0 WHERE status = 4 AND " +
                        "created_at + interval '" + expirationInSeconds + " seconds' < now()"
        );
    }

    public Format getFileFormat(int id) {
        return jdbcTemplate.query(
                "SELECT file_format FROM upload_queue WHERE id = ?",
                ps -> ps.setInt(1, id),
                rs -> {
                    if (rs.next()) {
                        String fileFormat = rs.getString(UploadQueueItem.FILE_FORMAT);
                        if (StringUtils.isNotBlank(fileFormat)) {
                            return Format.valueOf(fileFormat);
                        }
                    }

                    return null;
                }
        );
    }

    @Override
    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    @Override
    public QueueDaoDelegate getQueueDaoDelegate() {
        return new QueueDaoDelegate() {
            @Override
            public String getQueueName() {
                return UploadQueueItem.NAME;
            }

            @Override
            public String getBaseAdditionalClause() {
                return "AND producer = '" + workQueueDao.getProducerName() + "'";
            }
        };
    }

    private UploadQueueItem map(ResultSet rs) throws SQLException {
        return queueItemMapper.map(rs);
    }
}
