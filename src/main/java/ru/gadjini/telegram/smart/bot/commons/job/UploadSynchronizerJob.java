package ru.gadjini.telegram.smart.bot.commons.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import ru.gadjini.telegram.smart.bot.commons.common.Profiles;
import ru.gadjini.telegram.smart.bot.commons.dao.WorkQueueDao;
import ru.gadjini.telegram.smart.bot.commons.domain.UploadQueueItem;
import ru.gadjini.telegram.smart.bot.commons.service.file.FileUploadUtils;
import ru.gadjini.telegram.smart.bot.commons.service.queue.UploadSynchronizerService;
import ru.gadjini.telegram.smart.bot.commons.service.queue.WorkQueueService;

import javax.annotation.PostConstruct;
import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Profile({Profiles.PROFILE_PROD_PRIMARY, Profiles.PROFILE_DEV_PRIMARY})
public class UploadSynchronizerJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadSynchronizerJob.class);

    private UploadSynchronizerService uploadSynchronizerService;

    private WorkQueueService workQueueService;

    @Value("${upload.synchronizer.logging:false}")
    private boolean enableLogging;

    @Autowired
    public UploadSynchronizerJob(UploadSynchronizerService uploadSynchronizerService,
                                 WorkQueueService queueService) {
        this.uploadSynchronizerService = uploadSynchronizerService;
        this.workQueueService = queueService;
        LOGGER.debug("UploadSynchronizerJob initialized");
    }

    @PostConstruct
    public void init() {
        LOGGER.debug("Enable logging({})", enableLogging);
    }

    @Scheduled(fixedDelay = 10 * 1000)
    public void doSynchronize() {
        if (enableLogging) {
            LOGGER.debug("Start synchronize({})", LocalDateTime.now());
        }
        String producer = ((WorkQueueDao) workQueueService.getQueueDao()).getProducerName();
        List<UploadQueueItem> unsynchronizedUploads = uploadSynchronizerService.getUnsynchronizedUploads(producer);

        if (enableLogging) {
            LOGGER.debug("Synchronize items count({})", unsynchronizedUploads.size());
        }
        for (UploadQueueItem unsynchronizedUpload : unsynchronizedUploads) {
            if (enableLogging) {
                LOGGER.debug("Start synchronize({})", unsynchronizedUpload.getId());
            }
            try {
                synchronize(unsynchronizedUpload);
            } catch (Exception e) {
                LOGGER.debug(e.getMessage(), e);
            }
        }
        if (enableLogging) {
            LOGGER.debug("Finish synchronize({})", LocalDateTime.now());
        }
    }

    private void synchronize(UploadQueueItem uploadQueueItem) {
        if (isFullySynchronized(uploadQueueItem)) {
            uploadSynchronizerService.synchronize(uploadQueueItem.getId());
            LOGGER.debug("Upload synchronized({})", uploadQueueItem.getId());
        }
    }

    private boolean isFullySynchronized(UploadQueueItem uploadQueueItem) {
        InputFile inputFile = FileUploadUtils.getInputFile(uploadQueueItem.getMethod(), uploadQueueItem.getBody());
        if (inputFile.isNew()) {
            File file = inputFile.getNewMediaFile();

            boolean synced = file.exists() && file.length() == uploadQueueItem.getFileSize();

            if (enableLogging) {
                LOGGER.debug("File not found or size is less({}, {}, {}, {})", uploadQueueItem.getId(),
                        file.length(), uploadQueueItem.getFileSize(), file.getAbsolutePath());
            }

            return synced;
        }

        return true;
    }
}
