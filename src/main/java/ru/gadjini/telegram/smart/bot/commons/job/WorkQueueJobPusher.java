package ru.gadjini.telegram.smart.bot.commons.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.gadjini.telegram.smart.bot.commons.domain.QueueItem;
import ru.gadjini.telegram.smart.bot.commons.service.concurrent.SmartExecutorService;
import ru.gadjini.telegram.smart.bot.commons.service.concurrent.pool.ThreadPool;

import java.util.Collection;
import java.util.List;

public abstract class WorkQueueJobPusher {

    private final Logger logger;

    public WorkQueueJobPusher() {
        logger = LoggerFactory.getLogger(getLoggerClass());
    }

    public void push() {
        if (disableJobs()) {
            if (enableJobsLogging()) {
                logger.debug("Job disabled");
            }
            return;
        }

        ThreadPool heavyExecutor = getExecutor().getExecutor(SmartExecutorService.JobWeight.HEAVY);
        if (heavyExecutor.getActiveCount() < heavyExecutor.getCorePoolSize()) {
            int limit = heavyExecutor.getCorePoolSize() - heavyExecutor.getActiveCount();
            if (enableJobsLogging()) {
                logger.debug("Heavy threads free({})", limit);
            }
            Collection<QueueItem> items = getTasks(SmartExecutorService.JobWeight.HEAVY, limit);

            if (enableJobsLogging()) {
                logger.debug("Push heavy jobs({})", items.size());
            }
            items.forEach(queueItem -> getExecutor().execute(createJob(queueItem)));
        } else if (enableJobsLogging()) {
            logger.debug("Heavy threads busy");
        }

        ThreadPool lightExecutor = getExecutor().getExecutor(SmartExecutorService.JobWeight.LIGHT);
        if (lightExecutor.getActiveCount() < lightExecutor.getCorePoolSize()) {
            int limit = lightExecutor.getCorePoolSize() - lightExecutor.getActiveCount();
            if (enableJobsLogging()) {
                logger.debug("Light threads free({})", limit);
            }
            Collection<QueueItem> items = getTasks(SmartExecutorService.JobWeight.LIGHT, limit);

            if (enableJobsLogging()) {
                logger.debug("Push light jobs({})", items.size());
            }
            items.forEach(queueItem -> getExecutor().execute(createJob(queueItem)));
        } else if (enableJobsLogging()) {
            logger.debug("Light threads busy");
        }

        if (heavyExecutor.getActiveCount() < heavyExecutor.getCorePoolSize()) {
            int limit = heavyExecutor.getCorePoolSize() - heavyExecutor.getActiveCount();
            if (enableJobsLogging()) {
                logger.debug("Heavy threads for light free({})", limit);
            }
            Collection<QueueItem> items = getTasks(SmartExecutorService.JobWeight.LIGHT, limit);

            if (enableJobsLogging()) {
                logger.debug("Push light jobs to heavy threads({})", items.size());
            }
            items.forEach(queueItem -> getExecutor().execute(createJob(queueItem), SmartExecutorService.JobWeight.HEAVY));
        } else if (enableJobsLogging()) {
            logger.debug("Heavy threads for light tasks busy");
        }
    }

    public abstract SmartExecutorService getExecutor();

    public abstract boolean enableJobsLogging();

    public abstract boolean disableJobs();

    public abstract Class<?> getLoggerClass();

    public abstract List<QueueItem> getTasks(SmartExecutorService.JobWeight weight, int limit);

    public abstract SmartExecutorService.Job createJob(QueueItem item);
}
