package ru.gadjini.telegram.smart.bot.commons.service.message.smart;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import ru.gadjini.telegram.smart.bot.commons.command.impl.smart.file.SmartFileCommandState;
import ru.gadjini.telegram.smart.bot.commons.command.impl.smart.file.state.SmartFileCaptionState;
import ru.gadjini.telegram.smart.bot.commons.command.impl.smart.file.state.SmartFileMessageBodyDeserializer;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.domain.UploadQueueItem;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.file.FileUploadUtils;
import ru.gadjini.telegram.smart.bot.commons.service.message.SmartWorkMessageProperties;

import java.util.Locale;

@Service
public class SmartFileMessageBuilder {

    private LocalisationService localisationService;

    private SmartFileMessageBodyDeserializer messageBodyDeserializer;

    @Autowired
    public SmartFileMessageBuilder(LocalisationService localisationService, SmartFileMessageBodyDeserializer messageBodyDeserializer) {
        this.localisationService = localisationService;
        this.messageBodyDeserializer = messageBodyDeserializer;
    }

    public String buildFileNameMessage(String fileName, Locale locale) {
        String fileNameArg;
        if (StringUtils.isBlank(fileName)) {
            fileNameArg = localisationService.getMessage(SmartWorkMessageProperties.MESSAGE_FILENAME_NOT_DEFINED, locale);
        } else {
            fileNameArg = fileName;
        }

        return localisationService.getMessage(
                MessagesProperties.MESSAGE_CURRENT_FILENAME, new Object[]{fileNameArg},
                locale
        );
    }

    public String buildCaptionMessage(String caption, Locale locale) {
        String captionArg;
        if (StringUtils.isBlank(caption) || SmartFileCaptionState.REMOVE_CAPTION.equals(caption)) {
            captionArg = localisationService.getMessage(SmartWorkMessageProperties.MESSAGE_CAPTION_NOT_DEFINED, locale);
        } else {
            captionArg = caption;
        }

        return localisationService.getMessage(
                MessagesProperties.MESSAGE_CURRENT_CAPTION, new Object[]{captionArg},
                locale
        );
    }

    public String buildThumbMessage(int uploadId, String thumb, Locale locale) {
        String thumbArg;
        if (StringUtils.isBlank(thumb)) {
            thumbArg = localisationService.getMessage(SmartWorkMessageProperties.MESSAGE_THUMB_UNSUPPORTED, locale);
        } else {
            thumbArg = localisationService.getMessage(SmartWorkMessageProperties.VIEW_THUMB_COMMAND_NAME,
                    new Object[]{String.valueOf(uploadId)}, locale);
        }

        return localisationService.getMessage(
                MessagesProperties.MESSAGE_CURRENT_THUMB, new Object[]{thumbArg},
                locale
        );
    }

    public String buildSmartUploadMessage(SmartFileCommandState state, Locale locale) {
        return buildSmartUploadMessage(state.getUploadId(), state.getFileName(), state.getCaption(), state.getThumb(),
                state.getMethod(), messageBodyDeserializer.deserialize(state.getMethod(), state.getBody()), locale);
    }

    public String buildSmartUploadMessage(UploadQueueItem queueItem, Locale locale) {
        InputFile thumbFile = FileUploadUtils.getThumbFile(queueItem.getMethod(), queueItem.getBody());
        return buildSmartUploadMessage(queueItem.getId(), FileUploadUtils.getFileName(queueItem.getMethod(), queueItem.getBody()),
                FileUploadUtils.getCaption(queueItem.getMethod(), queueItem.getBody()), thumbFile != null
                        ? thumbFile.getMediaName() : null,
                queueItem.getMethod(), queueItem.getBody(), locale);
    }

    public String buildSmartUploadMessage(int uploadId, String fileName, String caption, String thumb,
                                          String method, Object body, Locale locale) {
        String fileNameArg;
        if (FileUploadUtils.isFileNameSupported(method, body)) {
            if (StringUtils.isBlank(fileName)) {
                fileNameArg = localisationService.getMessage(SmartWorkMessageProperties.MESSAGE_FILENAME_NOT_DEFINED, locale);
            } else {
                fileNameArg = fileName;
            }
        } else {
            fileNameArg = localisationService.getMessage(SmartWorkMessageProperties.MESSAGE_FILENAME_UNSUPPORTED, locale);
        }
        String captionArg;
        if (FileUploadUtils.isFileNameSupported(method, body)) {
            if (StringUtils.isBlank(caption) || SmartFileCaptionState.REMOVE_CAPTION.equals(caption)) {
                captionArg = localisationService.getMessage(SmartWorkMessageProperties.MESSAGE_CAPTION_NOT_DEFINED, locale);
            } else {
                captionArg = caption;
            }
        } else {
            captionArg = localisationService.getMessage(SmartWorkMessageProperties.MESSAGE_CAPTION_UNSUPPORTED, locale);
        }
        String thumbArg;
        if (FileUploadUtils.isThumbSupported(method, body)) {
            if (StringUtils.isBlank(thumb)) {
                thumbArg = localisationService.getMessage(SmartWorkMessageProperties.MESSAGE_THUMB_NOT_DEFINED, locale);
            } else {
                thumbArg = localisationService.getMessage(SmartWorkMessageProperties.VIEW_THUMB_COMMAND_NAME,
                        new Object[]{String.valueOf(uploadId)}, locale);
            }
        } else {
            thumbArg = localisationService.getMessage(SmartWorkMessageProperties.MESSAGE_THUMB_UNSUPPORTED, locale);
        }

        return localisationService.getMessage(MessagesProperties.MESSAGE_SMART_UPLOAD_IS_READY, new Object[]{
                fileNameArg, captionArg, thumbArg
        }, locale);
    }
}
