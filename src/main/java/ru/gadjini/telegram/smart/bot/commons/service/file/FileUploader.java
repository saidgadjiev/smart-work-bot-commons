package ru.gadjini.telegram.smart.bot.commons.service.file;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import ru.gadjini.telegram.smart.bot.commons.command.impl.smart.file.state.SmartFileCaptionState;
import ru.gadjini.telegram.smart.bot.commons.domain.TgFile;
import ru.gadjini.telegram.smart.bot.commons.domain.UploadQueueItem;
import ru.gadjini.telegram.smart.bot.commons.io.SmartTempFile;
import ru.gadjini.telegram.smart.bot.commons.model.Progress;
import ru.gadjini.telegram.smart.bot.commons.model.SendFileResult;
import ru.gadjini.telegram.smart.bot.commons.service.file.temp.FileTarget;
import ru.gadjini.telegram.smart.bot.commons.service.file.temp.TempFileService;
import ru.gadjini.telegram.smart.bot.commons.service.flood.UploadFloodWaitController;
import ru.gadjini.telegram.smart.bot.commons.service.message.MediaMessageService;
import ru.gadjini.telegram.smart.bot.commons.service.telegram.CancelableTelegramBotApiMediaService;

import static ru.gadjini.telegram.smart.bot.commons.service.file.FileUploadUtils.getFilePath;
import static ru.gadjini.telegram.smart.bot.commons.service.file.FileUploadUtils.getInputFile;

@Service
public class FileUploader {

    private static final String TAG = "updthmb";

    private CancelableTelegramBotApiMediaService telegramBotApiService;

    private MediaMessageService mediaMessageService;

    private UploadFloodWaitController uploadFloodWaitController;

    private FileDownloader fileDownloader;

    private TempFileService tempFileService;

    @Autowired
    public FileUploader(CancelableTelegramBotApiMediaService telegramBotApiService,
                        @Qualifier("mediaLimits") MediaMessageService mediaMessageService,
                        UploadFloodWaitController uploadFloodWaitController, FileDownloader fileDownloader,
                        TempFileService tempFileService) {
        this.telegramBotApiService = telegramBotApiService;
        this.mediaMessageService = mediaMessageService;
        this.uploadFloodWaitController = uploadFloodWaitController;
        this.fileDownloader = fileDownloader;
        this.tempFileService = tempFileService;
    }

    public SendFileResult upload(UploadQueueItem uploadQueueItem, boolean withFloodControl) {
        applySmartFileFeatures(uploadQueueItem);
        if (withFloodControl) {
            return uploadWithFloodControl(uploadQueueItem);
        } else {
            return uploadWithoutFloodControl(uploadQueueItem);
        }
    }

    private SendFileResult uploadWithoutFloodControl(UploadQueueItem uploadQueueItem) {
        return doUpload(uploadQueueItem.getMethod(), uploadQueueItem.getBody(), uploadQueueItem.getProgress());
    }

    private SendFileResult uploadWithFloodControl(UploadQueueItem uploadQueueItem) {
        String key = getFilePathOrFileId(uploadQueueItem.getMethod(), uploadQueueItem.getBody());
        uploadFloodWaitController.startUploading(key);

        try {
            return doUpload(uploadQueueItem.getMethod(), uploadQueueItem.getBody(), uploadQueueItem.getProgress());
        } finally {
            uploadFloodWaitController.finishUploading(key);
        }
    }

    public void cancelUploading(String method, Object body) {
        String key = getFilePathOrFileId(method, body);
        uploadFloodWaitController.cancelUploading(key);
        String filePath = getFilePath(method, body);

        if (StringUtils.isNotBlank(filePath)) {
            telegramBotApiService.cancelUploading(filePath);
        }
    }

    private String getFilePathOrFileId(String method, Object body) {
        InputFile inputFile = getInputFile(method, body);

        if (inputFile.isNew()) {
            return inputFile.getNewMediaFile().getAbsolutePath();
        }

        return inputFile.getAttachName();
    }

    private SendFileResult doUpload(String method, Object body, Progress progress) {
        switch (method) {
            case SendDocument.PATH: {
                SendDocument sendDocument = (SendDocument) body;
                return mediaMessageService.sendDocument(sendDocument, progress);
            }
            case SendAudio.PATH: {
                SendAudio sendAudio = (SendAudio) body;
                return mediaMessageService.sendAudio(sendAudio, progress);
            }
            case SendVideo.PATH: {
                SendVideo sendVideo = (SendVideo) body;
                return mediaMessageService.sendVideo(sendVideo, progress);
            }
            case SendVoice.PATH: {
                SendVoice sendVoice = (SendVoice) body;
                return mediaMessageService.sendVoice(sendVoice, progress);
            }
            case SendSticker.PATH: {
                SendSticker sendSticker = (SendSticker) body;
                return mediaMessageService.sendSticker(sendSticker, progress);
            }
            case SendVideoNote.PATH: {
                SendVideoNote sendVideoNote = (SendVideoNote) body;
                return mediaMessageService.sendVideoNote(sendVideoNote, progress);
            }
        }

        throw new IllegalArgumentException("Unsupported method to upload " + method);
    }

    private void applySmartFileFeatures(UploadQueueItem queueItem) {
        if (StringUtils.isNotBlank(queueItem.getCustomCaption())
                && SmartFileCaptionState.REMOVE_CAPTION.equals(queueItem.getCustomCaption())) {
            FileUploadUtils.setCaption(queueItem.getMethod(), queueItem.getBody(), queueItem.getCustomCaption());
        }
        if (StringUtils.isNotBlank(queueItem.getCustomFileName())) {
            FileUploadUtils.setFileName(queueItem.getMethod(), queueItem.getBody(), queueItem.getCustomFileName());
        }
        if (queueItem.getCustomThumb() != null) {
            SmartTempFile thumb = downloadThumb(queueItem.getUserId(), queueItem.getCustomThumb());
            if (thumb != null) {
                FileUploadUtils.setThumbFile(queueItem.getMethod(), queueItem.getBody(), new InputFile(thumb.getFile()));
            }
        }
    }

    private SmartTempFile downloadThumb(long userId, TgFile file) {
        SmartTempFile result = tempFileService.createTempFile(FileTarget.DOWNLOAD, userId,
                file.getFileId(), TAG, file.getFormat().getExt());
        try {
            fileDownloader.downloadFileByFileId(file.getFileId(), file.getSize(), result, false);
        } catch (Throwable e) {
            tempFileService.delete(result);
            return null;
        }

        return result;
    }
}
