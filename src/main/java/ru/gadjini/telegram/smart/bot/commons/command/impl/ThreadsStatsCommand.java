package ru.gadjini.telegram.smart.bot.commons.command.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.telegram.smart.bot.commons.command.api.BotCommand;
import ru.gadjini.telegram.smart.bot.commons.common.CommandNames;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.domain.ThreadsStats;
import ru.gadjini.telegram.smart.bot.commons.property.ServerProperties;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.ThreadsStatsApi;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.concurrent.SmartExecutorService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.bot.commons.service.queue.WorkQueueService;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ThreadsStatsCommand implements BotCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadsStatsCommand.class);

    private SmartExecutorService executorService;

    private LocalisationService localisationService;

    private WorkQueueService workQueueService;

    private MessageService messageService;

    private UserService userService;

    private ServerProperties serverProperties;

    private ThreadsStatsApi threadsStatsApi;

    @Autowired
    public ThreadsStatsCommand(LocalisationService localisationService, @Qualifier("messageLimits") MessageService messageService,
                               UserService userService, ServerProperties serverProperties, ThreadsStatsApi threadsStatsApi) {
        this.localisationService = localisationService;
        this.messageService = messageService;
        this.userService = userService;
        this.serverProperties = serverProperties;
        this.threadsStatsApi = threadsStatsApi;
    }

    @Override
    public boolean accept(Message message) {
        return userService.isAdmin(message.getFrom().getId());
    }

    @Autowired
    public void setWorkQueueService(WorkQueueService queueService) {
        this.workQueueService = queueService;
    }

    @Autowired
    public void setExecutorService(@Qualifier("queueTaskExecutor") SmartExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public void processMessage(Message message, String[] params) {
        int heavyCorePoolSize = executorService.getCorePoolSize(SmartExecutorService.JobWeight.HEAVY);
        int lightCorePoolSize = executorService.getCorePoolSize(SmartExecutorService.JobWeight.LIGHT);
        int heavyActiveCount = executorService.getExecutor(SmartExecutorService.JobWeight.HEAVY).getActiveCount();
        int lightActiveCount = executorService.getExecutor(SmartExecutorService.JobWeight.LIGHT).getActiveCount();

        long processingHeavy = workQueueService.countProcessing(SmartExecutorService.JobWeight.HEAVY);
        long processingLight = workQueueService.countProcessing(SmartExecutorService.JobWeight.LIGHT);

        long readyToCompleteHeavy = workQueueService.countReadToComplete(SmartExecutorService.JobWeight.HEAVY);
        long readToCompleteLight = workQueueService.countReadToComplete(SmartExecutorService.JobWeight.LIGHT);

        Map<Integer, SmartExecutorService.Job> activeTasks = executorService.getActiveTasks();
        StringBuilder activeTasksToString = new StringBuilder();
        Map<SmartExecutorService.JobWeight, List<Integer>> jobWeightListMap = new LinkedHashMap<>();
        activeTasks.forEach((integer, job) -> {
            jobWeightListMap.putIfAbsent(job.getWeight(), new ArrayList<>());
            jobWeightListMap.get(job.getWeight()).add(integer);
        });
        jobWeightListMap.forEach((jobWeight, integers) -> {
            activeTasksToString.append(jobWeight.name()).append("-").append(integers.stream().map(String::valueOf)
                    .collect(Collectors.joining(", "))).append("\n");
        });
        Locale locale = userService.getLocaleOrDefault(message.getFrom().getId());
        StringBuilder msg = new StringBuilder();
        msg.append(localisationService.getMessage(MessagesProperties.MESSAGE_WORK_THREAD_POOL_STATS, new Object[]{
                1, heavyCorePoolSize, lightCorePoolSize, heavyActiveCount, lightActiveCount,
                processingHeavy, processingLight, readyToCompleteHeavy, readToCompleteLight,
                activeTasksToString.toString().trim()
        }, locale)).append("\n").append("\n");
        for (Integer server : serverProperties.getServers().keySet()) {
            try {
                ThreadsStats threadsStats = threadsStatsApi.threadsStats(server);

                StringBuilder processingTasks = new StringBuilder();
                threadsStats.getProcessing().forEach((jobWeight, integers) -> {
                    processingTasks.append(jobWeight.name()).append("-").append(integers.stream().map(String::valueOf)
                            .collect(Collectors.joining(", "))).append("\n");
                });

                msg.append(localisationService.getMessage(MessagesProperties.MESSAGE_WORK_THREAD_POOL_STATS, new Object[]{
                        server, threadsStats.getHeavyCorePoolSize(), threadsStats.getLightCorePoolSize(), threadsStats.getHeavyActiveCount(),
                        threadsStats.getLightActiveCount(), threadsStats.getProcessingHeavy(), threadsStats.getProcessingLight(),
                        threadsStats.getReadyToCompleteHeavy(), threadsStats.getReadToCompleteLight(),
                        processingTasks.toString()
                }, locale)).append("\n").append("\n");
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                msg.append("Server ").append(server).append("\n502");
            }
        }

        messageService.sendMessage(
                SendMessage.builder().text(msg.toString())
                        .chatId(String.valueOf(message.getChatId()))
                        .parseMode(ParseMode.HTML)
                        .build()
        );
    }

    @Override
    public String getCommandIdentifier() {
        return CommandNames.TSTATS;
    }
}
