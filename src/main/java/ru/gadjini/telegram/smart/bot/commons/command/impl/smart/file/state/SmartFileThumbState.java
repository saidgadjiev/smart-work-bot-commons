package ru.gadjini.telegram.smart.bot.commons.command.impl.smart.file.state;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.telegram.smart.bot.commons.annotation.TgMessageLimitsControl;
import ru.gadjini.telegram.smart.bot.commons.command.impl.smart.file.SmartFileCommand;
import ru.gadjini.telegram.smart.bot.commons.command.impl.smart.file.SmartFileCommandState;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.model.MessageMedia;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.MessageMediaService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.command.CommandStateService;
import ru.gadjini.telegram.smart.bot.commons.service.command.navigator.CommandNavigator;
import ru.gadjini.telegram.smart.bot.commons.service.format.FormatCategory;
import ru.gadjini.telegram.smart.bot.commons.service.keyboard.smart.SmartFileInlineKeyboardService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.bot.commons.service.queue.UploadQueueService;

import java.util.Locale;

@Component
public class SmartFileThumbState implements SmartFileState {

    private MessageService messageService;

    private UserService userService;

    private LocalisationService localisationService;

    private SmartFileInlineKeyboardService smartFileInlineKeyboardService;

    private SmartFileFatherState fatherState;

    private CommandStateService commandStateService;

    private UploadQueueService uploadQueueService;

    private MessageMediaService messageMediaService;

    private CommandNavigator commandNavigator;

    @Autowired
    public SmartFileThumbState(@TgMessageLimitsControl MessageService messageService, UserService userService,
                               LocalisationService localisationService, SmartFileInlineKeyboardService smartFileInlineKeyboardService,
                               CommandStateService commandStateService, UploadQueueService uploadQueueService, MessageMediaService messageMediaService) {
        this.messageService = messageService;
        this.userService = userService;
        this.localisationService = localisationService;
        this.smartFileInlineKeyboardService = smartFileInlineKeyboardService;
        this.commandStateService = commandStateService;
        this.uploadQueueService = uploadQueueService;
        this.messageMediaService = messageMediaService;
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
    public void enter(SmartFileCommand command, CallbackQuery callbackQuery, SmartFileCommandState currentState) {
        Locale locale = userService.getLocaleOrDefault(callbackQuery.getFrom().getId());
        updateMessage(callbackQuery.getFrom().getId(), callbackQuery.getMessage().getMessageId(), locale);
    }

    @Override
    public void goBack(SmartFileCommand command, CallbackQuery callbackQuery, SmartFileCommandState currentState) {
        fatherState.enter(command, callbackQuery, currentState);
        currentState.setStateName(SmartFileStateName.FATHER);
        commandStateService.setState(callbackQuery.getFrom().getId(), command.getName(), currentState);
        commandNavigator.silentPop(callbackQuery.getFrom().getId());
    }

    @Override
    public void update(Message message, String text, SmartFileCommandState currentState) {
        Locale locale = userService.getLocaleOrDefault(message.getFrom().getId());
        MessageMedia thumbMedia = messageMediaService.getMedia(message, locale);
        if (thumbMedia != null && thumbMedia.getFormat().getCategory() == FormatCategory.IMAGES) {
            uploadQueueService.updateThumb(currentState.getUploadId(), new InputFile(thumbMedia.getFileId()));
            currentState.setCaption(text);
            updateMessage(message.getFrom().getId(), currentState.getMessageId(), locale);
        }
    }

    private void updateMessage(long userId, int messageId, Locale locale) {
        messageService.editMessage(
                EditMessageText.builder()
                        .chatId(String.valueOf(userId))
                        .messageId(messageId)
                        .text(localisationService.getMessage(
                                MessagesProperties.MESSAGE_CURRENT_THUMB, locale
                        ))
                        .replyMarkup(smartFileInlineKeyboardService.goBackKeyboard(messageId, locale))
                        .build()
        );
    }
}
