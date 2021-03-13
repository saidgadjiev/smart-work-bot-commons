package ru.gadjini.telegram.smart.bot.commons.service.queue;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.gadjini.telegram.smart.bot.commons.dao.DownloadQueueDao;
import ru.gadjini.telegram.smart.bot.commons.dao.QueueDao;
import ru.gadjini.telegram.smart.bot.commons.domain.DownloadQueueItem;
import ru.gadjini.telegram.smart.bot.commons.domain.QueueItem;
import ru.gadjini.telegram.smart.bot.commons.domain.TgFile;
import ru.gadjini.telegram.smart.bot.commons.io.SmartTempFile;
import ru.gadjini.telegram.smart.bot.commons.property.ServerProperties;
import ru.gadjini.telegram.smart.bot.commons.service.concurrent.SmartExecutorService;
import ru.gadjini.telegram.smart.bot.commons.service.file.temp.TempFileService;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Service
public class DownloadQueueService extends QueueService {

    private DownloadQueueDao downloadingQueueDao;

    private ServerProperties serverProperties;

    private TempFileService tempFileService;

    @Autowired
    public DownloadQueueService(DownloadQueueDao downloadingQueueDao, ServerProperties serverProperties, TempFileService tempFileService) {
        this.downloadingQueueDao = downloadingQueueDao;
        this.serverProperties = serverProperties;
        this.tempFileService = tempFileService;
    }

    public List<DownloadQueueItem> poll(String producer, SmartExecutorService.JobWeight weight, int limit) {
        return downloadingQueueDao.poll(producer, weight, limit);
    }

    public long unusedDownloadsCount(String producer, String producerTable, SmartExecutorService.JobWeight jobWeight) {
        return downloadingQueueDao.unusedDownloadsCount(producer, producerTable, jobWeight);
    }

    @Transactional
    public void create(Collection<TgFile> files, String producerTable, String producer, int producerId, int userId, Object extra) {
        for (TgFile file : files) {
            DownloadQueueItem queueItem = new DownloadQueueItem();
            queueItem.setFile(file);
            queueItem.setProducerTable(producerTable);
            queueItem.setProducer(producer);
            queueItem.setProgress(file.getProgress());
            queueItem.setFilePath(file.getFilePath());
            queueItem.setDeleteParentDir(file.isDeleteParentDir());
            queueItem.setProducerId(producerId);
            queueItem.setStatus(QueueItem.Status.WAITING);
            queueItem.setUserId(userId);
            queueItem.setExtra(extra);
            if (serverProperties.isPrimaryServer()) {
                queueItem.setSynced(true);
            }

            downloadingQueueDao.create(queueItem, DownloadQueueItem.getSynchronizationColumn(serverProperties.getNumber()));
        }
    }

    public List<DownloadQueueItem> getDownloads(String producer, int producerId) {
        return getDownloads(producer, Set.of(producerId));
    }

    public List<DownloadQueueItem> getDownloads(String producer, Set<Integer> producerIds) {
        return downloadingQueueDao.getDownloads(producer, producerIds);
    }

    public void setCompleted(int id, String filePath) {
        downloadingQueueDao.setCompleted(id, filePath);
    }

    public List<DownloadQueueItem> deleteByProducerIdsWithReturning(String producer, Set<Integer> producerIds) {
        return downloadingQueueDao.deleteByProducerIdsWithReturning(producer, producerIds);
    }

    public List<DownloadQueueItem> deleteOrphanDownloads(String producer, String producerTable) {
        return downloadingQueueDao.deleteOrphan(producer, producerTable);
    }

    public void deleteCompletedAndOrphans(String producer, String producerTable, Set<Integer> producerIds) {
        List<DownloadQueueItem> deleted = new ArrayList<>(deleteByProducerIdsWithReturning(producerTable, producerIds));
        List<DownloadQueueItem> orphanDownloads = deleteOrphanDownloads(producer, producerTable);
        deleted.addAll(orphanDownloads);
        releaseResources(deleted);
    }

    public void releaseResources(List<DownloadQueueItem> downloadQueueItems) {
        for (DownloadQueueItem downloadQueueItem : downloadQueueItems) {
            if (StringUtils.isNotBlank(downloadQueueItem.getFilePath())) {
                tempFileService.delete(new SmartTempFile(new File(downloadQueueItem.getFilePath()), downloadQueueItem.isDeleteParentDir()));
            }
        }
    }

    public long floodWaitsCount() {
        return downloadingQueueDao.countFloodWaits();
    }

    @Override
    public QueueDao getQueueDao() {
        return downloadingQueueDao;
    }
}
