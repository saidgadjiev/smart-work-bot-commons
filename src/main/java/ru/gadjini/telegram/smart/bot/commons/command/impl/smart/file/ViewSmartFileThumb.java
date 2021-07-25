package ru.gadjini.telegram.smart.bot.commons.command.impl.smart.file;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.telegram.smart.bot.commons.command.api.BotCommand;
import ru.gadjini.telegram.smart.bot.commons.common.SmartWorkCommandNames;
import ru.gadjini.telegram.smart.bot.commons.domain.FileSource;
import ru.gadjini.telegram.smart.bot.commons.domain.UploadQueueItem;
import ru.gadjini.telegram.smart.bot.commons.model.SendFileResult;
import ru.gadjini.telegram.smart.bot.commons.service.file.FileUploadUtils;
import ru.gadjini.telegram.smart.bot.commons.service.message.MediaMessageService;
import ru.gadjini.telegram.smart.bot.commons.service.queue.UploadQueueService;

@Component
public class ViewSmartFileThumb implements BotCommand {

    private UploadQueueService uploadQueueService;

    private MediaMessageService mediaMessageService;

    @Autowired
    public ViewSmartFileThumb(UploadQueueService uploadQueueService, @Qualifier("mediaLimits") MediaMessageService mediaMessageService) {
        this.uploadQueueService = uploadQueueService;
        this.mediaMessageService = mediaMessageService;
    }

    @Override
    public void processMessage(Message message, String[] strings) {
        int uploadId = Integer.parseInt(strings[0]);
        UploadQueueItem queueItem = uploadQueueService.getById(uploadId);
        if (queueItem.getCustomThumb() != null) {
            if (queueItem.getCustomThumb().getSource() == FileSource.DOCUMENT) {
                mediaMessageService.sendDocument(SendDocument.builder()
                        .chatId(String.valueOf(message.getChatId()))
                        .document(new InputFile(queueItem.getCustomThumb().getFileId()))
                        .build());
            } else if (queueItem.getCustomThumb().getSource() == FileSource.PHOTO) {
                mediaMessageService.sendPhoto(SendPhoto.builder().chatId(String.valueOf(message.getChatId()))
                        .photo(new InputFile(queueItem.getCustomThumb().getFileId()))
                        .build());
            } else {
                mediaMessageService.sendFile(message.getChatId(), queueItem.getCustomThumb().getFileId());
            }
        } else if (queueItem.getThumbFileId() != null) {
            mediaMessageService.sendPhoto(SendPhoto.builder().photo(new InputFile(queueItem.getThumbFileId()))
                    .chatId(String.valueOf(message.getChatId())).build());
        } else {
            InputFile thumbFile = FileUploadUtils.getThumbFile(queueItem.getMethod(), queueItem.getBody());

            SendFileResult sendFileResult = mediaMessageService.sendPhoto(
                    SendPhoto.builder()
                            .chatId(String.valueOf(message.getChatId()))
                            .photo(new InputFile(thumbFile.getNewMediaFile(), thumbFile.getMediaName()))
                            .build()
            );
            uploadQueueService.setThumbFileId(queueItem.getId(), sendFileResult.getFileId());
        }
    }

    @Override
    public String getCommandIdentifier() {
        return SmartWorkCommandNames.VTHUMB;
    }
}
