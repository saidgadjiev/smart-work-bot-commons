package ru.gadjini.telegram.smart.bot.commons.command.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.telegram.smart.bot.commons.command.api.CallbackBotCommand;
import ru.gadjini.telegram.smart.bot.commons.common.CommandNames;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.request.Arg;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.file.FileUploadService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.bot.commons.service.request.RequestParams;

import java.util.Locale;

//@Component
public class DoSmartUploadCommand implements CallbackBotCommand {

    private FileUploadService fileUploadService;

    private MessageService messageService;

    private LocalisationService localisationService;

    private UserService userService;

    @Autowired
    public DoSmartUploadCommand(FileUploadService fileUploadService, @Qualifier("messageLimits") MessageService messageService,
                                LocalisationService localisationService, UserService userService) {
        this.fileUploadService = fileUploadService;
        this.messageService = messageService;
        this.localisationService = localisationService;
        this.userService = userService;
    }

    @Override
    public String getName() {
        return CommandNames.GET_SMART_FILE;
    }

    @Override
    public void processCallbackQuery(CallbackQuery callbackQuery, RequestParams requestParams) {
        fileUploadService.uploadSmartFile(requestParams.getInt(Arg.QUEUE_ITEM_ID.getKey()));
        Locale locale = userService.getLocaleOrDefault(callbackQuery.getFrom().getId());
        messageService.sendAnswerCallbackQuery(
                AnswerCallbackQuery.builder().callbackQueryId(callbackQuery.getId())
                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_DO_SMART_UPLOAD_ANSWER, locale))
                        .showAlert(true)
                        .build()
        );
        messageService.deleteMessage(callbackQuery.getMessage().getChatId(), callbackQuery.getMessage().getMessageId());
    }
}
