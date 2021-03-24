package ru.gadjini.telegram.smart.bot.commons.command.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.telegram.smart.bot.commons.command.api.CallbackBotCommand;
import ru.gadjini.telegram.smart.bot.commons.common.CommandNames;
import ru.gadjini.telegram.smart.bot.commons.job.WorkQueueJob;
import ru.gadjini.telegram.smart.bot.commons.request.Arg;
import ru.gadjini.telegram.smart.bot.commons.service.request.RequestParams;

@Component
public class CancelQueryCommand implements CallbackBotCommand {

    private WorkQueueJob conversionJob;

    @Autowired
    public CancelQueryCommand(WorkQueueJob conversionJob) {
        this.conversionJob = conversionJob;
    }

    @Override
    public String getName() {
        return CommandNames.CANCEL_QUERY_COMMAND_NAME;
    }

    @Override
    public void processCallbackQuery(CallbackQuery callbackQuery, RequestParams requestParams) {
        int queryItemId = requestParams.getInt(Arg.QUEUE_ITEM_ID.getKey());
        conversionJob.cancel(callbackQuery.getMessage().getChatId(), callbackQuery.getMessage().getMessageId(),
                callbackQuery.getId(), queryItemId);
    }
}
