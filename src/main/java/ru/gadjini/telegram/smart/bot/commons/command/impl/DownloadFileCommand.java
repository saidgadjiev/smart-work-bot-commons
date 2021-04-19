package ru.gadjini.telegram.smart.bot.commons.command.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.telegram.smart.bot.commons.command.api.BotCommand;
import ru.gadjini.telegram.smart.bot.commons.model.MessageMedia;
import ru.gadjini.telegram.smart.bot.commons.service.MessageMediaService;
import ru.gadjini.telegram.smart.bot.commons.service.file.FileDownloadService;

import java.util.Locale;

@Component
public class DownloadFileCommand implements BotCommand {

    private FileDownloadService fileDownloadService;

    private MessageMediaService messageMediaService;

    @Autowired
    public DownloadFileCommand(FileDownloadService fileDownloadService, MessageMediaService messageMediaService) {
        this.fileDownloadService = fileDownloadService;
        this.messageMediaService = messageMediaService;
    }

    @Override
    public void processMessage(Message message, String[] params) {
        MessageMedia media = messageMediaService.getMedia(message, Locale.getDefault());
        fileDownloadService.createDownload(media.toTgFile(), -1, message.getFrom().getId());
    }

    @Override
    public String getCommandIdentifier() {
        return "dfile";
    }
}
