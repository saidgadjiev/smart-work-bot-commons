package ru.gadjini.telegram.smart.bot.commons.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.domain.QueueItem;
import ru.gadjini.telegram.smart.bot.commons.domain.WorkQueueItem;
import ru.gadjini.telegram.smart.bot.commons.exception.BusyWorkerException;
import ru.gadjini.telegram.smart.bot.commons.property.FileLimitProperties;
import ru.gadjini.telegram.smart.bot.commons.property.JobsProperties;
import ru.gadjini.telegram.smart.bot.commons.property.ServerProperties;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.UserTasksApi;
import ru.gadjini.telegram.smart.bot.commons.service.concurrent.SmartExecutorService;
import ru.gadjini.telegram.smart.bot.commons.service.file.FileDownloadService;
import ru.gadjini.telegram.smart.bot.commons.service.file.FileUploadService;
import ru.gadjini.telegram.smart.bot.commons.service.localisation.ErrorCode;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.bot.commons.service.queue.QueueJobConfigurator;
import ru.gadjini.telegram.smart.bot.commons.service.queue.QueueWorker;
import ru.gadjini.telegram.smart.bot.commons.service.queue.QueueWorkerFactory;
import ru.gadjini.telegram.smart.bot.commons.service.queue.WorkQueueService;
import ru.gadjini.telegram.smart.bot.commons.service.queue.event.QueueJobInitialization;
import ru.gadjini.telegram.smart.bot.commons.service.queue.event.QueueJobShuttingDown;
import ru.gadjini.telegram.smart.bot.commons.utils.MemoryUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component
public class WorkQueueJob extends WorkQueueJobPusher {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkQueueJob.class);

    private WorkQueueService workQueueService;

    private SmartExecutorService executor;

    private FileLimitProperties fileLimitProperties;

    private UserService userService;

    private MessageService messageService;

    private LocalisationService localisationService;

    private QueueWorkerFactory queueWorkerFactory;

    private ApplicationEventPublisher applicationEventPublisher;

    private QueueJobConfigurator queueJobConfigurator;

    private FileDownloadService fileDownloadService;

    private FileUploadService fileUploadService;

    private JobsProperties jobsProperties;

    private ServerProperties serverProperties;

    private UserTasksApi userTasksService;

    @Value("${disable.work.job:false}")
    public boolean disableWorkJob;

    @Autowired
    public WorkQueueJob(JobsProperties jobsProperties, ServerProperties serverProperties, UserTasksApi userTasksService) {
        this.jobsProperties = jobsProperties;
        this.serverProperties = serverProperties;
        this.userTasksService = userTasksService;
        LOGGER.debug("WorkQueueJob initialized");
    }

    @Autowired
    public void setFileUploadService(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    @Autowired
    public void setFileDownloadService(FileDownloadService fileDownloadService) {
        this.fileDownloadService = fileDownloadService;
    }

    @Autowired
    public void setQueueJobConfigurator(QueueJobConfigurator queueJobConfigurator) {
        this.queueJobConfigurator = queueJobConfigurator;
    }

    @Autowired
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Autowired
    public void setQueueWorkerFactory(QueueWorkerFactory queueWorkerFactory) {
        this.queueWorkerFactory = queueWorkerFactory;
    }

    @Autowired
    public void setLocalisationService(LocalisationService localisationService) {
        this.localisationService = localisationService;
    }

    @Autowired
    public void setWorkQueueService(WorkQueueService workQueueService) {
        this.workQueueService = workQueueService;
    }

    @Autowired
    public void setMessageService(@Qualifier("messageLimits") MessageService messageService) {
        this.messageService = messageService;
    }

    @Autowired
    public void setFileLimitProperties(FileLimitProperties fileLimitProperties) {
        this.fileLimitProperties = fileLimitProperties;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setExecutor(@Qualifier("queueTaskExecutor") SmartExecutorService executor) {
        this.executor = executor;
    }

    @PostConstruct
    public final void init() {
        LOGGER.debug("Disable jobs {}", jobsProperties.isDisable());
        LOGGER.debug("Enable jobs logging {}", jobsProperties.isEnableLogging());
        LOGGER.debug("Disable work job {}", disableWorkJob);
        applicationEventPublisher.publishEvent(new QueueJobInitialization(this));
        try {
            workQueueService.resetProcessing();
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    @Scheduled(fixedDelay = 3000)
    public final void pushJobs() {
        if (disableWorkJob) {
            return;
        }
        super.push();
    }

    @Override
    public SmartExecutorService getExecutor() {
        return executor;
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
        return workQueueService.poll(weight, limit);
    }

    @Override
    public SmartExecutorService.Job createJob(QueueItem queueItem) {
        return new QueueTask((WorkQueueItem) queueItem, queueWorkerFactory.createWorker(queueItem));
    }

    public final void rejectTask(SmartExecutorService.Job job) {
        workQueueService.setWaitingAndDecrementAttempts(job.getId());
        LOGGER.debug("Rejected({}, {})", job.getId(), job.getWeight());
    }

    public void cancelCurrentTasks(long chatId) {
        try {
            LOGGER.debug("Cancel current tasks({})", chatId);
            List<QueueItem> queueItems = workQueueService.deleteAndGetProcessingOrWaitingByUserId((int) chatId);
            if (!queueItems.isEmpty()) {
                LOGGER.debug("Cancel current tasks({}, {}, {})", chatId, queueItems.size(),
                        queueItems.stream().map(QueueItem::getId).collect(Collectors.toList()));
            }
            for (QueueItem item : queueItems) {
                if (serverProperties.isMe(item.getServer())) {
                    executor.cancel(item.getId(), true);
                } else {
                    userTasksService.cancel(item.getServer(), item.getUserId(), item.getId());
                }
            }
            fileDownloadService.cancelDownloadsByUserId((int) chatId);
            fileUploadService.cancelUploadsByUserId((int) chatId);
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void cancel(int jobId) {
        LOGGER.debug("Cancel query({})", jobId);
        QueueItem queueItem = workQueueService.deleteAndGetProcessingOrWaitingById(jobId);

        if (queueItem == null) {
            LOGGER.debug("Query item not found({})", jobId);
            return;
        }

        if (!executor.cancel(jobId, true)) {
            fileDownloadService.cancelProcessingOrWaitingDownloads(jobId);
            fileUploadService.cancelProcessingOrWaitingUploads(jobId);
        }
    }

    public void cancel(long chatId, int messageId, String queryId, int jobId) {
        LOGGER.debug("Cancel query({}, {})", chatId, jobId);

        QueueItem queueItem = workQueueService.getById(jobId);
        if (queueItem == null) {
            messageService.sendAnswerCallbackQuery(AnswerCallbackQuery.builder()
                    .callbackQueryId(queryId)
                    .text(localisationService.getMessage(MessagesProperties.MESSAGE_QUERY_ITEM_NOT_FOUND, userService.getLocaleOrDefault((int) chatId)))
                    .showAlert(true)
                    .build()
            );
        } else {
            messageService.sendAnswerCallbackQuery(AnswerCallbackQuery.builder().callbackQueryId(queryId)
                    .text(localisationService.getMessage(MessagesProperties.MESSAGE_QUERY_CANCELED, userService.getLocaleOrDefault((int) chatId)))
                    .build()
            );
            if (serverProperties.isMe(queueItem.getServer())) {
                if (!executor.cancel(jobId, true)) {
                    workQueueService.deleteByIdAndStatuses(jobId, Set.of(QueueItem.Status.WAITING, QueueItem.Status.PROCESSING));
                    fileDownloadService.cancelProcessingOrWaitingDownloads(jobId);
                    fileUploadService.cancelProcessingOrWaitingUploads(jobId);
                }
            } else if (!userTasksService.cancel(queueItem.getServer(), chatId, jobId)) {
                workQueueService.deleteByIdAndStatuses(jobId, Set.of(QueueItem.Status.WAITING, QueueItem.Status.PROCESSING));
                fileDownloadService.cancelProcessingOrWaitingDownloads(jobId);
                fileUploadService.cancelProcessingOrWaitingUploads(jobId);
            }
        }
        if (queueJobConfigurator.isNeedUpdateMessageAfterCancel(queueItem)) {
            messageService.editMessage(EditMessageText.builder()
                    .chatId(String.valueOf(chatId))
                    .messageId(messageId)
                    .text(localisationService.getMessage(MessagesProperties.MESSAGE_QUERY_CANCELED, userService.getLocaleOrDefault((int) chatId)))
                    .build(), false);
        }
    }

    public final void shutdown() {
        executor.shutdown();
        applicationEventPublisher.publishEvent(new QueueJobShuttingDown());
    }

    public class QueueTask implements SmartExecutorService.Job {

        private final WorkQueueItem queueItem;

        private volatile Supplier<Boolean> checker;

        private volatile boolean canceledByUser;

        private QueueWorker queueWorker;

        private QueueTask(WorkQueueItem queueItem, QueueWorker queueWorker) {
            this.queueItem = queueItem;
            this.queueWorker = queueWorker;
        }

        @Override
        public Integer getReplyToMessageId() {
            return queueItem.getReplyToMessageId();
        }

        @Override
        public boolean isSuppressUserExceptions() {
            return queueItem.isSuppressUserExceptions();
        }

        @Override
        public void execute() throws Exception {
            try {
                queueWorker.execute();
                workQueueService.setCompleted(queueItem.getId());
            } catch (BusyWorkerException e) {
                workQueueService.setWaiting(queueItem.getId());
            } catch (Throwable ex) {
                if (checker == null || !checker.get()) {
                    workQueueService.setExceptionStatus(queueItem.getId(), ex);
                    queueWorker.unhandledException(ex);

                    throw ex;
                }
            } finally {
                if (checker == null || !checker.get()) {
                    queueWorker.finish();
                }
            }
        }

        @Override
        public void cancel() {
            if (canceledByUser) {
                workQueueService.deleteById(queueItem.getId());
                fileDownloadService.cancelDownloads(queueItem.getId());
                fileUploadService.cancelUploads(queueItem.getId());

                LOGGER.debug("Canceled({}, {}, {})", queueItem.getUserId(), queueItem.getId(), MemoryUtils.humanReadableByteCount(queueItem.getSize()));
            }
            queueWorker.cancel(canceledByUser);
        }

        @Override
        public ErrorCode getErrorCode(Throwable e) {
            return queueWorker.getErrorCode(e);
        }

        @Override
        public int getId() {
            return queueItem.getId();
        }

        @Override
        public void setCancelChecker(Supplier<Boolean> checker) {
            this.checker = checker;
            queueWorker.setCancelChecker(checker);
        }

        @Override
        public Supplier<Boolean> getCancelChecker() {
            return checker;
        }

        @Override
        public void setCanceledByUser(boolean canceledByUser) {
            this.canceledByUser = canceledByUser;
            queueWorker.setCanceledByUser(canceledByUser);
        }

        @Override
        public boolean isCanceledByUser() {
            return canceledByUser;
        }

        @Override
        public SmartExecutorService.JobWeight getWeight() {
            return queueItem.getSize() > fileLimitProperties.getLightFileMaxWeight() ? SmartExecutorService.JobWeight.HEAVY : SmartExecutorService.JobWeight.LIGHT;
        }

        @Override
        public long getChatId() {
            return queueItem.getUserId();
        }

    }
}
