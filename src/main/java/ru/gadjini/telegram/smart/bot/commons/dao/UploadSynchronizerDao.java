package ru.gadjini.telegram.smart.bot.commons.dao;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.telegram.smart.bot.commons.common.Profiles;
import ru.gadjini.telegram.smart.bot.commons.domain.UploadQueueItem;

import java.util.List;

@Repository
@Profile({Profiles.PROFILE_PROD_PRIMARY, Profiles.PROFILE_DEV_PRIMARY})
public class UploadSynchronizerDao {

    private JdbcTemplate jdbcTemplate;

    private UploadQueueItemMapper uploadQueueItemMapper;

    public UploadSynchronizerDao(JdbcTemplate jdbcTemplate, UploadQueueItemMapper uploadQueueItemMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.uploadQueueItemMapper = uploadQueueItemMapper;
    }

    public List<UploadQueueItem> getUnsynchronizedUploads(String producer) {
        return jdbcTemplate.query(
                "select id, method, body, file_size\n" +
                        "from upload_queue\n" +
                        "where status = 0 and synced = false and producer ='" + producer + "' ORDER BY attempts DESC",
                (resultSet, i) -> {
                    UploadQueueItem queueItem = new UploadQueueItem();

                    queueItem.setId(resultSet.getInt(UploadQueueItem.ID));
                    queueItem.setFileSize(resultSet.getLong(UploadQueueItem.FILE_SIZE));
                    queueItem.setMethod(resultSet.getString(UploadQueueItem.METHOD));
                    queueItem.setBody(uploadQueueItemMapper.deserializeBody(queueItem.getMethod(), resultSet.getString(UploadQueueItem.BODY)));

                    return queueItem;
                }
        );
    }

    public void synchronize(int id) {
        jdbcTemplate.update(
                "UPDATE upload_queue SET synced = true WHERE id = ?",
                ps -> ps.setInt(1, id)
        );
    }
}
