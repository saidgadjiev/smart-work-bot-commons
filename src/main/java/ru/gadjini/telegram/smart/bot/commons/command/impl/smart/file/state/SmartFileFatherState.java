package ru.gadjini.telegram.smart.bot.commons.command.impl.smart.file.state;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.gadjini.telegram.smart.bot.commons.annotation.TgMessageLimitsControl;
import ru.gadjini.telegram.smart.bot.commons.command.impl.smart.file.SmartFileCommand;
import ru.gadjini.telegram.smart.bot.commons.command.impl.smart.file.SmartFileCommandState;
import ru.gadjini.telegram.smart.bot.commons.common.SmartFileArg;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.command.CommandStateService;
import ru.gadjini.telegram.smart.bot.commons.service.command.navigator.CommandNavigator;
import ru.gadjini.telegram.smart.bot.commons.service.keyboard.smart.SmartFileInlineKeyboardService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.bot.commons.service.message.smart.SmartUploadMessageBuilder;
import ru.gadjini.telegram.smart.bot.commons.service.request.RequestParams;

import java.util.Locale;

@Component
public class SmartFileFatherState implements SmartFileState {

    private MessageService messageService;

    private UserService userService;

    private SmartFileInlineKeyboardService smartFileInlineKeyboardService;

    private SmartUploadMessageBuilder smartUploadMessageBuilder;

    private CommandStateService commandStateService;

    private SmartFileCaptionState captionState;

    private SmartFileThumbState thumbState;

    private CommandNavigator commandNavigator;

    private SmartStateNonCommandUpdateHandler nonCommandUpdateHandler;

    @Autowired
    public SmartFileFatherState(@TgMessageLimitsControl MessageService messageService, UserService userService,
                                SmartFileInlineKeyboardService smartFileInlineKeyboardService,
                                SmartUploadMessageBuilder smartUploadMessageBuilder) {
        this.messageService = messageService;
        this.userService = userService;
        this.smartFileInlineKeyboardService = smartFileInlineKeyboardService;
        this.smartUploadMessageBuilder = smartUploadMessageBuilder;
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
    public void enter(SmartFileCommand command, CallbackQuery callbackQuery, SmartFileCommandState currentState) {
        Locale locale = userService.getLocaleOrDefault(callbackQuery.getFrom().getId());
        InlineKeyboardMarkup smartKeyboard = smartFileInlineKeyboardService.getSmartUploadKeyboard(currentState.getUploadId(), locale);
        messageService.editMessage(
                EditMessageText.builder().chatId(String.valueOf(callbackQuery.getFrom().getId()))
                        .messageId(callbackQuery.getMessage().getMessageId())
                        .text(smartUploadMessageBuilder.buildSmartUploadMessage(locale))
                        .parseMode(ParseMode.HTML)
                        .replyMarkup(smartKeyboard)
                        .build()
        );
    }

    @Override
    public void goBack(SmartFileCommand command, CallbackQuery callbackQuery, SmartFileCommandState currentState) {
        enter(command, callbackQuery, currentState);
        currentState.setStateName(SmartFileStateName.FATHER);
        commandStateService.setState(callbackQuery.getFrom().getId(), command.getName(), currentState);
    }

    @Override
    public void callbackUpdate(SmartFileCommand command, CallbackQuery callbackQuery, RequestParams requestParams, SmartFileCommandState currentState) {
        if (requestParams.contains(SmartFileArg.STATE.getKey())) {
            SmartFileStateName stateName = requestParams.get(SmartFileArg.STATE.getKey(), SmartFileStateName::valueOf);
            switch (stateName) {
                case THUMB:
                    thumbState.enter(command, callbackQuery, currentState);
                    break;
                case CAPTION:
                    captionState.enter(command, callbackQuery, currentState);
                    break;
                default:
                    break;
            }
            currentState.setStateName(stateName);
            commandStateService.setState(callbackQuery.getFrom().getId(), command.getName(), currentState);
            commandNavigator.push(callbackQuery.getFrom().getId(), nonCommandUpdateHandler);
        }
    }
}
