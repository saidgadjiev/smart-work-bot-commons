package ru.gadjini.telegram.smart.bot.commons.service.message.smart;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.domain.UploadQueueItem;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;

import java.util.Locale;

@Service
public class SmartUploadMessageBuilder {

    private LocalisationService localisationService;

    @Autowired
    public SmartUploadMessageBuilder(LocalisationService localisationService) {
        this.localisationService = localisationService;
    }

    public String buildSmartUploadMessage(UploadQueueItem queueItem, Locale locale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_SMART_UPLOAD_IS_READY, new Object[]{
                queueItem.getUploadType()
        }, locale);
    }
}
