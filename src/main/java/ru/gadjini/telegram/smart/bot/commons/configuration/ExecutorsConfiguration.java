package ru.gadjini.telegram.smart.bot.commons.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import ru.gadjini.telegram.smart.bot.commons.job.DownloadJob;
import ru.gadjini.telegram.smart.bot.commons.job.UploadJob;
import ru.gadjini.telegram.smart.bot.commons.job.WorkQueueJob;
import ru.gadjini.telegram.smart.bot.commons.property.ServerProperties;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.concurrent.SmartExecutorService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
@SuppressWarnings("CPD-START")
public class ExecutorsConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorsConfiguration.class);

    private WorkQueueJob workQueueJob;

    private DownloadJob downloadingJob;

    private UploadJob uploadJob;

    @Value("${work.light.threads:2}")
    private int workQueueLightThreads;

    @Value("${work.heavy.threads:4}")
    private int workQueueHeavyThreads;

    @Value("${download.heavy.threads:1}")
    private int downloadHeavyThreads;

    @Value("${download.light.threads:2}")
    private int downloadLightThreads;

    @Value("${upload.heavy.threads:1}")
    private int uploadHeavyThreads;

    @Value("${upload.light.threads:2}")
    private int uploadLightThreads;

    @Autowired
    public ExecutorsConfiguration(ServerProperties serverProperties) {
        LOGGER.debug("Server number({})", serverProperties.getNumber());
        LOGGER.debug("Servers({})", serverProperties.getServers());
    }

    @PostConstruct
    public void init() {
        LOGGER.debug("Work queue light threads({})", workQueueLightThreads);
        LOGGER.debug("Work queue heavy threads({})", workQueueHeavyThreads);
        LOGGER.debug("Download heavy threads({})", downloadHeavyThreads);
        LOGGER.debug("Download light threads({})", downloadLightThreads);
        LOGGER.debug("Upload heavy threads({})", uploadHeavyThreads);
        LOGGER.debug("Upload light threads({})", uploadLightThreads);
    }

    @Autowired
    public void setWorkQueueJob(WorkQueueJob workQueueJob) {
        this.workQueueJob = workQueueJob;
    }

    @Autowired
    public void setDownloadingJob(DownloadJob downloadingJob) {
        this.downloadingJob = downloadingJob;
    }

    @Autowired
    public void setUploadJob(UploadJob uploadJob) {
        this.uploadJob = uploadJob;
    }

    @Bean
    @Qualifier("queueTaskExecutor")
    public SmartExecutorService queueTaskExecutor(UserService userService,
                                                  @Qualifier("messageLimits") MessageService messageService, LocalisationService localisationService) {
        SmartExecutorService executorService = new SmartExecutorService(messageService, localisationService, userService);
        ThreadPoolExecutor lightTaskExecutor = new ThreadPoolExecutor(workQueueLightThreads, workQueueLightThreads, 0, TimeUnit.SECONDS, new SynchronousQueue<>()) {
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
                executorService.complete(r);
            }
        };
        ThreadPoolExecutor heavyTaskExecutor = new ThreadPoolExecutor(workQueueHeavyThreads, workQueueHeavyThreads, 0, TimeUnit.SECONDS, new SynchronousQueue<>()) {
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
                executorService.complete(r);
            }
        };

        LOGGER.debug("Conversion light thread pool({})", lightTaskExecutor.getCorePoolSize());
        LOGGER.debug("Conversion heavy thread pool({})", heavyTaskExecutor.getCorePoolSize());

        executorService.setExecutors(Map.of(SmartExecutorService.JobWeight.LIGHT, lightTaskExecutor, SmartExecutorService.JobWeight.HEAVY, heavyTaskExecutor));
        executorService.setRejectJobHandler(SmartExecutorService.JobWeight.HEAVY, job -> workQueueJob.rejectTask(job));
        executorService.setRejectJobHandler(SmartExecutorService.JobWeight.LIGHT, job -> workQueueJob.rejectTask(job));

        return executorService;
    }

    @Bean
    public TaskScheduler jobsThreadPoolTaskScheduler(UserService userService) {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(Runtime.getRuntime().availableProcessors());
        threadPoolTaskScheduler.setThreadNamePrefix("JobsThreadPoolTaskScheduler");
        threadPoolTaskScheduler.setErrorHandler(throwable -> {
            LOGGER.error(throwable.getMessage(), throwable);
            userService.handleBotBlockedByUser(throwable);
        });

        LOGGER.debug("Jobs thread pool scheduler initialized with pool size: {}", threadPoolTaskScheduler.getPoolSize());

        return threadPoolTaskScheduler;
    }

    @Bean
    @Qualifier("downloadTasksExecutor")
    public SmartExecutorService downloadTasksExecutor(UserService userService, @Qualifier("messageLimits") MessageService messageService,
                                                      LocalisationService localisationService) {
        SmartExecutorService executorService = new SmartExecutorService(messageService, localisationService, userService);
        ThreadPoolExecutor heavyTaskExecutor = new ThreadPoolExecutor(downloadHeavyThreads, downloadHeavyThreads, 0, TimeUnit.SECONDS, new SynchronousQueue<>()) {
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
                executorService.complete(r);
            }
        };

        ThreadPoolExecutor lightTaskExecutor = new ThreadPoolExecutor(downloadLightThreads, downloadLightThreads, 0, TimeUnit.SECONDS, new SynchronousQueue<>()) {
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
                executorService.complete(r);
            }
        };

        LOGGER.debug("Heavy download threads initialized with pool size: {}", heavyTaskExecutor.getCorePoolSize());
        LOGGER.debug("Light download threads initialized with pool size: {}", lightTaskExecutor.getCorePoolSize());
        executorService.setExecutors(Map.of(SmartExecutorService.JobWeight.LIGHT, lightTaskExecutor, SmartExecutorService.JobWeight.HEAVY, heavyTaskExecutor));
        executorService.setRejectJobHandler(SmartExecutorService.JobWeight.HEAVY, job -> downloadingJob.rejectTask(job));
        executorService.setRejectJobHandler(SmartExecutorService.JobWeight.LIGHT, job -> downloadingJob.rejectTask(job));

        return executorService;
    }

    @Bean
    @Qualifier("uploadTasksExecutor")
    public SmartExecutorService uploadTasksExecutor(UserService userService, @Qualifier("messageLimits") MessageService messageService,
                                                    LocalisationService localisationService) {
        SmartExecutorService executorService = new SmartExecutorService(messageService, localisationService, userService);
        ThreadPoolExecutor heavyTaskExecutor = new ThreadPoolExecutor(uploadHeavyThreads, uploadHeavyThreads, 0, TimeUnit.SECONDS, new SynchronousQueue<>()) {
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
                executorService.complete(r);
            }
        };

        ThreadPoolExecutor lightTaskExecutor = new ThreadPoolExecutor(uploadLightThreads, uploadLightThreads, 0, TimeUnit.SECONDS, new SynchronousQueue<>()) {
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
                executorService.complete(r);
            }
        };

        LOGGER.debug("Heavy upload threads initialized with pool size: {}", heavyTaskExecutor.getCorePoolSize());
        LOGGER.debug("Light upload threads initialized with pool size: {}", lightTaskExecutor.getCorePoolSize());
        executorService.setExecutors(Map.of(SmartExecutorService.JobWeight.LIGHT, lightTaskExecutor, SmartExecutorService.JobWeight.HEAVY, heavyTaskExecutor));
        executorService.setRejectJobHandler(SmartExecutorService.JobWeight.HEAVY, job -> uploadJob.rejectTask(job));
        executorService.setRejectJobHandler(SmartExecutorService.JobWeight.LIGHT, job -> uploadJob.rejectTask(job));

        return executorService;
    }
}
