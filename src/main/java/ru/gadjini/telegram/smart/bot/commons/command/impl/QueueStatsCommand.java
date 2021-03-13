package ru.gadjini.telegram.smart.bot.commons.command.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.telegram.smart.bot.commons.command.api.BotCommand;
import ru.gadjini.telegram.smart.bot.commons.common.CommandNames;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.dao.QueueDao;
import ru.gadjini.telegram.smart.bot.commons.domain.QueueItem;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.bot.commons.service.queue.DownloadQueueService;
import ru.gadjini.telegram.smart.bot.commons.service.queue.UploadQueueService;
import ru.gadjini.telegram.smart.bot.commons.service.queue.WorkQueueService;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

@Component
public class QueueStatsCommand implements BotCommand {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private WorkQueueService queueService;

    private LocalisationService localisationService;

    private UserService userService;

    private MessageService messageService;

    private DownloadQueueService downloadQueueService;

    private UploadQueueService uploadQueueService;

    @Autowired
    public QueueStatsCommand(WorkQueueService queueService, LocalisationService localisationService,
                             UserService userService, @Qualifier("messageLimits") MessageService messageService,
                             DownloadQueueService downloadQueueService, UploadQueueService uploadQueueService) {
        this.queueService = queueService;
        this.localisationService = localisationService;
        this.userService = userService;
        this.messageService = messageService;
        this.downloadQueueService = downloadQueueService;
        this.uploadQueueService = uploadQueueService;
    }

    @Override
    public boolean accept(Message message) {
        return userService.isAdmin(message.getFrom().getId());
    }

    @Override
    public void processMessage(Message message, String[] params) {
        Locale locale = userService.getLocaleOrDefault(message.getFrom().getId());
        long processing = queueService.countByStatusAllTime(QueueItem.Status.PROCESSING);
        long waiting = queueService.countByStatusAllTime(QueueItem.Status.WAITING);
        long errorForToday = queueService.countByStatusForToday(QueueItem.Status.EXCEPTION);
        long errorAllTime = queueService.countByStatusAllTime(QueueItem.Status.EXCEPTION);
        long completed = queueService.countByStatusForToday(QueueItem.Status.COMPLETED);
        long activeUsers = queueService.countActiveUsersForToday();
        Map<String, Object> minLastRunAtValues = queueService.getProcessingMinLastRunAt();
        ZonedDateTime minCreatedAt = queueService.getWaitingMinCreatedAt();

        long processingDownloads = downloadQueueService.countByStatusAllTime(QueueItem.Status.PROCESSING);
        long waitingDownloads = downloadQueueService.countByStatusAllTime(QueueItem.Status.WAITING);
        long errorForTodayDownloads = downloadQueueService.countByStatusForToday(QueueItem.Status.EXCEPTION);
        long errorAllTimeDownloads = downloadQueueService.countByStatusAllTime(QueueItem.Status.EXCEPTION);
        long floodWaitsCount = downloadQueueService.floodWaitsCount();
        long completedDownloads = downloadQueueService.countByStatusForToday(QueueItem.Status.COMPLETED);
        ZonedDateTime maxNextRunAt = downloadQueueService.getWaitingMaxNextRunAt();

        long processingUploads = uploadQueueService.countByStatusAllTime(QueueItem.Status.PROCESSING);
        long waitingUploads = uploadQueueService.countByStatusAllTime(QueueItem.Status.WAITING);
        long errorForTodayUploads = uploadQueueService.countByStatusForToday(QueueItem.Status.EXCEPTION);
        long errorAllTimeUploads = uploadQueueService.countByStatusAllTime(QueueItem.Status.EXCEPTION);
        long completedUploads = uploadQueueService.countByStatusForToday(QueueItem.Status.COMPLETED);

        ZonedDateTime minLastRunAt = (ZonedDateTime) minLastRunAtValues.get(QueueDao.MIN_LAST_RUN_AT);
        Integer id = (Integer) minLastRunAtValues.get(QueueDao.MIN_LAST_RUN_AT_ID);
        String statsMessage = localisationService.getMessage(MessagesProperties.MESSAGE_QUEUE_STATS, new Object[]{
                processing, minLastRunAt == null ? "" : id + "(" + DATE_TIME_FORMATTER.format(minLastRunAt) + ")", waiting,
                minCreatedAt == null ? "" : DATE_TIME_FORMATTER.format(minCreatedAt),
                errorForToday, completed, activeUsers, errorAllTime,
                processingDownloads, waitingDownloads, maxNextRunAt == null ? "" : DATE_TIME_FORMATTER.format(maxNextRunAt),
                errorForTodayDownloads, completedDownloads, errorAllTimeDownloads, floodWaitsCount,
                processingUploads, waitingUploads, errorForTodayUploads, completedUploads, errorAllTimeUploads
        }, locale);
        messageService.sendMessage(SendMessage.builder().chatId(String.valueOf(message.getChatId()))
                .text(statsMessage).parseMode(ParseMode.HTML).build());
    }

    @Override
    public String getCommandIdentifier() {
        return CommandNames.QUEUE_STATS;
    }
}
