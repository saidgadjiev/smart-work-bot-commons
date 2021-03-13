package ru.gadjini.telegram.smart.bot.commons.command.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.telegram.smart.bot.commons.command.api.CallbackBotCommand;
import ru.gadjini.telegram.smart.bot.commons.common.CommandNames;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.domain.QueueItem;
import ru.gadjini.telegram.smart.bot.commons.request.Arg;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.keyboard.SmartInlineKeyboardService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.bot.commons.service.queue.WorkQueueService;
import ru.gadjini.telegram.smart.bot.commons.service.request.RequestParams;
import ru.gadjini.telegram.smart.bot.commons.service.update.UpdateQueryStatusCommandMessageProvider;
import ru.gadjini.telegram.smart.bot.commons.utils.TextUtils;

import java.util.Locale;
import java.util.Objects;

@Component
public class UpdateQueryStatusCommand implements CallbackBotCommand {

    private static final int CACHE_TIME_IN_SECONDS = 30;

    private UpdateQueryStatusCommandMessageProvider messageProvider;

    private WorkQueueService queueService;

    private UserService userService;

    private MessageService messageService;

    private LocalisationService localisationService;

    private SmartInlineKeyboardService inlineKeyboardService;

    @Autowired
    public UpdateQueryStatusCommand(UpdateQueryStatusCommandMessageProvider messageProvider,
                                    WorkQueueService queueService, UserService userService,
                                    @Qualifier("messageLimits") MessageService messageService,
                                    LocalisationService localisationService, SmartInlineKeyboardService inlineKeyboardService) {
        this.messageProvider = messageProvider;
        this.queueService = queueService;
        this.userService = userService;
        this.messageService = messageService;
        this.localisationService = localisationService;
        this.inlineKeyboardService = inlineKeyboardService;
    }

    @Override
    public boolean accept(Message message) {
        return userService.isAdmin(message.getFrom().getId());
    }

    @Override
    public String getName() {
        return CommandNames.UPDATE_QUERY_STATUS;
    }

    @Override
    public void processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        int queryItemId = requestParams.getInt(Arg.QUEUE_ITEM_ID.getKey());
        Locale locale = userService.getLocaleOrDefault(callbackQuery.getFrom().getId());
        QueueItem queueItem = queueService.getById(queryItemId);
        if (queueItem == null) {
            messageService.editMessage(
                    EditMessageText.builder().chatId(String.valueOf(callbackQuery.getMessage().getChatId()))
                            .messageId(callbackQuery.getMessage().getMessageId())
                            .text(localisationService.getMessage(MessagesProperties.MESSAGE_QUERY_ITEM_NOT_FOUND, locale))
                            .build()
            );
            messageService.sendAnswerCallbackQuery(
                    AnswerCallbackQuery.builder().callbackQueryId(callbackQuery.getId())
                            .text(localisationService.getMessage(MessagesProperties.MESSAGE_QUERY_ITEM_NOT_FOUND, locale))
                            .showAlert(true)
                            .build());
        } else {
            String queuedMessage = messageProvider.getUpdateStatusMessage(queueItem, locale);
            if (!Objects.equals(TextUtils.removeHtmlTags(queuedMessage), callbackQuery.getMessage().getText())) {
                messageService.editMessage(
                        EditMessageText.builder().chatId(String.valueOf(callbackQuery.getMessage().getChatId()))
                                .messageId(callbackQuery.getMessage().getMessageId())
                                .text(queuedMessage)
                                .replyMarkup(inlineKeyboardService.getWaitingKeyboard(queryItemId, locale))
                                .build()
                );
            }
            messageService.sendAnswerCallbackQuery(
                    AnswerCallbackQuery.builder().callbackQueryId(callbackQuery.getId())
                            .cacheTime(CACHE_TIME_IN_SECONDS)
                            .text(localisationService.getMessage(MessagesProperties.UPDATED_CALLBACK_ANSWER, locale)).build());
        }
    }
}
