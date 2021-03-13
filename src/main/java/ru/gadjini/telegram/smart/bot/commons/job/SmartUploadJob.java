package ru.gadjini.telegram.smart.bot.commons.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import ru.gadjini.telegram.smart.bot.commons.property.JobsProperties;
import ru.gadjini.telegram.smart.bot.commons.service.queue.UploadQueueService;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;

public class SmartUploadJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmartUploadJob.class);

    private static final int EXPIRATION_TIME_IN_SECONDS = 30 * 60;

    private UploadQueueService uploadQueueService;

    private JobsProperties jobsProperties;

    @Autowired
    public SmartUploadJob(UploadQueueService uploadQueueService, JobsProperties jobsProperties) {
        this.uploadQueueService = uploadQueueService;
        this.jobsProperties = jobsProperties;
    }

    @PostConstruct
    public final void init() {
        LOGGER.debug("Disable jobs {}", jobsProperties.isDisable());
        LOGGER.debug("Enable jobs logging {}", jobsProperties.isDisable());
    }

    @Scheduled(fixedDelay = 1000)
    public void autoSend() {
        if (jobsProperties.isDisable()) {
            return;
        }
        if (jobsProperties.isEnableLogging()) {
            LOGGER.debug("Start({})", LocalDateTime.now());
        }
        uploadQueueService.setWaitingExpiredSmartUploads(EXPIRATION_TIME_IN_SECONDS);
        if (jobsProperties.isEnableLogging()) {
            LOGGER.debug("Start({})", LocalDateTime.now());
        }
    }
}
