package ru.gadjini.telegram.smart.bot.commons.service.queue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.common.SmartWorkProfiles;
import ru.gadjini.telegram.smart.bot.commons.dao.DownloadSynchronizerDao;
import ru.gadjini.telegram.smart.bot.commons.domain.DownloadQueueItem;
import ru.gadjini.telegram.smart.bot.commons.property.ServerProperties;

import java.util.List;

@Service
@Profile({SmartWorkProfiles.PROFILE_PROD_SECONDARY, SmartWorkProfiles.PROFILE_DEV_SECONDARY})
public class DownloadSynchronizerService {

    private DownloadSynchronizerDao downloadSynchronizerDao;

    private ServerProperties serverProperties;

    @Autowired
    public DownloadSynchronizerService(DownloadSynchronizerDao downloadSynchronizerDao, ServerProperties serverProperties) {
        this.downloadSynchronizerDao = downloadSynchronizerDao;
        this.serverProperties = serverProperties;
    }

    public List<DownloadQueueItem> getUnsynchronizedDownloads(String producer) {
        return downloadSynchronizerDao.getUnsynchronizedDownloads(producer, DownloadQueueItem.getSynchronizationColumn(serverProperties.getNumber()));
    }

    public void synchronize(int id) {
        downloadSynchronizerDao.synchronize(id, DownloadQueueItem.getSynchronizationColumn(serverProperties.getNumber()));
    }
}
