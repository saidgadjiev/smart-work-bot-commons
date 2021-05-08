package ru.gadjini.telegram.smart.bot.commons.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.common.Profiles;

@Component
@Profile({Profiles.PROFILE_DEV_PRIMARY, Profiles.PROFILE_PROD_PRIMARY})
public class UploadJobExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadJobExecutor.class);

    private UploadJob uploadJob;

    @Autowired
    public UploadJobExecutor(UploadJob uploadJob) {
        this.uploadJob = uploadJob;
        LOGGER.debug("Upload job initialized");
    }

    @Scheduled(fixedDelay = 1000)
    public void doUploads() {
        uploadJob.doUploads();
    }

}
