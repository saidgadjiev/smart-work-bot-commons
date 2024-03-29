package ru.gadjini.telegram.smart.bot.commons.job;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.common.SmartWorkProfiles;
import ru.gadjini.telegram.smart.bot.commons.dao.WorkQueueDao;
import ru.gadjini.telegram.smart.bot.commons.domain.DownloadQueueItem;
import ru.gadjini.telegram.smart.bot.commons.service.queue.DownloadSynchronizerService;
import ru.gadjini.telegram.smart.bot.commons.service.queue.WorkQueueService;

import javax.annotation.PostConstruct;
import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Profile({SmartWorkProfiles.PROFILE_PROD_SECONDARY, SmartWorkProfiles.PROFILE_DEV_SECONDARY})
public class DownloadSynchronizerJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadSynchronizerJob.class);

    private DownloadSynchronizerService downloadSynchronizerService;

    private WorkQueueService workQueueService;

    @Value("${download.synchronizer.logging:false}")
    private boolean enableLogging;

    @Autowired
    public DownloadSynchronizerJob(DownloadSynchronizerService downloadSynchronizerService,
                                   WorkQueueService queueService) {
        this.downloadSynchronizerService = downloadSynchronizerService;
        this.workQueueService = queueService;
        LOGGER.debug("DownloadSynchronizerJob initialized");
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
        List<DownloadQueueItem> unsynchronizedDownloads = downloadSynchronizerService.getUnsynchronizedDownloads(producer);

        if (enableLogging) {
            LOGGER.debug("Synchronize items count({})", unsynchronizedDownloads.size());
        }
        for (DownloadQueueItem unsynchronizedDownload : unsynchronizedDownloads) {
            if (enableLogging) {
                LOGGER.debug("Start synchronize({}, {})", unsynchronizedDownload.getId(), unsynchronizedDownload.getFilePath());
            }
            try {
                synchronize(unsynchronizedDownload);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

        if (enableLogging) {
            LOGGER.debug("Finish synchronize({})", LocalDateTime.now());
        }
    }

    private void synchronize(DownloadQueueItem downloadQueueItem) {
        if (isFullySynchronized(downloadQueueItem)) {
            downloadSynchronizerService.synchronize(downloadQueueItem.getId());
            LOGGER.debug("Download synchronized({})", downloadQueueItem.getId());
        }
    }

    private boolean isFullySynchronized(DownloadQueueItem downloadQueueItem) {
        if (StringUtils.isBlank(downloadQueueItem.getFilePath())) {
            return true;
        }
        File file = new File(downloadQueueItem.getFilePath());
        boolean synced = file.exists() && (file.length() == downloadQueueItem.getFile().getSize()
                || downloadQueueItem.getFile().getSize() == 0); //May be on old thumb

        if (enableLogging) {
            LOGGER.debug("File not found or size is less({}, {}, {}, {})", downloadQueueItem.getId(),
                    file.length(), downloadQueueItem.getFile().getSize(), file.getAbsolutePath());
        }

        return synced;
    }
}
