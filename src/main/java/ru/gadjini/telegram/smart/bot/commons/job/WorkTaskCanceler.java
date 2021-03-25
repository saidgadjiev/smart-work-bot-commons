package ru.gadjini.telegram.smart.bot.commons.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.service.concurrent.SmartExecutorService;
import ru.gadjini.telegram.smart.bot.commons.service.queue.WorkQueueService;

import java.util.Set;

@Component
public class WorkTaskCanceler {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkTaskCanceler.class);

    private SmartExecutorService executor;

    private WorkQueueService queueService;

    @Autowired
    public WorkTaskCanceler(@Qualifier("queueTaskExecutor") SmartExecutorService executor, WorkQueueService queueService) {
        this.executor = executor;
        this.queueService = queueService;
    }

    @Scheduled(fixedDelay = 60 * 1000)
    public void cancelOrphans() {
        Set<Integer> jobs = executor.getActiveTasks().keySet();
        Set<Integer> orphanProcessingItems = queueService.getOrphanItems(jobs);

        for (Integer orphanProcessingItem : orphanProcessingItems) {
            LOGGER.debug("Cancel orphan({})", orphanProcessingItem);
            executor.cancel(orphanProcessingItem, true);
        }
    }
}
