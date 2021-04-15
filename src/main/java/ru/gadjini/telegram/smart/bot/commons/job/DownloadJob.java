package ru.gadjini.telegram.smart.bot.commons.job;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.dao.WorkQueueDao;
import ru.gadjini.telegram.smart.bot.commons.domain.DownloadQueueItem;
import ru.gadjini.telegram.smart.bot.commons.domain.QueueItem;
import ru.gadjini.telegram.smart.bot.commons.exception.FloodControlException;
import ru.gadjini.telegram.smart.bot.commons.exception.FloodWaitException;
import ru.gadjini.telegram.smart.bot.commons.io.SmartTempFile;
import ru.gadjini.telegram.smart.bot.commons.model.Progress;
import ru.gadjini.telegram.smart.bot.commons.property.FileManagerProperties;
import ru.gadjini.telegram.smart.bot.commons.property.JobsProperties;
import ru.gadjini.telegram.smart.bot.commons.property.MediaLimitProperties;
import ru.gadjini.telegram.smart.bot.commons.service.concurrent.SmartExecutorService;
import ru.gadjini.telegram.smart.bot.commons.service.file.FileDownloader;
import ru.gadjini.telegram.smart.bot.commons.service.file.temp.FileTarget;
import ru.gadjini.telegram.smart.bot.commons.service.file.temp.TempFileService;
import ru.gadjini.telegram.smart.bot.commons.service.format.FormatService;
import ru.gadjini.telegram.smart.bot.commons.service.queue.DownloadQueueService;
import ru.gadjini.telegram.smart.bot.commons.service.queue.event.DownloadCompleted;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component
public class DownloadJob extends WorkQueueJobPusher {

    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadJob.class);

    private static final String TAG = "down";

    private DownloadQueueService downloadingQueueService;

    private FileDownloader fileDownloader;

    private TempFileService tempFileService;

    private FileManagerProperties fileManagerProperties;

    private MediaLimitProperties mediaLimitProperties;

    private WorkQueueDao workQueueDao;

    private SmartExecutorService downloadTasksExecutor;

    private final List<DownloadQueueItem> currentDownloads = new ArrayList<>();

    private ApplicationEventPublisher applicationEventPublisher;

    private FormatService formatService;

    private JobsProperties jobsProperties;

    @Value("${available.unused.downloads.count:-1}")
    private int availableUnusedDownloadsCount;

    @Autowired
    public DownloadJob(DownloadQueueService downloadingQueueService, FileDownloader fileDownloader,
                       TempFileService tempFileService, FileManagerProperties fileManagerProperties,
                       MediaLimitProperties mediaLimitProperties, WorkQueueDao workQueueDao,
                       ApplicationEventPublisher applicationEventPublisher, FormatService formatService,
                       JobsProperties jobsProperties) {
        this.downloadingQueueService = downloadingQueueService;
        this.fileDownloader = fileDownloader;
        this.tempFileService = tempFileService;
        this.fileManagerProperties = fileManagerProperties;
        this.mediaLimitProperties = mediaLimitProperties;
        this.workQueueDao = workQueueDao;
        this.applicationEventPublisher = applicationEventPublisher;
        this.formatService = formatService;
        this.jobsProperties = jobsProperties;
    }

    @Autowired
    public void setDownloadTasksExecutor(@Qualifier("downloadTasksExecutor") SmartExecutorService downloadTasksExecutor) {
        this.downloadTasksExecutor = downloadTasksExecutor;
    }

    @PostConstruct
    public final void init() {
        LOGGER.debug("Available unused downloads count {}", availableUnusedDownloadsCount);
        try {
            downloadingQueueService.resetProcessing();
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    public void rejectTask(SmartExecutorService.Job job) {
        downloadingQueueService.setWaitingAndDecrementAttempts(job.getId());
        LOGGER.debug("Rejected({})", job.getId());
    }

    public void doDownloads() {
        super.push();
    }

    @Override
    public SmartExecutorService getExecutor() {
        return downloadTasksExecutor;
    }

    @Override
    public boolean enableJobsLogging() {
        return jobsProperties.isEnableLogging();
    }

    @Override
    public boolean disableJobs() {
        return jobsProperties.isDisable();
    }

    @Override
    public Class<?> getLoggerClass() {
        return getClass();
    }

    @Override
    public List<QueueItem> getTasks(SmartExecutorService.JobWeight weight, int limit) {
        if (availableUnusedDownloadsCount > 0) {
            long unusedDownloadsCount = downloadingQueueService.unusedDownloadsCount(workQueueDao.getProducerName(), workQueueDao.getQueueName(), weight);
            if (unusedDownloadsCount < availableUnusedDownloadsCount) {
                return (List<QueueItem>) (Object) downloadingQueueService.poll(workQueueDao.getProducerName(), weight, limit);
            } else if (jobsProperties.isEnableLogging()) {
                LOGGER.debug("No available downloads({}, {})", availableUnusedDownloadsCount, unusedDownloadsCount);
            }
        } else {
            return (List<QueueItem>) (Object) downloadingQueueService.poll(workQueueDao.getProducerName(), weight, limit);
        }

        return List.of();
    }

    @Override
    public SmartExecutorService.Job createJob(QueueItem item) {
        return new DownloadTask((DownloadQueueItem) item);
    }

    public void cancelDownloads(String producer, int producerId) {
        cancelDownloads(producer, Set.of(producerId));
    }

    public void cancelDownloadsByUserId(String producer, int userId) {
        List<DownloadQueueItem> deleted = downloadingQueueService.deleteAndGetProcessingOrWaitingByUserId(producer, userId);
        downloadTasksExecutor.cancel(deleted.stream().map(DownloadQueueItem::getId).collect(Collectors.toList()), true);
        downloadingQueueService.releaseResources(deleted);
    }

    public void cancelDownloads(String producer, Set<Integer> producerIds) {
        deleteDownloads(producer, producerIds);
    }

    public void deleteDownloads(String producer, Set<Integer> producerIds) {
        List<DownloadQueueItem> deleted = downloadingQueueService.deleteByProducerIdsWithReturning(producer, producerIds);
        downloadTasksExecutor.cancel(deleted.stream().map(DownloadQueueItem::getId).collect(Collectors.toList()), true);
        downloadingQueueService.releaseResources(deleted);
    }

    public void cancelDownloads() {
        downloadTasksExecutor.cancel(currentDownloads.stream().map(DownloadQueueItem::getId).collect(Collectors.toList()), false);
    }

    public final void shutdown() {
        downloadTasksExecutor.shutdown();
    }

    private class DownloadTask implements SmartExecutorService.Job {

        private DownloadQueueItem downloadingQueueItem;

        private volatile Supplier<Boolean> checker;

        private volatile boolean canceledByUser;

        private volatile SmartTempFile tempFile;

        private DownloadTask(DownloadQueueItem downloadingQueueItem) {
            this.downloadingQueueItem = downloadingQueueItem;
        }

        @Override
        public void execute() {
            currentDownloads.add(downloadingQueueItem);
            try {
                if (downloadingQueueItem.getFile().getFormat() == null
                        || downloadingQueueItem.getFile().getFormat().isDownloadable()) {
                    doDownloadFile(downloadingQueueItem);
                } else {
                    downloadingQueueService.setCompleted(downloadingQueueItem.getId());
                    applicationEventPublisher.publishEvent(new DownloadCompleted(downloadingQueueItem));
                }
            } finally {
                currentDownloads.remove(downloadingQueueItem);
            }
        }

        @Override
        public int getId() {
            return downloadingQueueItem.getId();
        }

        @Override
        public SmartExecutorService.JobWeight getWeight() {
            return downloadingQueueItem.getFile().getSize() > mediaLimitProperties.getLightFileMaxWeight()
                    ? SmartExecutorService.JobWeight.HEAVY : SmartExecutorService.JobWeight.LIGHT;
        }

        @Override
        public long getChatId() {
            return downloadingQueueItem.getUserId();
        }

        @Override
        public void setCancelChecker(Supplier<Boolean> checker) {
            this.checker = checker;
        }

        @Override
        public Supplier<Boolean> getCancelChecker() {
            return checker;
        }

        @Override
        public void setCanceledByUser(boolean canceledByUser) {
            this.canceledByUser = canceledByUser;
        }

        @Override
        public boolean isCanceledByUser() {
            return canceledByUser;
        }

        @Override
        public void cancel() {
            fileDownloader.cancelDownloading(downloadingQueueItem.getFile().getFileId(), downloadingQueueItem.getFile().getSize());
            if (canceledByUser) {
                downloadingQueueService.deleteById(downloadingQueueItem.getId());
                LOGGER.debug("Canceled downloading({}, {}, {})", downloadingQueueItem.getFile().getFileId(), downloadingQueueItem.getProducerTable(), downloadingQueueItem.getProducerId());
            }
            if (tempFile != null) {
                tempFileService.delete(tempFile);
            }
        }

        private void doDownloadFile(DownloadQueueItem downloadingQueueItem) {
            if (StringUtils.isBlank(downloadingQueueItem.getFilePath())) {
                String ext;
                if (downloadingQueueItem.getFile().getFormat() != null) {
                    ext = downloadingQueueItem.getFile().getFormat().getExt();
                } else {
                    ext = formatService.getExt(downloadingQueueItem.getFile().getFileName(), downloadingQueueItem.getFile().getMimeType());
                }
                tempFile = tempFileService.createTempFile(FileTarget.DOWNLOAD, downloadingQueueItem.getUserId(),
                        downloadingQueueItem.getFile().getFileId(), TAG, ext);
            } else {
                tempFile = new SmartTempFile(new File(downloadingQueueItem.getFilePath()));
            }
            try {
                fileDownloader.downloadFileByFileId(downloadingQueueItem.getFile().getFileId(), downloadingQueueItem.getFile().getSize(),
                        getProgress(), tempFile, getWeight().equals(SmartExecutorService.JobWeight.HEAVY));

                downloadingQueueService.setCompleted(downloadingQueueItem.getId(), tempFile.getAbsolutePath());
                downloadingQueueItem.setFilePath(tempFile.getAbsolutePath());
                applicationEventPublisher.publishEvent(new DownloadCompleted(downloadingQueueItem));
            } catch (Throwable e) {
                tempFileService.delete(tempFile);

                if (checker == null || !checker.get()) {
                    if (e instanceof FloodControlException) {
                        floodControlException(downloadingQueueItem, (FloodControlException) e);
                    } else if (e instanceof FloodWaitException) {
                        floodWaitException(downloadingQueueItem, (FloodWaitException) e);
                    } else if (FileDownloader.isNoneCriticalDownloadingException(e)) {
                        noneCriticalException(downloadingQueueItem, e);
                    } else {
                        downloadingQueueService.setExceptionStatus(downloadingQueueItem.getId(), e);

                        throw e;
                    }
                }
            }
        }

        private Progress getProgress() {
            return downloadingQueueItem.getAttempts() == 1 ? downloadingQueueItem.getProgress() : null;
        }

        private void noneCriticalException(DownloadQueueItem downloadingQueueItem, Throwable e) {
            downloadingQueueService.setWaitingAndDecrementAttempts(downloadingQueueItem.getId(), fileManagerProperties.getSleepTimeBeforeDownloadAttempt(), e);
        }

        private void floodControlException(DownloadQueueItem downloadingQueueItem, FloodControlException e) {
            downloadingQueueService.setWaitingAndDecrementAttempts(downloadingQueueItem.getId(), e.getSleepTime(), e);
        }

        private void floodWaitException(DownloadQueueItem downloadingQueueItem, FloodWaitException e) {
            long sleepTime = e.getSleepTime() + Math.max(1, downloadingQueueItem.getAttempts())
                    * fileManagerProperties.getFloodWaitPenalty();
            downloadingQueueService.setWaiting(downloadingQueueItem.getId(), sleepTime, e);
        }
    }
}
