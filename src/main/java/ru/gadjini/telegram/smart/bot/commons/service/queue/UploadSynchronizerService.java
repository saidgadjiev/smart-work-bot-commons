package ru.gadjini.telegram.smart.bot.commons.service.queue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.configuration.SmartBotConfiguration;
import ru.gadjini.telegram.smart.bot.commons.dao.UploadSynchronizerDao;
import ru.gadjini.telegram.smart.bot.commons.domain.UploadQueueItem;

import java.util.List;

@Service
@Profile({SmartBotConfiguration.PROFILE_PROD_PRIMARY, SmartBotConfiguration.PROFILE_DEV_PRIMARY})
public class UploadSynchronizerService {

    private UploadSynchronizerDao uploadSynchronizerDao;

    @Autowired
    public UploadSynchronizerService(UploadSynchronizerDao uploadSynchronizerDao) {
        this.uploadSynchronizerDao = uploadSynchronizerDao;
    }

    public List<UploadQueueItem> getUnsynchronizedUploads(String producer) {
        return uploadSynchronizerDao.getUnsynchronizedUploads(producer);
    }

    public void synchronize(int id) {
        uploadSynchronizerDao.synchronize(id);
    }
}
