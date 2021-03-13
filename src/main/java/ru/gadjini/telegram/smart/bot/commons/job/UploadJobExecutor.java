package ru.gadjini.telegram.smart.bot.commons.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.configuration.SmartBotConfiguration;

@Component
@Profile({SmartBotConfiguration.PROFILE_DEV_PRIMARY, SmartBotConfiguration.PROFILE_PROD_PRIMARY})
public class UploadJobExecutor {

    private UploadJob uploadJob;

    @Autowired
    public UploadJobExecutor(UploadJob uploadJob) {
        this.uploadJob = uploadJob;
    }

    @Scheduled(fixedDelay = 1000)
    public void doUploads() {
        uploadJob.doUploads();
    }

}
