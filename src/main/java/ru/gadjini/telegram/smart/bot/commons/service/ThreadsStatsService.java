package ru.gadjini.telegram.smart.bot.commons.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.domain.ThreadsStats;
import ru.gadjini.telegram.smart.bot.commons.service.concurrent.SmartExecutorService;
import ru.gadjini.telegram.smart.bot.commons.service.queue.WorkQueueService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ThreadsStatsService {

    private SmartExecutorService executorService;

    private WorkQueueService workQueueService;

    @Autowired
    public ThreadsStatsService(@Qualifier("queueTaskExecutor") SmartExecutorService executorService, WorkQueueService queueService) {
        this.executorService = executorService;
        workQueueService = queueService;
    }

    public ThreadsStats threadsStats() {
        int heavyCorePoolSize = executorService.getCorePoolSize(SmartExecutorService.JobWeight.HEAVY);
        int lightCorePoolSize = executorService.getCorePoolSize(SmartExecutorService.JobWeight.LIGHT);
        int heavyActiveCount = executorService.getExecutor(SmartExecutorService.JobWeight.HEAVY).getActiveCount();
        int lightActiveCount = executorService.getExecutor(SmartExecutorService.JobWeight.LIGHT).getActiveCount();

        long processingHeavy = workQueueService.countProcessing(SmartExecutorService.JobWeight.HEAVY);
        long processingLight = workQueueService.countProcessing(SmartExecutorService.JobWeight.LIGHT);

        long readyToCompleteHeavy = workQueueService.countReadToComplete(SmartExecutorService.JobWeight.HEAVY);
        long readToCompleteLight = workQueueService.countReadToComplete(SmartExecutorService.JobWeight.LIGHT);

        Map<Integer, SmartExecutorService.Job> activeTasks = executorService.getActiveTasks();
        Map<SmartExecutorService.JobWeight, List<Integer>> processing = new LinkedHashMap<>();
        activeTasks.forEach((integer, job) -> {
            processing.putIfAbsent(job.getWeight(), new ArrayList<>());
            processing.get(job.getWeight()).add(integer);
        });

        return new ThreadsStats(heavyCorePoolSize, lightCorePoolSize, heavyActiveCount, lightActiveCount, processingHeavy,
                processingLight, readyToCompleteHeavy, readToCompleteLight, processing);
    }
}
