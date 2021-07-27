package ru.gadjini.telegram.smart.bot.commons.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.common.Profiles;
import ru.gadjini.telegram.smart.bot.commons.service.queue.UploadQueueService;

@Component
@Profile({Profiles.PROFILE_DEV_PRIMARY, Profiles.PROFILE_PROD_PRIMARY})
public class SmartUploadJob {

    private static final int EXPIRATION_TIME_IN_SECONDS = 30 * 60;

    private UploadQueueService uploadQueueService;

    @Autowired
    public SmartUploadJob(UploadQueueService uploadQueueService) {
        this.uploadQueueService = uploadQueueService;
    }

    @Scheduled(fixedDelay = 1000)
    public void autoSend() {
        uploadQueueService.setWaitingExpiredSmartUploads(EXPIRATION_TIME_IN_SECONDS);
    }
}
