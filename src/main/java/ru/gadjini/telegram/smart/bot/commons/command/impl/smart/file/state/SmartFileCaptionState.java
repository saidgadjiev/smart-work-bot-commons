package ru.gadjini.telegram.smart.bot.commons.command.impl.smart.file.state;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.telegram.smart.bot.commons.annotation.TgMessageLimitsControl;
import ru.gadjini.telegram.smart.bot.commons.command.impl.smart.file.SmartFileCommandState;
import ru.gadjini.telegram.smart.bot.commons.common.SmartFileArg;
import ru.gadjini.telegram.smart.bot.commons.common.SmartWorkCommandNames;
import ru.gadjini.telegram.smart.bot.commons.exception.UserException;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.command.CommandStateService;
import ru.gadjini.telegram.smart.bot.commons.service.command.navigator.CommandNavigator;
import ru.gadjini.telegram.smart.bot.commons.service.keyboard.smart.SmartFileInlineKeyboardService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.bot.commons.service.message.SmartWorkMessageProperties;
import ru.gadjini.telegram.smart.bot.commons.service.message.smart.SmartFileMessageBuilder;
import ru.gadjini.telegram.smart.bot.commons.service.queue.UploadQueueService;
import ru.gadjini.telegram.smart.bot.commons.service.request.RequestParams;

import java.util.Locale;

@Component
public class SmartFileCaptionState implements SmartFileState {

    public static final String REMOVE_CAPTION = "xcptn";

    private static final int MAX_LENGTH = 512;

    private MessageService messageService;

    private SmartFileInlineKeyboardService smartFileInlineKeyboardService;

    private UserService userService;

    private SmartFileFatherState fatherState;

    private CommandStateService commandStateService;

    private UploadQueueService uploadQueueService;

    private CommandNavigator commandNavigator;

    private SmartStateNonCommandUpdateHandler nonCommandUpdateHandler;

    private SmartFileMessageBuilder messageBuilder;

    private LocalisationService localisationService;

    @Autowired
    public SmartFileCaptionState(@TgMessageLimitsControl MessageService messageService,
                                 SmartFileInlineKeyboardService smartFileInlineKeyboardService, UserService userService,
                                 CommandStateService commandStateService, UploadQueueService uploadQueueService,
                                 SmartFileMessageBuilder messageBuilder, LocalisationService localisationService) {
        this.messageService = messageService;
        this.smartFileInlineKeyboardService = smartFileInlineKeyboardService;
        this.userService = userService;
        this.commandStateService = commandStateService;
        this.uploadQueueService = uploadQueueService;
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
        return SmartFileStateName.CAPTION;
    }

    @Override
    public void enter(CallbackQuery callbackQuery, SmartFileCommandState currentState) {
        updateMessage(callbackQuery.getFrom().getId(), currentState.getUploadId(),
                callbackQuery.getMessage().getMessageId(), currentState.getCaption());
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
        validate(message, text);
        updateCaption(message.getFrom().getId(), currentState, text);
    }

    @Override
    public void callbackUpdate(CallbackQuery callbackQuery, RequestParams requestParams, SmartFileCommandState currentState) {
        if (requestParams.contains(SmartFileArg.REMOVE_CAPTION.getKey())) {
            updateCaption(callbackQuery.getFrom().getId(), currentState, REMOVE_CAPTION);
        }
    }

    private void updateCaption(long userId, SmartFileCommandState currentState, String caption) {
        uploadQueueService.updateCaption(currentState.getUploadId(), caption);
        currentState.setCaption(caption);
        currentState.setStateName(SmartFileStateName.FATHER);
        fatherState.restore(userId, currentState);
        commandNavigator.silentPop(userId);
        commandStateService.setState(userId, SmartWorkCommandNames.SMART_FILE_COMMAND, currentState);
    }

    private void updateMessage(long userId, int uploadId, int messageId, String caption) {
        Locale locale = userService.getLocaleOrDefault(userId);
        messageService.editMessage(
                EditMessageText.builder()
                        .chatId(String.valueOf(userId))
                        .messageId(messageId)
                        .text(messageBuilder.buildCaptionMessage(caption, locale))
                        .replyMarkup(smartFileInlineKeyboardService.captionKeyboard(uploadId, locale))
                        .build(),
                false
        );
    }

    private void validate(Message message, String caption) {
        if (message.hasText()) {
            if (caption.length() > MAX_LENGTH) {
                throw new UserException(localisationService.getMessage(SmartWorkMessageProperties.MESSAGE_CAPTION_MAX_LENGTH,
                        new Object[]{MAX_LENGTH}, userService.getLocaleOrDefault(message.getFrom().getId())));
            }
        } else {
            throw new UserException(localisationService.getMessage(SmartWorkMessageProperties.MESSAGE_SEND_CAPTION,
                    userService.getLocaleOrDefault(message.getFrom().getId())));
        }
    }
}
