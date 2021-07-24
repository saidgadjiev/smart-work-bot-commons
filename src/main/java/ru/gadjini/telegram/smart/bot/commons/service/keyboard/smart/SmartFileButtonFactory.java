package ru.gadjini.telegram.smart.bot.commons.service.keyboard.smart;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.gadjini.telegram.smart.bot.commons.command.impl.CallbackDelegate;
import ru.gadjini.telegram.smart.bot.commons.command.impl.smart.file.state.SmartFileStateName;
import ru.gadjini.telegram.smart.bot.commons.common.CommandNames;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.common.SmartFileArg;
import ru.gadjini.telegram.smart.bot.commons.common.SmartWorkCommandNames;
import ru.gadjini.telegram.smart.bot.commons.model.UploadType;
import ru.gadjini.telegram.smart.bot.commons.request.Arg;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.command.CommandParser;
import ru.gadjini.telegram.smart.bot.commons.service.request.RequestParams;

import java.util.Locale;

@Service
public class SmartFileButtonFactory {

    private LocalisationService localisationService;

    @Autowired
    public SmartFileButtonFactory(LocalisationService localisationService) {
        this.localisationService = localisationService;
    }

    public InlineKeyboardButton goBackButton(int uploadId, Locale locale) {
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton(
                localisationService.getMessage(MessagesProperties.GO_BACK_COMMAND_NAME, locale));

        inlineKeyboardButton.setCallbackData(CommandNames.CALLBACK_DELEGATE_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(CallbackDelegate.ARG_NAME, SmartWorkCommandNames.SMART_FILE_COMMAND)
                        .add(Arg.QUEUE_ITEM_ID.getKey(), uploadId)
                        .add(SmartFileArg.GO_BACK.getKey(), true)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return inlineKeyboardButton;
    }

    public InlineKeyboardButton captionButton(int uploadId, Locale locale) {
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton(
                localisationService.getMessage(MessagesProperties.CAPTION_COMMAND_DESCRIPTION, locale));
        inlineKeyboardButton.setCallbackData(CommandNames.CALLBACK_DELEGATE_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(CallbackDelegate.ARG_NAME, SmartWorkCommandNames.SMART_FILE_COMMAND)
                        .add(Arg.QUEUE_ITEM_ID.getKey(), uploadId)
                        .add(SmartFileArg.STATE.getKey(), SmartFileStateName.CAPTION.name())
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return inlineKeyboardButton;
    }


    public InlineKeyboardButton thumbButton(int uploadId, Locale locale) {
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton(
                localisationService.getMessage(MessagesProperties.THUMB_COMMAND_DESCRIPTION, locale));
        inlineKeyboardButton.setCallbackData(CommandNames.CALLBACK_DELEGATE_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(CallbackDelegate.ARG_NAME, SmartWorkCommandNames.SMART_FILE_COMMAND)
                        .add(Arg.QUEUE_ITEM_ID.getKey(), uploadId)
                        .add(SmartFileArg.STATE.getKey(), SmartFileStateName.THUMB.name())
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return inlineKeyboardButton;
    }

    public InlineKeyboardButton doSmartUpload(int uploadId, Locale locale) {
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.DO_SMART_UPLOAD_COMMAND_DESCRIPTION, locale));
        inlineKeyboardButton.setCallbackData(CommandNames.GET_SMART_FILE + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.QUEUE_ITEM_ID.getKey(), uploadId)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return inlineKeyboardButton;
    }

    public InlineKeyboardButton streamingVideo(int uploadId, Locale locale) {
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.VIDEO_SUPPORTS_STREAMING_COMMAND_DESCRIPTION, locale));
        inlineKeyboardButton.setCallbackData(SmartWorkCommandNames.SMART_FILE_COMMAND + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.UPLOAD_TYPE.getKey(), UploadType.STREAMING_VIDEO.name())
                        .add(Arg.QUEUE_ITEM_ID.getKey(), uploadId)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return inlineKeyboardButton;
    }

    public InlineKeyboardButton document(int uploadId, Locale locale) {
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.DOCUMENT_COMMAND_DESCRIPTION, locale));
        inlineKeyboardButton.setCallbackData(SmartWorkCommandNames.SMART_FILE_COMMAND + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.UPLOAD_TYPE.getKey(), UploadType.DOCUMENT.name())
                        .add(Arg.QUEUE_ITEM_ID.getKey(), uploadId)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return inlineKeyboardButton;
    }

    public InlineKeyboardButton video(int uploadId, Locale locale) {
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.VIDEO_COMMAND_DESCRIPTION, locale));
        inlineKeyboardButton.setCallbackData(SmartWorkCommandNames.SMART_FILE_COMMAND + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.UPLOAD_TYPE.getKey(), UploadType.VIDEO.name())
                        .add(Arg.QUEUE_ITEM_ID.getKey(), uploadId)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return inlineKeyboardButton;
    }
}
