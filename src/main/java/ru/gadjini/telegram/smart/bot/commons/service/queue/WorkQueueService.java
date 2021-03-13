package ru.gadjini.telegram.smart.bot.commons.service.queue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.dao.QueueDao;
import ru.gadjini.telegram.smart.bot.commons.dao.WorkQueueDao;
import ru.gadjini.telegram.smart.bot.commons.domain.QueueItem;
import ru.gadjini.telegram.smart.bot.commons.service.concurrent.SmartExecutorService;

import java.util.List;

@Service
public class WorkQueueService extends QueueService {

    private final WorkQueueDao queueDao;

    @Autowired
    public WorkQueueService(WorkQueueDao workQueueDao) {
        this.queueDao = workQueueDao;
    }

    public List<QueueItem> poll(SmartExecutorService.JobWeight weight, int limit) {
        return queueDao.poll(weight, limit);
    }

    public QueueItem getById(int id) {
        return queueDao.getById(id);
    }

    public List<QueueItem> deleteAndGetProcessingOrWaitingByUserId(int userId) {
        return queueDao.deleteAndGetProcessingOrWaitingByUserId(userId);
    }

    public QueueItem deleteAndGetProcessingOrWaitingById(int id) {
        return queueDao.deleteAndGetProcessingOrWaitingById(id);
    }

    public QueueItem deleteAndGetById(int id) {
        return queueDao.deleteAndGetById(id);
    }

    public List<QueueItem> deleteCompleted() {
        return queueDao.deleteCompleted();
    }

    public long countReadToComplete(SmartExecutorService.JobWeight weight) {
        return queueDao.countReadToComplete(weight);
    }

    public long countProcessing(SmartExecutorService.JobWeight weight) {
        return queueDao.countProcessing(weight);
    }

    @Override
    public QueueDao getQueueDao() {
        return queueDao;
    }
}
