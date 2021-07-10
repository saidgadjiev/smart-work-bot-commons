package ru.gadjini.telegram.smart.bot.commons.service.file;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.dao.WorkQueueDao;
import ru.gadjini.telegram.smart.bot.commons.domain.QueueItem;
import ru.gadjini.telegram.smart.bot.commons.domain.TgFile;
import ru.gadjini.telegram.smart.bot.commons.job.DownloadJob;
import ru.gadjini.telegram.smart.bot.commons.service.queue.DownloadQueueService;

import java.util.Collection;
import java.util.Collections;

@Service
public class FileDownloadService {

    private DownloadQueueService queueService;

    private WorkQueueDao workQueueDao;

    private DownloadJob downloadingJob;

    @Autowired
    public FileDownloadService(DownloadQueueService queueService, WorkQueueDao workQueueDao) {
        this.queueService = queueService;
        this.workQueueDao = workQueueDao;
    }

    @Autowired
    public void setDownloadingJob(DownloadJob downloadingJob) {
        this.downloadingJob = downloadingJob;
    }

    public void createDownload(TgFile file, int producerId, long userId, Object extra) {
        createDownloads(Collections.singletonList(file), producerId, userId, extra);
    }

    public void createDownloads(Collection<TgFile> files, int producerId, long userId, Object extra) {
        queueService.create(files, QueueItem.Status.WAITING, workQueueDao.getQueueName(), workQueueDao.getProducerName(), producerId, userId, extra);
    }

    public void createCompletedDownloads(Collection<TgFile> files, int producerId, long userId, Object extra) {
        queueService.create(files, QueueItem.Status.COMPLETED, workQueueDao.getQueueName(), workQueueDao.getProducerName(), producerId, userId, extra);
    }

    public void createDownload(TgFile file, int producerId, long userId) {
        createDownloads(Collections.singletonList(file), producerId, userId, null);
    }

    public void createDownloads(Collection<TgFile> files, int producerId, long userId) {
        createDownloads(files, producerId, userId, null);
    }

    public void cancelDownloads(int producerId) {
        downloadingJob.cancelDownloads(workQueueDao.getProducerName(), producerId);
    }

    public void cancelProcessingOrWaitingDownloads(int producerId) {
        downloadingJob.cancelProcessingOrWaitingDownloads(workQueueDao.getProducerName(), producerId);
    }

    public void cancelDownloadsByUserId(long userId) {
        downloadingJob.cancelDownloadsByUserId(workQueueDao.getProducerName(), userId);
    }

    public void cancelDownloads() {
        downloadingJob.cancelDownloads();
    }

    public void deleteDownload(int id) {
        queueService.deleteById(id);
    }
}
