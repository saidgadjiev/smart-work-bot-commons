package ru.gadjini.telegram.smart.bot.commons.command.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.telegram.smart.bot.commons.annotation.TgMessageLimitsControl;
import ru.gadjini.telegram.smart.bot.commons.command.api.BotCommand;
import ru.gadjini.telegram.smart.bot.commons.command.api.NavigableBotCommand;
import ru.gadjini.telegram.smart.bot.commons.common.SmartWorkCommandNames;
import ru.gadjini.telegram.smart.bot.commons.model.MessageMedia;
import ru.gadjini.telegram.smart.bot.commons.service.MessageMediaService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.file.FileDownloadService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;

import java.util.Locale;

@Component
public class DownloadFileCommand implements BotCommand, NavigableBotCommand {

    public static final int FAKE_PRODUCER = -1;

    private FileDownloadService fileDownloadService;

    private MessageMediaService messageMediaService;

    private MessageService messageService;

    private UserService userService;

    @Autowired
    public DownloadFileCommand(FileDownloadService fileDownloadService, MessageMediaService messageMediaService,
                               @TgMessageLimitsControl MessageService messageService, UserService userService) {
        this.fileDownloadService = fileDownloadService;
        this.messageMediaService = messageMediaService;
        this.messageService = messageService;
        this.userService = userService;
    }

    @Override
    public boolean accept(Message message) {
        return userService.isAdmin(message.getFrom().getId());
    }

    @Override
    public void processMessage(Message message, String[] params) {
        messageService.sendMessage(SendMessage.builder().chatId(String.valueOf(message.getChatId()))
                .text("Hello i'm ready").build());
    }

    @Override
    public void processNonCommandUpdate(Message message, String text) {
        MessageMedia media = messageMediaService.getMedia(message, Locale.getDefault());
        fileDownloadService.createDownload(media.toTgFile(), FAKE_PRODUCER, message.getFrom().getId());

        messageService.sendMessage(SendMessage.builder().chatId(String.valueOf(message.getChatId()))
                .text("Okay i'll do it").build());
    }

    @Override
    public String getCommandIdentifier() {
        return SmartWorkCommandNames.DFILE;
    }

    @Override
    public String getParentCommandName(long l) {
        return SmartWorkCommandNames.DFILE;
    }

    @Override
    public String getHistoryName() {
        return SmartWorkCommandNames.DFILE;
    }
}
