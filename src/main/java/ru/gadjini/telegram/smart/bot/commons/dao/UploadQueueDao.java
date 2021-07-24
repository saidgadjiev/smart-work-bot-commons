package ru.gadjini.telegram.smart.bot.commons.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import ru.gadjini.telegram.smart.bot.commons.domain.QueueItem;
import ru.gadjini.telegram.smart.bot.commons.domain.UploadQueueItem;
import ru.gadjini.telegram.smart.bot.commons.model.UploadType;
import ru.gadjini.telegram.smart.bot.commons.property.DownloadUploadFileLimitProperties;
import ru.gadjini.telegram.smart.bot.commons.property.ServerProperties;
import ru.gadjini.telegram.smart.bot.commons.service.Jackson;
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

    private Jackson jackson;

    private DownloadUploadFileLimitProperties mediaLimitProperties;

    private WorkQueueDao workQueueDao;

    private UploadQueueItemMapper queueItemMapper;

    private ServerProperties serverProperties;

    private ObjectMapper objectMapper;

    @Autowired
    public UploadQueueDao(JdbcTemplate jdbcTemplate, Jackson jackson,
                          DownloadUploadFileLimitProperties mediaLimitProperties, WorkQueueDao workQueueDao,
                          UploadQueueItemMapper queueItemMapper, ServerProperties serverProperties, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.jackson = jackson;
        this.mediaLimitProperties = mediaLimitProperties;
        this.workQueueDao = workQueueDao;
        this.queueItemMapper = queueItemMapper;
        this.serverProperties = serverProperties;
        this.objectMapper = objectMapper;
    }

    public void create(UploadQueueItem queueItem) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                con -> {
                    PreparedStatement ps = con.prepareStatement("INSERT INTO upload_queue " +
                            "(user_id, method, body, producer_table, progress, status, " +
                            "producer_id, extra, file_size, producer, file_format, upload_type, synced)\n" +
                            "    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id", Statement.RETURN_GENERATED_KEYS);
                    ps.setLong(1, queueItem.getUserId());
                    ps.setString(2, queueItem.getMethod());
                    PGobject pGobject = new PGobject();
                    pGobject.setType("jsonb");
                    pGobject.setValue(jackson.writeValueAsString(queueItem.getBody()));
                    ps.setObject(3, pGobject);
                    ps.setString(4, queueItem.getProducerTable());
                    ps.setString(5, jackson.writeValueAsString(queueItem.getProgress()));
                    ps.setInt(6, queueItem.getStatus().getCode());
                    ps.setInt(7, queueItem.getProducerId());
                    if (queueItem.getExtra() != null) {
                        ps.setObject(8, jackson.writeValueAsString(queueItem.getExtra()));
                    } else {
                        ps.setNull(8, Types.OTHER);
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
        return jdbcTemplate.query("DELETE FROM " + UploadQueueItem.NAME + " WHERE producer = ? AND producer_id IN("
                        + producerIds.stream().map(String::valueOf).collect(Collectors.joining(",")) + ") RETURNING *",
                ps -> ps.setString(1, producer),
                (rs, rowNum) -> map(rs)
        );
    }

    public List<UploadQueueItem> deleteAndGetProcessingOrWaitingByUserId(String producer, long userId) {
        return jdbcTemplate.query("DELETE FROM " + UploadQueueItem.NAME + " WHERE producer = ? AND user_id = ? " +
                        "AND status IN(0,1) RETURNING *",
                ps -> {
                    ps.setString(1, producer);
                    ps.setLong(2, userId);
                },
                (rs, rowNum) -> map(rs)
        );
    }

    public List<UploadQueueItem> deleteAndGetProcessingOrWaitingByProducerId(String producer, int producerId) {
        return jdbcTemplate.query(
                "DELETE FROM " + UploadQueueItem.NAME + " WHERE producer = ? AND producer_id = ? AND status IN(0,1) RETURNING *",
                ps -> {
                    ps.setString(1, producer);
                    ps.setInt(2, producerId);
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
        return jdbcTemplate.query("delete\n" +
                        "from upload_queue dq\n" +
                        "where producer = ?\n" +
                        "  and not exists(select 1 from " + producerTable + " uq where uq.id = dq.producer_id) RETURNING *",
                ps -> ps.setString(1, producer),
                (rs, rowNum) -> map(rs)
        );
    }

    public void updateStatus(int id, QueueItem.Status newStatus) {
        jdbcTemplate.update(
                "UPDATE upload_queue SET status = ? WHERE id = ?",
                ps -> {
                    ps.setInt(1, newStatus.getCode());
                    ps.setInt(2, id);
                }
        );
    }

    public int updateCaption(int id, String caption) {
        return jdbcTemplate.update(
                "update upload_queue set body = body || '{\"caption\": \"" + caption + "\"}'::jsonb where id = ?",
                ps -> {
                    ps.setInt(1, id);
                }
        );
    }

    public int updateThumb(int id, InputFile thumb) {
        try {
            String json = objectMapper.writeValueAsString(thumb);

            return jdbcTemplate.update(
                    "update upload_queue set body = body || '{\"thumb\": \"" + json + "\"}'::jsonb where id = ?",
                    ps -> ps.setInt(1, id)
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public UploadQueueItem getById(int id) {
        return jdbcTemplate.query("SELECT * FROM upload_queue WHERE id = ?",
                ps -> ps.setInt(1, id),
                rs -> rs.next() ? map(rs) : null
        );
    }

    public UploadQueueItem updateUploadType(int id, UploadType uploadType) {
        return jdbcTemplate.query("UPDATE upload_queue SET upload_type = ? WHERE id = ? AND status = ? RETURNING *",
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
