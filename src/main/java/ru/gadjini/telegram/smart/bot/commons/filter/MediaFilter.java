package ru.gadjini.telegram.smart.bot.commons.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.exception.UserException;
import ru.gadjini.telegram.smart.bot.commons.model.MessageMedia;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.telegram.smart.bot.commons.property.FileLimitProperties;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.MessageMediaService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.utils.MemoryUtils;

import java.util.Locale;

import static ru.gadjini.telegram.smart.bot.commons.common.TgConstants.LARGE_FILE_SIZE;

@Component
public class MediaFilter extends BaseBotFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MediaFilter.class);

    private UserService userService;

    private LocalisationService localisationService;

    private MessageMediaService fileService;

    private FileLimitProperties fileLimitProperties;

    @Autowired
    public MediaFilter(UserService userService, LocalisationService localisationService,
                       MessageMediaService fileService, FileLimitProperties fileLimitProperties) {
        this.userService = userService;
        this.localisationService = localisationService;
        this.fileService = fileService;
        this.fileLimitProperties = fileLimitProperties;
    }

    @Override
    public void doFilter(Update update) {
        if (update.hasMessage()) {
            MessageMedia file = fileService.getMedia(update.getMessage(), Locale.getDefault());
            if (file != null) {
                checkInMediaSize(update.getMessage(), file);
            }
        }

        super.doFilter(update);
    }

    private void checkInMediaSize(Message message, MessageMedia file) {
        if (file.getFileSize() > LARGE_FILE_SIZE) {
            LOGGER.warn("Large in file({}, {})", message.getFrom().getId(), file);
            throw new UserException(localisationService.getMessage(
                    MessagesProperties.MESSAGE_TOO_LARGE_IN_FILE,
                    new Object[]{MemoryUtils.humanReadableByteCount(message.getDocument().getFileSize())},
                    userService.getLocaleOrDefault(message.getFrom().getId())));
        } else if (file.getFileSize() > fileLimitProperties.getLightFileMaxWeight()) {
            LOGGER.warn("Heavy file({}, {})", message.getFrom().getId(), file);
        }
    }
}
