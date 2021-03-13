package ru.gadjini.telegram.smart.bot.commons.command.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.telegram.smart.bot.commons.command.api.BotCommand;
import ru.gadjini.telegram.smart.bot.commons.common.CommandNames;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MediaMessageService;

@Component
public class GetFileCommand implements BotCommand {

    private MediaMessageService mediaMessageService;

    private UserService userService;

    @Autowired
    public GetFileCommand(@Qualifier("mediaLimits") MediaMessageService messageService, UserService userService) {
        this.mediaMessageService = messageService;
        this.userService = userService;
    }

    @Override
    public boolean accept(Message message) {
        return userService.isAdmin(message.getFrom().getId());
    }

    @Override
    public void processMessage(Message message, String[] params) {
        mediaMessageService.sendFile(message.getChatId(), params[0]);
    }

    @Override
    public String getCommandIdentifier() {
        return CommandNames.GET_FILE_COMMAND;
    }
}
