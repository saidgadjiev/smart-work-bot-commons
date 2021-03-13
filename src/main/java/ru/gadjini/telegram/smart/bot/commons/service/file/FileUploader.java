package ru.gadjini.telegram.smart.bot.commons.service.file;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import ru.gadjini.telegram.smart.bot.commons.domain.UploadQueueItem;
import ru.gadjini.telegram.smart.bot.commons.model.Progress;
import ru.gadjini.telegram.smart.bot.commons.model.SendFileResult;
import ru.gadjini.telegram.smart.bot.commons.model.UploadType;
import ru.gadjini.telegram.smart.bot.commons.service.flood.UploadFloodWaitController;
import ru.gadjini.telegram.smart.bot.commons.service.message.MediaMessageService;
import ru.gadjini.telegram.smart.bot.commons.service.telegram.TelegramBotApiService;

import java.util.Set;

@Service
public class FileUploader {

    private TelegramBotApiService telegramBotApiService;

    private MediaMessageService mediaMessageService;

    private UploadFloodWaitController uploadFloodWaitController;

    @Autowired
    public FileUploader(TelegramBotApiService telegramBotApiService,
                        @Qualifier("mediaLimits") MediaMessageService mediaMessageService, UploadFloodWaitController uploadFloodWaitController) {
        this.telegramBotApiService = telegramBotApiService;
        this.mediaMessageService = mediaMessageService;
        this.uploadFloodWaitController = uploadFloodWaitController;
    }

    public SendFileResult upload(UploadQueueItem uploadQueueItem) {
        String key = getFilePathOrFileId(uploadQueueItem.getMethod(), uploadQueueItem.getBody());
        uploadFloodWaitController.startUploading(key);
        applySmartOptionsToBody(uploadQueueItem);

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

    public InputFile getInputFile(String method, Object body) {
        InputFile inputFile = null;
        switch (method) {
            case SendDocument.PATH: {
                SendDocument sendDocument = (SendDocument) body;
                inputFile = sendDocument.getDocument();
                break;
            }
            case SendAudio.PATH: {
                SendAudio sendAudio = (SendAudio) body;
                inputFile = sendAudio.getAudio();
                break;
            }
            case SendVideo.PATH: {
                SendVideo sendVideo = (SendVideo) body;
                inputFile = sendVideo.getVideo();
                break;
            }
            case SendVoice.PATH: {
                SendVoice sendVoice = (SendVoice) body;
                inputFile = sendVoice.getVoice();
                break;
            }
            case SendSticker.PATH: {
                SendSticker sendSticker = (SendSticker) body;
                inputFile = sendSticker.getSticker();
                break;
            }
        }
        if (inputFile == null) {
            throw new IllegalArgumentException("Null input file " + body);
        }

        return inputFile;
    }

    private String getFilePath(String method, Object body) {
        InputFile inputFile = getInputFile(method, body);

        if (inputFile.isNew()) {
            return inputFile.getNewMediaFile().getAbsolutePath();
        }

        return null;
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
        }

        throw new IllegalArgumentException("Unsupported method to upload " + method);
    }

    private void applySmartOptionsToBody(UploadQueueItem queueItem) {
        switch (queueItem.getMethod()) {
            case SendDocument.PATH: {
                SendDocument sendDocument = (SendDocument) queueItem.getBody();
                if (Set.of(UploadType.VIDEO, UploadType.STREAMING_VIDEO).contains(queueItem.getUploadType())) {
                    SendVideo sendVideo = convert(sendDocument);
                    sendVideo.setSupportsStreaming(queueItem.getUploadType() == UploadType.STREAMING_VIDEO);
                    queueItem.setBody(sendVideo);
                    queueItem.setMethod(SendVideo.PATH);
                }
                break;
            }
            case SendVideo.PATH: {
                SendVideo sendVideo = (SendVideo) queueItem.getBody();
                if (queueItem.getUploadType() == UploadType.DOCUMENT) {
                    queueItem.setBody(convert(sendVideo));
                    queueItem.setMethod(SendDocument.PATH);
                } else if (queueItem.getUploadType() == UploadType.STREAMING_VIDEO) {
                    sendVideo.setSupportsStreaming(true);
                } else if (queueItem.getUploadType() == UploadType.VIDEO) {
                    sendVideo.setSupportsStreaming(null);
                }
                break;
            }
        }
    }

    private SendDocument convert(SendVideo sendVideo) {
        return SendDocument.builder().chatId(sendVideo.getChatId())
                .document(sendVideo.getVideo())
                .allowSendingWithoutReply(sendVideo.getAllowSendingWithoutReply())
                .caption(sendVideo.getCaption())
                .parseMode(sendVideo.getParseMode())
                .parseMode(sendVideo.getParseMode()).build();
    }

    private SendVideo convert(SendDocument sendDocument) {
        return SendVideo.builder().chatId(sendDocument.getChatId())
                .video(sendDocument.getDocument())
                .allowSendingWithoutReply(sendDocument.getAllowSendingWithoutReply())
                .caption(sendDocument.getCaption())
                .parseMode(sendDocument.getParseMode())
                .parseMode(sendDocument.getParseMode()).build();
    }
}
