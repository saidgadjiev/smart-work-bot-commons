package ru.gadjini.telegram.smart.bot.commons.service.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.gadjini.telegram.smart.bot.commons.dao.WorkQueueDao;
import ru.gadjini.telegram.smart.bot.commons.domain.QueueItem;
import ru.gadjini.telegram.smart.bot.commons.domain.UploadQueueItem;
import ru.gadjini.telegram.smart.bot.commons.job.UploadJob;
import ru.gadjini.telegram.smart.bot.commons.model.Progress;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.file.temp.FileTarget;
import ru.gadjini.telegram.smart.bot.commons.service.file.temp.TempFileService;
import ru.gadjini.telegram.smart.bot.commons.service.format.Format;
import ru.gadjini.telegram.smart.bot.commons.service.keyboard.smart.SmartUploadKeyboardService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.bot.commons.service.message.smart.SmartUploadMessageBuilder;
import ru.gadjini.telegram.smart.bot.commons.service.queue.UploadQueueService;
import ru.gadjini.telegram.smart.bot.commons.service.settings.UserSettingsService;

import java.io.File;
import java.util.Locale;
import java.util.Set;

@Service
public class FileUploadService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUploadService.class);

    private UploadQueueService uploadQueueService;

    private UploadJob uploadJob;

    private WorkQueueDao workQueueDao;

    private SmartUploadKeyboardService smartKeyboardService;

    private SmartUploadMessageBuilder smartUploadMessageBuilder;

    private MessageService messageService;

    private UserService userService;

    private UserSettingsService userSettingsService;

    private FileUploader fileUploader;

    private TempFileService tempFileService;

    @Autowired
    public FileUploadService(UploadQueueService uploadQueueService,
                             WorkQueueDao workQueueDao, SmartUploadKeyboardService smartKeyboardService,
                             SmartUploadMessageBuilder smartUploadMessageBuilder, @Qualifier("messageLimits") MessageService messageService,
                             UserService userService, UserSettingsService userSettingsService, FileUploader fileUploader, TempFileService tempFileService) {
        this.uploadQueueService = uploadQueueService;
        this.workQueueDao = workQueueDao;
        this.smartKeyboardService = smartKeyboardService;
        this.smartUploadMessageBuilder = smartUploadMessageBuilder;
        this.messageService = messageService;
        this.userService = userService;
        this.userSettingsService = userSettingsService;
        this.fileUploader = fileUploader;
        this.tempFileService = tempFileService;
    }

    @Autowired
    public void setUploadJob(UploadJob uploadJob) {
        this.uploadJob = uploadJob;
    }

    public void uploadSmartFile(int uploadId) {
        uploadQueueService.updateStatus(uploadId, QueueItem.Status.WAITING, QueueItem.Status.BLOCKED);
    }

    public void createUpload(int userId, String method, Object body, Format fileFormat, Progress progress, int producerId, Object extra) {
        moveFileToUploadDir(method, body);
        if (isSmartFile(userId)) {
            UploadQueueItem upload = uploadQueueService.createUpload(userId, method, body, fileFormat, progress, workQueueDao.getQueueName(),
                    workQueueDao.getProducerName(), producerId, QueueItem.Status.BLOCKED, extra);
            sendSmartFile(upload);
        } else {
            uploadQueueService.createUpload(userId, method, body, fileFormat, progress, workQueueDao.getQueueName(),
                    workQueueDao.getProducerName(), producerId, QueueItem.Status.WAITING, extra);
        }
    }

    public void createUpload(int userId, String method, Object body, Progress progress, int producerId) {
        createUpload(userId, method, body, null, progress, producerId, null);
    }

    public void createUpload(int userId, String method, Object body, Format fileFormat, Progress progress, int producerId) {
        createUpload(userId, method, body, fileFormat, progress, producerId, null);
    }

    public void cancelUploads(int producerId) {
        uploadJob.cancelUploads(workQueueDao.getProducerName(), producerId);
    }

    public void cancelUploads(Set<Integer> producerIds) {
        uploadJob.cancelUploads(workQueueDao.getProducerName(), producerIds);
    }

    public void cancelUploadsByUserId(int userId) {
        uploadJob.cancelUploadsByUserId(workQueueDao.getProducerName(), userId);
    }

    public void cancelUploads() {
        uploadJob.cancelUploads();
    }

    private boolean isSmartFile(int userId) {
        return userSettingsService.isSmartFileFeatureEnabled(userId);
    }

    private void sendSmartFile(UploadQueueItem uploadQueueItem) {
        Locale locale = userService.getLocaleOrDefault(uploadQueueItem.getUserId());
        InlineKeyboardMarkup smartKeyboard = smartKeyboardService.getSmartUploadKeyboard(uploadQueueItem.getId(), locale);
        messageService.sendMessage(
                SendMessage.builder().chatId(String.valueOf(uploadQueueItem.getUserId()))
                        .text(smartUploadMessageBuilder.buildSmartUploadMessage(uploadQueueItem, locale))
                        .parseMode(ParseMode.HTML)
                        .replyMarkup(smartKeyboard)
                        .build()
        );
    }

    private void moveFileToUploadDir(String method, Object body) {
        InputFile inputFile = fileUploader.getInputFile(method, body);

        if (inputFile.isNew()) {
            File file = inputFile.getNewMediaFile();

            File result = tempFileService.moveTo(file, FileTarget.UPLOAD);
            inputFile.setMedia(result, inputFile.getMediaName());
            LOGGER.debug("Move({}, {})", file.getAbsolutePath(), result.getAbsolutePath());
        }
    }
}
