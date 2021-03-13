package ru.gadjini.telegram.smart.bot.commons.service.concurrent;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.exception.ProcessException;
import ru.gadjini.telegram.smart.bot.commons.exception.UserException;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.localisation.ErrorCode;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;

import java.util.Locale;
import java.util.function.Supplier;

public class ExceptionHandlerJob implements SmartExecutorService.Job {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionHandlerJob.class);

    private MessageService messageService;

    private UserService userService;

    private LocalisationService localisationService;

    private SmartExecutorService.Job job;

    ExceptionHandlerJob(MessageService messageService, UserService userService,
                        LocalisationService localisationService, SmartExecutorService.Job job) {
        this.messageService = messageService;
        this.userService = userService;
        this.localisationService = localisationService;
        this.job = job;
    }

    @Override
    public void execute() {
        try {
            job.execute();
        } catch (Throwable e) {
            try {
                if (userService.handleBotBlockedByUser(e)) {
                    return;
                }
                if (!job.getCancelChecker().get()) {
                    Locale locale = userService.getLocaleOrDefault((int) job.getChatId());

                    if (e instanceof UserException) {
                        if (((UserException) e).isPrintLog()) {
                            LOGGER.error(e.getMessage(), e);
                        }
                        sendUserExceptionMessage(SendMessage.builder().chatId(String.valueOf(job.getChatId())).text(((UserException) e).getHumanMessage())
                                .parseMode(ParseMode.HTML)
                                .replyToMessageId(((UserException) e).getReplyToMessageId()).build());
                    } else {
                        if (e instanceof ProcessException) {
                            ErrorCode errorCode = job.getErrorCode(e);
                            LOGGER.error(e.getMessage(), e);
                            sendUserExceptionMessage(
                                    SendMessage.builder().chatId(String.valueOf(job.getChatId()))
                                            .parseMode(ParseMode.HTML)
                                            .text(localisationService.getMessage(StringUtils.defaultIfBlank(errorCode.getCode(),
                                                    MessagesProperties.MESSAGE_ERROR), errorCode.getArgs(), locale)).replyToMessageId(job.getReplyToMessageId()).build());
                        } else {
                            ErrorCode errorCode = job.getErrorCode(e);
                            LOGGER.error(e.getMessage(), e);
                            sendUserExceptionMessage(SendMessage.builder().chatId(String.valueOf(job.getChatId()))
                                    .text(localisationService.getMessage(StringUtils.defaultIfBlank(errorCode.getCode(),
                                            MessagesProperties.MESSAGE_ERROR), errorCode.getArgs(), locale))
                                    .parseMode(ParseMode.HTML).replyToMessageId(job.getReplyToMessageId())
                                    .build());
                        }
                    }
                }
            } catch (Throwable ex) {
                if (!userService.handleBotBlockedByUser(e)) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public int getId() {
        return job.getId();
    }

    @Override
    public SmartExecutorService.JobWeight getWeight() {
        return job.getWeight();
    }

    @Override
    public long getChatId() {
        return job.getChatId();
    }

    @Override
    public ErrorCode getErrorCode(Throwable e) {
        return job.getErrorCode(e);
    }

    @Override
    public void cancel() {
        job.cancel();
    }

    @Override
    public void setCancelChecker(Supplier<Boolean> checker) {
        job.setCancelChecker(checker);
    }

    @Override
    public void setCanceledByUser(boolean canceledByUser) {
        job.setCanceledByUser(canceledByUser);
    }

    private void sendUserExceptionMessage(SendMessage sendMessage) {
        if (job.isSuppressUserExceptions()) {
            return;
        }
        messageService.sendMessage(sendMessage);
    }

    public SmartExecutorService.Job getOriginalJob() {
        return job;
    }
}
