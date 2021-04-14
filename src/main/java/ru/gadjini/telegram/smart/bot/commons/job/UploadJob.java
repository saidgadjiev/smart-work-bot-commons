package ru.gadjini.telegram.smart.bot.commons.job;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.dao.WorkQueueDao;
import ru.gadjini.telegram.smart.bot.commons.domain.QueueItem;
import ru.gadjini.telegram.smart.bot.commons.domain.UploadQueueItem;
import ru.gadjini.telegram.smart.bot.commons.exception.FloodControlException;
import ru.gadjini.telegram.smart.bot.commons.exception.FloodWaitException;
import ru.gadjini.telegram.smart.bot.commons.exception.InvalidMediaMessageException;
import ru.gadjini.telegram.smart.bot.commons.model.SendFileResult;
import ru.gadjini.telegram.smart.bot.commons.property.FileManagerProperties;
import ru.gadjini.telegram.smart.bot.commons.property.JobsProperties;
import ru.gadjini.telegram.smart.bot.commons.property.MediaLimitProperties;
import ru.gadjini.telegram.smart.bot.commons.service.concurrent.SmartExecutorService;
import ru.gadjini.telegram.smart.bot.commons.service.file.FileUploader;
import ru.gadjini.telegram.smart.bot.commons.service.queue.UploadQueueService;
import ru.gadjini.telegram.smart.bot.commons.service.queue.event.UploadCompleted;

