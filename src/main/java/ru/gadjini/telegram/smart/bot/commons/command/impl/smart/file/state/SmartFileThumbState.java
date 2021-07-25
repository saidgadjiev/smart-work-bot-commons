package ru.gadjini.telegram.smart.bot.commons.command.impl.smart.file.state;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.telegram.smart.bot.commons.annotation.TgMessageLimitsControl;
import ru.gadjini.telegram.smart.bot.commons.command.impl.smart.file.SmartFileCommandState;
import ru.gadjini.telegram.smart.bot.commons.common.SmartWorkCommandNames;
import ru.gadjini.telegram.smart.bot.commons.exception.UserException;
import ru.gadjini.telegram.smart.bot.commons.model.MessageMedia;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.MessageMediaService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.command.CommandStateService;
import ru.gadjini.telegram.smart.bot.commons.service.command.navigator.CommandNavigator;
import ru.gadjini.telegram.smart.bot.commons.service.format.FormatCategory;
import ru.gadjini.telegram.smart.bot.commons.service.keyboard.smart.SmartFileInlineKeyboardService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.bot.commons.service.message.SmartWorkMessageProperties;
import ru.gadjini.telegram.smart.bot.commons.service.message.smart.SmartFileMessageBuilder;
import ru.gadjini.telegram.smart.bot.commons.service.queue.UploadQueueService;

import java.util.Locale;

@Component
public class SmartFileThumbState implements SmartFileState {

    private MessageService messageService;

    private UserService userService;

    private SmartFileInlineKeyboardService smartFileInlineKeyboardService;

    private SmartFileFatherState fatherState;

    private CommandStateService commandStateService;

    private UploadQueueService uploadQueueService;

    private MessageMediaService messageMediaService;

    private CommandNavigator commandNavigator;

    private SmartStateNonCommandUpdateHandler nonCommandUpdateHandler;

    private SmartFileMessageBuilder messageBuilder;

    private LocalisationService localisationService;

    @Autowired
    public SmartFileThumbState(@TgMessageLimitsControl MessageService messageService, UserService userService,
                               SmartFileInlineKeyboardService smartFileInlineKeyboardService,
                               CommandStateService commandStateService, UploadQueueService uploadQueueService,
                               MessageMediaService messageMediaService, SmartFileMessageBuilder messageBuilder,
                               LocalisationService localisationService) {
        this.messageService = messageService;
        this.userService = userService;
        this.smartFileInlineKeyboardService = smartFileInlineKeyboardService;
        this.commandStateService = commandStateService;
        this.uploadQueueService = uploadQueueService;
        this.messageMediaService = messageMediaService;
        this.messageBuilder = messageBuilder;
        this.localisationService = localisationService;
    }

    @Autowired
    public void setNonCommandUpdateHandler(SmartStateNonCommandUpdateHandler nonCommandUpdateHandler) {
        this.nonCommandUpdateHandler = nonCommandUpdateHandler;
    }

    @Autowired
    public void setCommandNavigator(CommandNavigator commandNavigator) {
        this.commandNavigator = commandNavigator;
    }

    @Autowired
    public void setFatherState(SmartFileFatherState fatherState) {
        this.fatherState = fatherState;
    }

    @Override
    public SmartFileStateName getName() {
        return SmartFileStateName.THUMB;
    }

    @Override
    public void enter(CallbackQuery callbackQuery, SmartFileCommandState currentState) {
        Locale locale = userService.getLocaleOrDefault(callbackQuery.getFrom().getId());
        messageService.editMessage(
                EditMessageText.builder()
                        .chatId(String.valueOf(callbackQuery.getFrom().getId()))
                        .messageId(callbackQuery.getMessage().getMessageId())
                        .text(messageBuilder.buildThumbMessage(currentState.getUploadId(), currentState.getThumb(), locale))
                        .replyMarkup(smartFileInlineKeyboardService.goBackKeyboard(callbackQuery.getMessage().getMessageId(), locale))
                        .build()
        );
        commandNavigator.push(callbackQuery.getFrom().getId(), nonCommandUpdateHandler);
    }

    @Override
    public void goBack(CallbackQuery callbackQuery, SmartFileCommandState currentState) {
        fatherState.enter(callbackQuery, currentState);
        currentState.setStateName(SmartFileStateName.FATHER);
        commandStateService.setState(callbackQuery.getFrom().getId(), SmartWorkCommandNames.SMART_FILE_COMMAND, currentState);
        commandNavigator.silentPop(callbackQuery.getFrom().getId());
    }

    @Override
    public void update(Message message, String text, SmartFileCommandState currentState) {
        Locale locale = userService.getLocaleOrDefault(message.getFrom().getId());
        MessageMedia thumbMedia = messageMediaService.getMedia(message, locale);
        if (thumbMedia != null && thumbMedia.getFormat().getCategory() == FormatCategory.IMAGES) {
            uploadQueueService.updateThumb(currentState.getUploadId(), thumbMedia.toTgFile());
            currentState.setThumb(thumbMedia.getFileId());
            currentState.setStateName(fatherState.getName());
            fatherState.restore(message.getFrom().getId(), currentState);
            commandNavigator.silentPop(message.getFrom().getId());
            commandStateService.setState(message.getFrom().getId(), SmartWorkCommandNames.SMART_FILE_COMMAND, currentState);
        } else {
            throw new UserException(localisationService.getMessage(SmartWorkMessageProperties.MESSAGE_SEND_THUMB, locale));
        }
    }
}
