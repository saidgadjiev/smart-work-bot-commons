package ru.gadjini.telegram.smart.bot.commons.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.common.Profiles;

@Component
@Profile({Profiles.PROFILE_PROD_PRIMARY, Profiles.PROFILE_DEV_PRIMARY})
public class DownloadJobExecutor {

    private DownloadJob downloadJob;

    @Autowired
    public DownloadJobExecutor(DownloadJob downloadJob) {
        this.downloadJob = downloadJob;
    }

    @Scheduled(fixedDelay = 1000)
    public void doDownloads() {
        downloadJob.doDownloads();
    }
}
