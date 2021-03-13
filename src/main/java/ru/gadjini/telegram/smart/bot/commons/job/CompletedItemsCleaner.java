package ru.gadjini.telegram.smart.bot.commons.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.dao.WorkQueueDao;
import ru.gadjini.telegram.smart.bot.commons.domain.QueueItem;
import ru.gadjini.telegram.smart.bot.commons.service.queue.DownloadQueueService;
import ru.gadjini.telegram.smart.bot.commons.service.queue.UploadQueueService;
import ru.gadjini.telegram.smart.bot.commons.service.queue.WorkQueueService;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CompletedItemsCleaner {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompletedItemsCleaner.class);

    private WorkQueueService workQueueService;

    private DownloadQueueService downloadQueueService;

    private UploadQueueService uploadQueueService;

    @Autowired
    public CompletedItemsCleaner(WorkQueueService workQueueService, DownloadQueueService downloadQueueService, UploadQueueService uploadQueueService) {
        this.workQueueService = workQueueService;
        this.downloadQueueService = downloadQueueService;
        this.uploadQueueService = uploadQueueService;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void clean() {
        List<QueueItem> queueItems = workQueueService.deleteCompleted();
        String queueName = workQueueService.getQueueDao().getQueueName();
        String producerName = ((WorkQueueDao) workQueueService.getQueueDao()).getProducerName();
        Set<Integer> queueItemsIds = queueItems.stream().map(QueueItem::getId).collect(Collectors.toSet());
        downloadQueueService.deleteCompletedAndOrphans(producerName, queueName, queueItemsIds);
        uploadQueueService.deleteCompletedAndOrphans(producerName, queueName, queueItemsIds);

        LOGGER.debug("Delete completed({}, {})", queueItems.size(), new Date());
    }
}