import javax.annotation.PostConstruct;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component
@SuppressWarnings("PMD")
public class UploadJob extends WorkQueueJobPusher {

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadJob.class);

    private UploadQueueService uploadQueueService;

    private FileManagerProperties fileManagerProperties;

    private MediaLimitProperties mediaLimitProperties;

    private WorkQueueDao workQueueDao;

    private FileUploader fileUploader;

    private SmartExecutorService uploadTasksExecutor;

    private ApplicationEventPublisher applicationEventPublisher;

    private final List<UploadQueueItem> currentUploads = new ArrayList<>();

    private JobsProperties jobsProperties;

    @Autowired
    public UploadJob(UploadQueueService uploadQueueService,
                     FileManagerProperties fileManagerProperties,
                     MediaLimitProperties mediaLimitProperties, WorkQueueDao workQueueDao,
                     ApplicationEventPublisher applicationEventPublisher,
                     FileUploader fileUploader, JobsProperties jobsProperties) {
        this.uploadQueueService = uploadQueueService;
        this.fileManagerProperties = fileManagerProperties;
        this.mediaLimitProperties = mediaLimitProperties;
        this.workQueueDao = workQueueDao;
        this.applicationEventPublisher = applicationEventPublisher;
        this.fileUploader = fileUploader;
        this.jobsProperties = jobsProperties;
    }

    @Autowired
    public void setUploadTasksExecutor(@Qualifier("uploadTasksExecutor") SmartExecutorService uploadTasksExecutor) {
        this.uploadTasksExecutor = uploadTasksExecutor;
    }

    @PostConstruct
    public final void init() {
        LOGGER.debug("Disable jobs {}", jobsProperties.isDisable());
        LOGGER.debug("Enable jobs logging {}", jobsProperties.isEnableLogging());
        try {
            uploadQueueService.resetProcessing();
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    public void rejectTask(SmartExecutorService.Job job) {
        uploadQueueService.setWaitingAndDecrementAttempts(job.getId());
        LOGGER.debug("Rejected({})", job.getId());
    }

    public void doUploads() {
        super.push();
    }

    @Override
    public SmartExecutorService getExecutor() {
        return uploadTasksExecutor;
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
        return (List<QueueItem>) (Object) uploadQueueService.poll(workQueueDao.getProducerName(), weight, limit);
    }

    @Override
    public SmartExecutorService.Job createJob(QueueItem item) {
        return new UploadTask((UploadQueueItem) item);
    }

    public void cancelUploads(String producer, int producerId) {
        cancelUploads(producer, Set.of(producerId));
    }

    public void cancelUploads(String producer, Set<Integer> producerIds) {
        deleteUploads(producer, producerIds);
    }

    public void cancelUploadsByUserId(String producer, int userId) {
        List<UploadQueueItem> deleted = uploadQueueService.deleteAndGetProcessingOrWaitingByUserId(producer, userId);
        uploadTasksExecutor.cancel(deleted.stream().map(UploadQueueItem::getId).collect(Collectors.toList()), true);
        uploadQueueService.releaseResources(deleted);
    }

    public void deleteUploads(String producer, Set<Integer> producerIds) {
        List<UploadQueueItem> deleted = uploadQueueService.deleteByProducerIdsWithReturning(producer, producerIds);
        uploadTasksExecutor.cancel(deleted.stream().map(UploadQueueItem::getId).collect(Collectors.toList()), true);
        uploadQueueService.releaseResources(deleted);
    }

    public void cancelUploads() {
        uploadTasksExecutor.cancel(currentUploads.stream().map(UploadQueueItem::getId).collect(Collectors.toList()), false);
    }

    public final void shutdown() {
        uploadTasksExecutor.shutdown();
    }

    private class UploadTask implements SmartExecutorService.Job {

        private UploadQueueItem uploadQueueItem;

        private volatile Supplier<Boolean> checker;

        private volatile boolean canceledByUser;

        private UploadTask(UploadQueueItem uploadQueueItem) {
            this.uploadQueueItem = uploadQueueItem;
        }

        @Override
        @SuppressWarnings("PMD")
        public void execute() {
            currentUploads.add(uploadQueueItem);
            try {
                updateProgress();
                SendFileResult sendFileResult = null;
                try {
                    sendFileResult = fileUploader.upload(uploadQueueItem, getWeight().equals(SmartExecutorService.JobWeight.HEAVY));
                } catch (InvalidMediaMessageException ignore) {

                }
                uploadQueueService.setCompleted(uploadQueueItem.getId());
                uploadQueueService.releaseResources(uploadQueueItem);

                applicationEventPublisher.publishEvent(new UploadCompleted(sendFileResult, uploadQueueItem));
            } catch (Throwable e) {
                if (checker == null || !checker.get()) {
                    if (e instanceof FloodControlException) {
                        floodControlException(uploadQueueItem, (FloodControlException) e);
                    } else if (e instanceof FloodWaitException) {
                        floodWaitException(uploadQueueItem, (FloodWaitException) e);
                    } else if (shouldTryToUploadAgain(e)) {
                        noneCriticalException(uploadQueueItem, e);
                    } else {
                        uploadQueueService.setExceptionStatus(uploadQueueItem.getId(), e);
                        //Unknown exception. Release resources
                        uploadQueueService.releaseResources(uploadQueueItem);

                        throw e;
                    }
                }
            } finally {
                currentUploads.remove(uploadQueueItem);
            }
        }

        @Override
        public int getId() {
            return uploadQueueItem.getId();
        }

        @Override
        public SmartExecutorService.JobWeight getWeight() {
            return uploadQueueItem.getFileSize() > mediaLimitProperties.getLightFileMaxWeight()
                    ? SmartExecutorService.JobWeight.HEAVY : SmartExecutorService.JobWeight.LIGHT;
        }

        @Override
        public long getChatId() {
            return uploadQueueItem.getUserId();
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
            fileUploader.cancelUploading(uploadQueueItem.getMethod(), uploadQueueItem.getBody());
            if (canceledByUser) {
                uploadQueueService.deleteById(uploadQueueItem.getId());
                uploadQueueService.releaseResources(uploadQueueItem);
                LOGGER.debug("Canceled upload({}, {}, {})", uploadQueueItem.getMethod(), uploadQueueItem.getProducerTable(), uploadQueueItem.getProducerId());
            }
        }

        private void updateProgress() {
            if (uploadQueueItem.getAttempts() != 1) {
                uploadQueueItem.setProgress(null);
            }
        }

        public boolean shouldTryToUploadAgain(Throwable ex) {
            int socketException = ExceptionUtils.indexOfThrowable(ex, SocketException.class);
            int floodWaitExceptionIndexOf = ExceptionUtils.indexOfThrowable(ex, FloodWaitException.class);

            return socketException != -1 || floodWaitExceptionIndexOf != -1;
        }

        private void noneCriticalException(UploadQueueItem uploadQueueItem, Throwable e) {
            uploadQueueService.setWaitingAndDecrementAttempts(uploadQueueItem.getId(), fileManagerProperties.getSleepTimeBeforeUploadAttempt(), e);
        }

        private void floodControlException(UploadQueueItem uploadQueueItem, FloodControlException e) {
            uploadQueueService.setWaitingAndDecrementAttempts(uploadQueueItem.getId(), e.getSleepTime(), e);
        }

        private void floodWaitException(UploadQueueItem uploadQueueItem, FloodWaitException e) {
            uploadQueueService.setWaiting(uploadQueueItem.getId(), e.getSleepTime(), e);
        }
    }
}
