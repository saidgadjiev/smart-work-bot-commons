package ru.gadjini.telegram.smart.bot.commons.command.impl.smart.file.state;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.telegram.smart.bot.commons.command.impl.smart.file.SmartFileCommand;
import ru.gadjini.telegram.smart.bot.commons.command.impl.smart.file.SmartFileCommandState;
import ru.gadjini.telegram.smart.bot.commons.service.request.RequestParams;

public interface SmartFileState {

    SmartFileStateName getName();

    void enter(SmartFileCommand command, CallbackQuery callbackQuery, SmartFileCommandState currentState);

    void goBack(SmartFileCommand command, CallbackQuery callbackQuery, SmartFileCommandState currentState);

    default void update(Message message, String text, SmartFileCommandState currentState) {

    }

    default void callbackUpdate(SmartFileCommand command, CallbackQuery callbackQuery,
                                RequestParams requestParams, SmartFileCommandState currentState) {

    }
}
