package ru.gadjini.telegram.smart.bot.commons.service.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.localisation.ErrorCode;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Supplier;

public class SmartExecutorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmartExecutorService.class);

    private MessageService messageService;

    private LocalisationService localisationService;

    private UserService userService;

    private Map<JobWeight, ThreadPoolExecutor> executors;

    private final Map<Integer, Future<?>> processing = new ConcurrentHashMap<>();

    private final Map<Integer, Job> activeTasks = new ConcurrentHashMap<>();

    public SmartExecutorService(MessageService messageService, LocalisationService localisationService, UserService userService) {
        this.messageService = messageService;
        this.localisationService = localisationService;
        this.userService = userService;
    }

    public void setExecutors(Map<JobWeight, ThreadPoolExecutor> executors) {
        this.executors = executors;
    }

    public int getCorePoolSize(JobWeight weight) {
        return executors.get(weight).getCorePoolSize();
    }

    public void execute(Job job, JobWeight jobWeight) {
        ExceptionHandlerJob toExecute = new ExceptionHandlerJob(messageService, userService, localisationService, job);
        try {
            Future<Job> submit = executors.get(jobWeight).submit(new SmartCallable(toExecute));
            job.setCancelChecker(submit::isCancelled);
            processing.put(job.getId(), submit);
            activeTasks.put(job.getId(), job);
        } catch (RejectedExecutionException e) {
            LOGGER.debug("Rejected({}, {}, {})" + "\n" + e.getMessage(), job.getId(), job.getWeight(), jobWeight);
        }
    }

    public void execute(Job job) {
        execute(job, job.getWeight());
    }

    public void complete(Runnable job) {
        Job smartJob = getJobFromFutureTaskResult(job);
        if (smartJob != null) {
            complete(smartJob.getId());
        }
    }

    public void setRejectJobHandler(JobWeight weight, RejectJobHandler rejectJobHandler) {
        executors.get(weight).setRejectedExecutionHandler((r, executor) -> {
            Job job = getJobFromFutureTask(r);
            rejectJobHandler.reject(job);

            throw new RejectedExecutionException("Task " + r.toString() +
                    " rejected from " +
                    executor.toString());
        });
    }

    public boolean cancel(int jobId, boolean userOriginated) {
        try {
            Future<?> future = processing.get(jobId);
            if (future != null && (!future.isCancelled() || !future.isDone())) {
                Job job = activeTasks.get(jobId);
                job.setCanceledByUser(userOriginated);
                future.cancel(true);
                job.cancel();

                return true;
            }

            return false;
        } finally {
            complete(jobId);
        }
    }

    public void cancel(List<Integer> ids, boolean userOriginated) {
        ids.forEach(jobId -> cancel(jobId, userOriginated));
    }

    public ThreadPoolExecutor getExecutor(JobWeight jobWeight) {
        return executors.get(jobWeight);
    }

    public Map<Integer, Job> getActiveTasks() {
        return activeTasks;
    }

    public void shutdown() {
        try {
            for (Map.Entry<JobWeight, ThreadPoolExecutor> entry : executors.entrySet()) {
                entry.getValue().shutdown();
            }
            Set<Integer> jobs = new HashSet<>(processing.keySet());
            for (Integer job : jobs) {
                cancel(job, false);
            }
            for (Map.Entry<JobWeight, ThreadPoolExecutor> entry : executors.entrySet()) {
                if (!entry.getValue().awaitTermination(10, TimeUnit.SECONDS)) {
                    entry.getValue().shutdownNow();
                }
            }
        } catch (Throwable ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    private void complete(int jobId) {
        processing.remove(jobId);
        activeTasks.remove(jobId);
    }

    private SmartExecutorService.Job getJobFromFutureTaskResult(Runnable runnable) {
        try {
            Field field = runnable.getClass().getDeclaredField("outcome");
            field.setAccessible(true);

            return (Job) field.get(runnable);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private SmartExecutorService.Job getJobFromFutureTask(Runnable runnable) {
        try {
            Field field = runnable.getClass().getDeclaredField("callable");
            field.setAccessible(true);
            SmartCallable smartCallable = (SmartCallable) field.get(runnable);

            return smartCallable.job;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public interface Job {

        void execute() throws Exception;

        int getId();

        JobWeight getWeight();

        long getChatId();

        default Integer getReplyToMessageId() {
            return null;
        }

        default boolean isSuppressUserExceptions() {
            return false;
        }

        default ErrorCode getErrorCode(Throwable e) {
            return ErrorCode.EMPTY;
        }

        default void cancel() {

        }

        default void setCancelChecker(Supplier<Boolean> checker) {

        }

        default Supplier<Boolean> getCancelChecker() {
            return () -> false;
        }

        default void setCanceledByUser(boolean canceledByUser) {

        }

        default boolean isCanceledByUser() {
            return false;
        }
    }

    public interface RejectJobHandler {

        void reject(Job job);
    }

    public enum JobWeight {

        LIGHT,

        HEAVY
    }

    private static class SmartCallable implements Callable<Job> {

        private Job job;

        private SmartCallable(Job job) {
            this.job = job;
        }

        @Override
        public Job call() throws Exception {
            job.execute();
            return job;
        }
    }
}
