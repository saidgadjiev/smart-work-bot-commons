package ru.gadjini.telegram.smart.bot.commons.service.queue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import ru.gadjini.telegram.smart.bot.commons.dao.QueueDao;
import ru.gadjini.telegram.smart.bot.commons.dao.UploadQueueDao;
import ru.gadjini.telegram.smart.bot.commons.domain.QueueItem;
import ru.gadjini.telegram.smart.bot.commons.domain.UploadQueueItem;
import ru.gadjini.telegram.smart.bot.commons.io.SmartTempFile;
import ru.gadjini.telegram.smart.bot.commons.model.Progress;
import ru.gadjini.telegram.smart.bot.commons.model.UploadType;
import ru.gadjini.telegram.smart.bot.commons.property.ServerProperties;
import ru.gadjini.telegram.smart.bot.commons.service.concurrent.SmartExecutorService;
import ru.gadjini.telegram.smart.bot.commons.service.file.FileUploader;
import ru.gadjini.telegram.smart.bot.commons.service.file.temp.TempFileService;
import ru.gadjini.telegram.smart.bot.commons.service.format.Format;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class UploadQueueService extends QueueService {

    private UploadQueueDao uploadQueueDao;

    private FileUploader fileUploader;

    private ServerProperties serverProperties;

    private TempFileService tempFileService;

    @Autowired
    public UploadQueueService(UploadQueueDao uploadQueueDao, ServerProperties serverProperties, TempFileService tempFileService) {
        this.uploadQueueDao = uploadQueueDao;
        this.serverProperties = serverProperties;
        this.tempFileService = tempFileService;
    }

    @Autowired
    public void setFileUploader(FileUploader fileUploader) {
        this.fileUploader = fileUploader;
    }

    public UploadQueueItem createUpload(int userId, String method, Object body, Format fileFormat,
                                        Progress progress, String producerTable, String producer,
                                        int producerId, QueueItem.Status status, Object extra) {
        UploadQueueItem uploadQueueItem = new UploadQueueItem();
        uploadQueueItem.setUserId(userId);
        uploadQueueItem.setMethod(method);
        uploadQueueItem.setBody(body);
        uploadQueueItem.setProducerTable(producerTable);
        uploadQueueItem.setProducer(producer);
        uploadQueueItem.setProgress(progress);
        uploadQueueItem.setProducerId(producerId);
        uploadQueueItem.setStatus(status);
        uploadQueueItem.setFileFormat(fileFormat);
        uploadQueueItem.setExtra(extra);
        uploadQueueItem.setUploadType(getUploadType(method, body, UploadType.DOCUMENT));
        uploadQueueItem.setFileSize(fileUploader.getInputFile(method, body).getNewMediaFile().length());

        if (serverProperties.isPrimaryServer()) {
            uploadQueueItem.setSynced(true);
        }

        uploadQueueDao.create(uploadQueueItem);

        return uploadQueueItem;
    }

    public List<UploadQueueItem> poll(String producer, SmartExecutorService.JobWeight jobWeight, int limit) {
        return uploadQueueDao.poll(producer, jobWeight, limit);
    }

    public void updateStatus(int id, QueueItem.Status newStatus, QueueItem.Status oldStatus) {
        uploadQueueDao.updateStatus(id, newStatus, oldStatus);
    }

    public Format getFileFormat(int id) {
        return uploadQueueDao.getFileFormat(id);
    }

    public UploadQueueItem updateUploadType(int id, UploadType uploadType) {
        return uploadQueueDao.updateUploadType(id, uploadType);
    }

    public List<UploadQueueItem> deleteByProducerIdsWithReturning(String producer, Set<Integer> producerIds) {
        return uploadQueueDao.deleteByProducerIdsWithReturning(producer, producerIds);
    }

    public List<UploadQueueItem> deleteOrphanUploads(String producer, String producerTable) {
        return uploadQueueDao.deleteOrphan(producer, producerTable);
    }

    public List<UploadQueueItem> deleteAndGetProcessingOrWaitingByUserId(String producer, int userId) {
        return uploadQueueDao.deleteAndGetProcessingOrWaitingByUserId(producer, userId);
    }

    public void setWaitingExpiredSmartUploads(long expirationInSeconds) {
        uploadQueueDao.setWaitingExpiredSmartUploads(expirationInSeconds);
    }

    public void deleteCompletedAndOrphans(String producer, String producerTable, Set<Integer> producerIds) {
        List<UploadQueueItem> deleted = new ArrayList<>(deleteByProducerIdsWithReturning(producer, producerIds));
        List<UploadQueueItem> orphanUploads = deleteOrphanUploads(producer, producerTable);
        deleted.addAll(orphanUploads);
        releaseResources(deleted);
    }

    public void releaseResources(List<UploadQueueItem> uploadQueueItems) {
        for (UploadQueueItem uploadQueueItem : uploadQueueItems) {
            releaseResources(uploadQueueItem);
        }
    }

    public void releaseResources(UploadQueueItem uploadQueueItem) {
        if (uploadQueueItem == null) {
            return;
        }
        InputFile inputFile = null;
        InputFile thumb = null;
        switch (uploadQueueItem.getMethod()) {
            case SendDocument.PATH: {
                SendDocument sendDocument = (SendDocument) uploadQueueItem.getBody();
                inputFile = sendDocument.getDocument();
                thumb = sendDocument.getThumb();
                break;
            }
            case SendAudio.PATH: {
                SendAudio sendAudio = (SendAudio) uploadQueueItem.getBody();
                inputFile = sendAudio.getAudio();
                thumb = sendAudio.getThumb();
                break;
            }
            case SendVideo.PATH: {
                SendVideo sendVideo = (SendVideo) uploadQueueItem.getBody();
                inputFile = sendVideo.getVideo();
                thumb = sendVideo.getThumb();
                break;
            }
            case SendVoice.PATH: {
                SendVoice sendVoice = (SendVoice) uploadQueueItem.getBody();
                inputFile = sendVoice.getVoice();
                break;
            }
            case SendSticker.PATH: {
                SendSticker sendSticker = (SendSticker) uploadQueueItem.getBody();
                inputFile = sendSticker.getSticker();
                break;
            }
        }

        if (inputFile != null && inputFile.isNew()) {
            tempFileService.delete(new SmartTempFile(inputFile.getNewMediaFile()));
        }
        if (thumb != null && thumb.isNew()) {
            tempFileService.delete(new SmartTempFile(thumb.getNewMediaFile()));
        }
    }

    private UploadType getUploadType(String method, Object body, UploadType defaultUploadType) {
        if (SendVideo.PATH.equals(method)) {
            SendVideo sendVideo = (SendVideo) body;

            if (sendVideo.getSupportsStreaming()) {
                return UploadType.STREAMING_VIDEO;
            }
            return UploadType.VIDEO;
        }

        return defaultUploadType;
    }

    @Override
    public QueueDao getQueueDao() {
        return uploadQueueDao;
    }
}
