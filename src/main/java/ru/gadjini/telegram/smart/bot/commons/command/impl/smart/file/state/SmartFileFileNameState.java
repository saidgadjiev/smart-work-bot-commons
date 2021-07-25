package ru.gadjini.telegram.smart.bot.commons.command.impl.smart.file.state;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.telegram.smart.bot.commons.annotation.TgMessageLimitsControl;
import ru.gadjini.telegram.smart.bot.commons.command.impl.smart.file.SmartFileCommandState;
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
import ru.gadjini.telegram.smart.bot.commons.service.utils.SmartFileFeatureUtils;

import java.util.Locale;

@Component
public class SmartFileFileNameState implements SmartFileState {

    private static final int MAX_LENGTH = 256;

    private MessageService messageService;

    private SmartFileInlineKeyboardService smartFileInlineKeyboardService;

    private UserService userService;

    private CommandNavigator commandNavigator;

    private SmartStateNonCommandUpdateHandler nonCommandUpdateHandler;

    private SmartFileFatherState fatherState;

    private CommandStateService commandStateService;

    private UploadQueueService uploadQueueService;

    private SmartFileMessageBuilder messageBuilder;

    private LocalisationService localisationService;

    @Autowired
    public SmartFileFileNameState(@TgMessageLimitsControl MessageService messageService,
                                  SmartFileInlineKeyboardService smartFileInlineKeyboardService,
                                  CommandStateService commandStateService, UploadQueueService uploadQueueService,
                                  SmartFileMessageBuilder messageBuilder, LocalisationService localisationService) {
        this.messageService = messageService;
        this.smartFileInlineKeyboardService = smartFileInlineKeyboardService;
        this.commandStateService = commandStateService;
        this.uploadQueueService = uploadQueueService;
        this.messageBuilder = messageBuilder;
        this.localisationService = localisationService;
    }

    @Autowired
    public void setFatherState(SmartFileFatherState fatherState) {
        this.fatherState = fatherState;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setNonCommandUpdateHandler(SmartStateNonCommandUpdateHandler nonCommandUpdateHandler) {
        this.nonCommandUpdateHandler = nonCommandUpdateHandler;
    }

    @Autowired
    public void setCommandNavigator(CommandNavigator commandNavigator) {
        this.commandNavigator = commandNavigator;
    }

    @Override
    public SmartFileStateName getName() {
        return SmartFileStateName.FILENAME;
    }

    @Override
    public void enter(CallbackQuery callbackQuery, SmartFileCommandState currentState) {
        updateMessage(callbackQuery.getFrom().getId(), callbackQuery.getMessage().getMessageId(), currentState.getFileName());
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
        String newFileName = SmartFileFeatureUtils.createNewFileName(text, FilenameUtils.getExtension(currentState.getFileName()));
        uploadQueueService.updateFileName(currentState.getUploadId(), newFileName);
        currentState.setFileName(newFileName);
        currentState.setStateName(SmartFileStateName.FATHER);
        fatherState.restore(message.getFrom().getId(), currentState);
        commandNavigator.silentPop(message.getFrom().getId());
        commandStateService.setState(message.getFrom().getId(), SmartWorkCommandNames.SMART_FILE_COMMAND, currentState);
    }

    private void updateMessage(long userId, int messageId, String fileName) {
        Locale locale = userService.getLocaleOrDefault(userId);
        messageService.editMessage(
                EditMessageText.builder()
                        .chatId(String.valueOf(userId))
                        .messageId(messageId)
                        .text(messageBuilder.buildFileNameMessage(fileName, locale))
                        .replyMarkup(smartFileInlineKeyboardService.goBackKeyboard(messageId, locale))
                        .build(),
                false
        );
    }

    private void validate(Message message, String fileName) {
        if (message.hasText()) {
            if (fileName.length() > MAX_LENGTH) {
                throw new UserException(localisationService.getMessage(SmartWorkMessageProperties.MESSAGE_FILENAME_MAX_LENGTH,
                        new Object[]{MAX_LENGTH}, userService.getLocaleOrDefault(message.getFrom().getId())));
            }
        } else {
            throw new UserException(localisationService.getMessage(SmartWorkMessageProperties.MESSAGE_SEND_FILENAME,
                    userService.getLocaleOrDefault(message.getFrom().getId())));
        }
    }
}
