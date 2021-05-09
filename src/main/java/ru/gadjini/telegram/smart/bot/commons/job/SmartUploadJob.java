package ru.gadjini.telegram.smart.bot.commons.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import ru.gadjini.telegram.smart.bot.commons.service.queue.UploadQueueService;

import java.time.LocalDateTime;

public class SmartUploadJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmartUploadJob.class);

    private static final int EXPIRATION_TIME_IN_SECONDS = 30 * 60;

    private UploadQueueService uploadQueueService;

    @Autowired
    public SmartUploadJob(UploadQueueService uploadQueueService) {
        this.uploadQueueService = uploadQueueService;
    }

    @Scheduled(fixedDelay = 1000)
    public void autoSend() {
        LOGGER.debug("Start({})", LocalDateTime.now());
        uploadQueueService.setWaitingExpiredSmartUploads(EXPIRATION_TIME_IN_SECONDS);
        LOGGER.debug("Finish({})", LocalDateTime.now());
    }
}
