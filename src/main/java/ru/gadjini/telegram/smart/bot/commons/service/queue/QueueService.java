package ru.gadjini.telegram.smart.bot.commons.service.queue;

import org.apache.commons.lang3.exception.ExceptionUtils;
import ru.gadjini.telegram.smart.bot.commons.dao.QueueDao;
import ru.gadjini.telegram.smart.bot.commons.domain.QueueItem;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Set;

public abstract class QueueService {

    public final void setExceptionStatus(int id, Throwable ex) {
        String exception = ExceptionUtils.getMessage(ex) + "\n" + ExceptionUtils.getStackTrace(ex);
        getQueueDao().setExceptionStatus(id, exception);
    }

    public final String getException(int id) {
        return getQueueDao().getException(id);
    }

    public final void setProgressMessageId(int id, int progressMessageId) {
        getQueueDao().setProgressMessageId(id, progressMessageId);
    }

    public final void setCompleted(int id) {
        getQueueDao().setCompleted(id);
    }

    public final void setException(int id, Throwable ex) {
        String exception = ExceptionUtils.getMessage(ex) + "\n" + ExceptionUtils.getStackTrace(ex);
        getQueueDao().setException(id, exception);
    }

    public final void setWaitingAndDecrementAttempts(int id) {
        getQueueDao().setWaitingAndDecrementAttempts(id);
    }

    public final void setWaiting(int id) {
        getQueueDao().setWaiting(id);
    }

    public final void setWaitingAndDecrementAttempts(int id, long seconds, Throwable reason) {
        String exception = ExceptionUtils.getMessage(reason) + "\n" + ExceptionUtils.getStackTrace(reason);
        getQueueDao().setWaitingAndDecrementAttempts(id, seconds, exception);
    }

    public final void setWaiting(int id, long seconds, Throwable reason) {
        String exception = ExceptionUtils.getMessage(reason) + "\n" + ExceptionUtils.getStackTrace(reason);
        getQueueDao().setWaiting(id, seconds, exception);
    }

    public final long countByStatusAllTime(QueueItem.Status status) {
        return getQueueDao().countByStatusAllTime(status);
    }

    public final long countByStatusForToday(QueueItem.Status status) {
        return getQueueDao().countByStatusForToday(status);
    }

    public final long countActiveUsersForToday() {
        return getQueueDao().countActiveUsersForToday();
    }

    public final void resetProcessing() {
        getQueueDao().resetProcessing();
    }

    public final void deleteByIdAndStatuses(int id, Set<QueueItem.Status> statuses) {
        getQueueDao().deleteByIdAndStatuses(id, statuses);
    }

    public final void deleteById(int id) {
        getQueueDao().deleteById(id);
    }

    public final boolean exists(int id) {
        return getQueueDao().exists(id);
    }

    public final ZonedDateTime getWaitingMaxNextRunAt() {
        return getQueueDao().getWaitingMaxNextRunAt();
    }

    public final Map<String, Object> getProcessingMinLastRunAt() {
        return getQueueDao().getProcessingMinLastRunAt();
    }

    public final ZonedDateTime getWaitingMinCreatedAt() {
        return getQueueDao().getWaitingMinCreatedAt();
    }

    public abstract QueueDao getQueueDao();
}
