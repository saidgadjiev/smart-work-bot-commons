package ru.gadjini.telegram.smart.bot.commons.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.telegram.smart.bot.commons.configuration.SmartBotConfiguration;
import ru.gadjini.telegram.smart.bot.commons.domain.DownloadQueueItem;
import ru.gadjini.telegram.smart.bot.commons.domain.TgFile;

import java.util.List;

@Repository
@Profile({SmartBotConfiguration.PROFILE_PROD_SECONDARY, SmartBotConfiguration.PROFILE_DEV_SECONDARY})
public class DownloadSynchronizerDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public DownloadSynchronizerDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<DownloadQueueItem> getUnsynchronizedDownloads(String producer, String synchronizationColumn) {
        return jdbcTemplate.query(
                "select id, (file).*, file_path\n" +
                        "from downloading_queue\n" +
                        "where status = 3 and " + synchronizationColumn + " = false and producer = '" + producer + "'  ORDER BY attempts DESC",
                (resultSet, i) -> {
                    DownloadQueueItem queueItem = new DownloadQueueItem();

                    queueItem.setFilePath(resultSet.getString(DownloadQueueItem.FILE_PATH));
                    TgFile tgFile = new TgFile();
                    tgFile.setFileId(resultSet.getString(TgFile.FILE_ID));
                    tgFile.setFileName(resultSet.getString(TgFile.FILE_NAME));
                    tgFile.setMimeType(resultSet.getString(TgFile.MIME_TYPE));
                    tgFile.setSize(resultSet.getLong(TgFile.SIZE));

                    queueItem.setId(resultSet.getInt(DownloadQueueItem.ID));

                    queueItem.setFile(tgFile);

                    return queueItem;
                }
        );
    }

    public void synchronize(int id, String synchronizationColumn) {
        jdbcTemplate.update(
                "UPDATE downloading_queue SET " + synchronizationColumn + " = true WHERE id = ?",
                ps -> ps.setInt(1, id)
        );
    }
}
