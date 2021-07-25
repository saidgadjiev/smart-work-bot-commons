package ru.gadjini.telegram.smart.bot.commons.command.impl.smart.file.state;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.gadjini.telegram.smart.bot.commons.annotation.TgMessageLimitsControl;
import ru.gadjini.telegram.smart.bot.commons.command.impl.smart.file.SmartFileCommandState;
import ru.gadjini.telegram.smart.bot.commons.common.SmartFileArg;
import ru.gadjini.telegram.smart.bot.commons.common.SmartWorkCommandNames;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.command.CommandStateService;
import ru.gadjini.telegram.smart.bot.commons.service.keyboard.smart.SmartFileInlineKeyboardService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.bot.commons.service.message.smart.SmartFileMessageBuilder;
import ru.gadjini.telegram.smart.bot.commons.service.request.RequestParams;

import java.util.Locale;

@Component
public class SmartFileFatherState implements SmartFileState {

    private MessageService messageService;

    private UserService userService;

    private SmartFileInlineKeyboardService smartFileInlineKeyboardService;

    private SmartFileMessageBuilder smartUploadMessageBuilder;

    private CommandStateService commandStateService;

    private SmartFileCaptionState captionState;

    private SmartFileThumbState thumbState;

    private SmartFileFileNameState fileNameState;

    private SmartFileMessageBodyDeserializer messageBodyDeserializer;

    @Autowired
    public SmartFileFatherState(@TgMessageLimitsControl MessageService messageService, UserService userService,
                                SmartFileInlineKeyboardService smartFileInlineKeyboardService,
                                SmartFileMessageBuilder smartUploadMessageBuilder,
                                SmartFileMessageBodyDeserializer messageBodyDeserializer) {
        this.messageService = messageService;
        this.userService = userService;
        this.smartFileInlineKeyboardService = smartFileInlineKeyboardService;
        this.smartUploadMessageBuilder = smartUploadMessageBuilder;
        this.messageBodyDeserializer = messageBodyDeserializer;
    }

    @Autowired
    public void setFileNameState(SmartFileFileNameState fileNameState) {
        this.fileNameState = fileNameState;
    }

    @Autowired
    public void setThumbState(SmartFileThumbState thumbState) {
        this.thumbState = thumbState;
    }

    @Autowired
    public void setCaptionState(SmartFileCaptionState captionState) {
        this.captionState = captionState;
    }

    @Autowired
    public void setCommandStateService(CommandStateService commandStateService) {
        this.commandStateService = commandStateService;
    }

    @Override
    public SmartFileStateName getName() {
        return SmartFileStateName.FATHER;
    }

    @Override
    public void enter(CallbackQuery callbackQuery, SmartFileCommandState currentState) {
        Locale locale = userService.getLocaleOrDefault(callbackQuery.getFrom().getId());
        InlineKeyboardMarkup smartKeyboard = smartFileInlineKeyboardService.getSmartUploadKeyboard(currentState.getUploadId(),
                currentState.getMethod(),
                messageBodyDeserializer.deserialize(currentState.getMethod(), currentState.getBody()), locale);
        messageService.editMessage(
                callbackQuery.getMessage().getText(),
                callbackQuery.getMessage().getReplyMarkup(),
                EditMessageText.builder().chatId(String.valueOf(callbackQuery.getFrom().getId()))
                        .messageId(callbackQuery.getMessage().getMessageId())
                        .text(smartUploadMessageBuilder.buildSmartUploadMessage(currentState, locale))
                        .parseMode(ParseMode.HTML)
                        .replyMarkup(smartKeyboard)
                        .build()
        );
    }

    public void restore(long userId, SmartFileCommandState currentState) {
        Locale locale = userService.getLocaleOrDefault(userId);
        InlineKeyboardMarkup smartKeyboard = smartFileInlineKeyboardService.getSmartUploadKeyboard(currentState.getUploadId(),
                currentState.getMethod(), messageBodyDeserializer.deserialize(currentState.getMethod(), currentState.getBody()), locale);
        messageService.editMessage(
                EditMessageText.builder().chatId(String.valueOf(userId))
                        .messageId(currentState.getMessageId())
                        .text(smartUploadMessageBuilder.buildSmartUploadMessage(currentState, locale))
                        .parseMode(ParseMode.HTML)
                        .replyMarkup(smartKeyboard)
                        .build()
        );
    }

    @Override
    public void goBack(CallbackQuery callbackQuery, SmartFileCommandState currentState) {
        enter(callbackQuery, currentState);
        currentState.setStateName(SmartFileStateName.FATHER);
        commandStateService.setState(callbackQuery.getFrom().getId(), SmartWorkCommandNames.SMART_FILE_COMMAND, currentState);
    }

    @Override
    public void callbackUpdate(CallbackQuery callbackQuery, RequestParams requestParams, SmartFileCommandState currentState) {
        if (requestParams.contains(SmartFileArg.STATE.getKey())) {
            SmartFileStateName stateName = requestParams.get(SmartFileArg.STATE.getKey(), SmartFileStateName::valueOf);
            switch (stateName) {
                case FILENAME:
                    fileNameState.enter(callbackQuery, currentState);
                    break;
                case THUMB:
                    thumbState.enter(callbackQuery, currentState);
                    break;
                case CAPTION:
                    captionState.enter(callbackQuery, currentState);
                    break;
                default:
                    break;
            }
            currentState.setStateName(stateName);
            commandStateService.setState(callbackQuery.getFrom().getId(), SmartWorkCommandNames.SMART_FILE_COMMAND, currentState);
        }
    }
}
