package ru.gadjini.telegram.smart.bot.commons.command.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.telegram.smart.bot.commons.command.api.CallbackBotCommand;
import ru.gadjini.telegram.smart.bot.commons.common.CommandNames;
import ru.gadjini.telegram.smart.bot.commons.domain.UploadQueueItem;
import ru.gadjini.telegram.smart.bot.commons.model.UploadType;
import ru.gadjini.telegram.smart.bot.commons.request.Arg;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.format.Format;
import ru.gadjini.telegram.smart.bot.commons.service.keyboard.smart.SmartUploadKeyboardService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.bot.commons.service.message.smart.SmartUploadMessageBuilder;
import ru.gadjini.telegram.smart.bot.commons.service.queue.UploadQueueService;
import ru.gadjini.telegram.smart.bot.commons.service.request.RequestParams;
import ru.gadjini.telegram.smart.bot.commons.service.smart.UploadTypeValidator;

import java.util.Locale;

//@Component
public class UploadTypeCommand implements CallbackBotCommand {

    private UploadQueueService uploadQueueService;

    private MessageService messageService;

    private SmartUploadKeyboardService smartUploadKeyboardService;

    private SmartUploadMessageBuilder smartUploadMessageBuilder;

    private UserService userService;

    private UploadTypeValidator uploadTypeValidator;

    @Autowired
    public UploadTypeCommand(UploadQueueService uploadQueueService, @Qualifier("messageLimits") MessageService messageService,
                             SmartUploadKeyboardService smartUploadKeyboardService,
                             SmartUploadMessageBuilder smartUploadMessageBuilder,
                             UserService userService, UploadTypeValidator uploadTypeValidator) {
        this.uploadQueueService = uploadQueueService;
        this.messageService = messageService;
        this.smartUploadKeyboardService = smartUploadKeyboardService;
        this.smartUploadMessageBuilder = smartUploadMessageBuilder;
        this.userService = userService;
        this.uploadTypeValidator = uploadTypeValidator;
    }

    @Override
    public String getName() {
        return CommandNames.UPLOAD_TYPE_COMMAND;
    }

    @Override
    public void processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        if (requestParams.contains(Arg.UPLOAD_TYPE.getKey())) {
            int uploadId = requestParams.getInt(Arg.QUEUE_ITEM_ID.getKey());
            UploadType uploadType = requestParams.get(Arg.UPLOAD_TYPE.getKey(), UploadType::valueOf);
            Format fileFormat = uploadQueueService.getFileFormat(uploadId);
            Locale locale = userService.getLocaleOrDefault(callbackQuery.getFrom().getId());

            if (uploadTypeValidator.validate(callbackQuery.getId(), fileFormat, uploadType, locale)) {
                UploadQueueItem uploadQueueItem = uploadQueueService.updateUploadType(uploadId, uploadType);
                messageService.editMessage(
                        EditMessageText.builder().chatId(String.valueOf(callbackQuery.getMessage().getChatId()))
                                .messageId(callbackQuery.getMessage().getMessageId())
                                .text(smartUploadMessageBuilder.buildSmartUploadMessage(uploadQueueItem, locale))
                                .replyMarkup(smartUploadKeyboardService.getSmartUploadKeyboard(uploadId, locale))
                                .build()
                );
            }
        }
    }
}
