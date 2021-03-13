package ru.gadjini.telegram.smart.bot.commons.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.service.cleaner.GarbageFileCollector;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;

@Component
public class GarbageFilesCollectorJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(GarbageFilesCollectorJob.class);

    private GarbageFileCollector fileCollector;

    @Autowired
    public GarbageFilesCollectorJob(GarbageFileCollector fileCollector) {
        this.fileCollector = fileCollector;
    }

    @PostConstruct
    public void init() {
        try {
            run();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 */30 * * * *")
    public void run() {
        LOGGER.debug("Start({})", LocalDateTime.now());
        int clean = fileCollector.clean();
        LOGGER.debug("Finish({}, {})", clean, LocalDateTime.now());
    }
}
