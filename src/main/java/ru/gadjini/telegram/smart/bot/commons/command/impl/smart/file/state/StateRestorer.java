package ru.gadjini.telegram.smart.bot.commons.command.impl.smart.file.state;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.command.impl.smart.file.SmartFileCommandState;
import ru.gadjini.telegram.smart.bot.commons.domain.UploadQueueItem;
import ru.gadjini.telegram.smart.bot.commons.service.file.FileUploadUtils;
import ru.gadjini.telegram.smart.bot.commons.service.queue.UploadQueueService;

@Component
public class StateRestorer {

    private UploadQueueService uploadQueueService;

    @Autowired
    public StateRestorer(UploadQueueService uploadQueueService) {
        this.uploadQueueService = uploadQueueService;
    }

    public SmartFileCommandState restoreState(int uploadId, int messageId) {
        SmartFileCommandState commandState = new SmartFileCommandState();
        commandState.setStateName(SmartFileStateName.FATHER);
        UploadQueueItem uploadQueueItem = uploadQueueService.getById(uploadId);
        commandState.setUploadId(uploadId);

        commandState.setMethod(uploadQueueItem.getMethod());
        commandState.setBody(uploadQueueItem.getBody());
        commandState.setCaption(FileUploadUtils.getCaption(uploadQueueItem.getMethod(), uploadQueueItem.getBody()));
        commandState.setThumb(uploadQueueItem.getThumbFileId());
        commandState.setFileName(FileUploadUtils.getFileName(uploadQueueItem.getMethod(), uploadQueueItem.getBody()));

        commandState.setMessageId(messageId);

        return commandState;
    }
}
