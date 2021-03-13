package ru.gadjini.telegram.smart.bot.commons.service.message;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import ru.gadjini.telegram.smart.bot.commons.exception.FloodWaitException;
import ru.gadjini.telegram.smart.bot.commons.exception.botapi.TelegramApiException;
import ru.gadjini.telegram.smart.bot.commons.model.EditMediaResult;
import ru.gadjini.telegram.smart.bot.commons.model.Progress;
import ru.gadjini.telegram.smart.bot.commons.model.SendFileResult;
import ru.gadjini.telegram.smart.bot.commons.utils.ThreadUtils;

import java.net.SocketException;
import java.util.function.Supplier;

@Component
@Qualifier("forceMedia")
public class ForceMediaMessageService implements MediaMessageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ForceMediaMessageService.class);

    private static final int SLEEP_TIME_BEFORE_ATTEMPT = 60000;

    private static final int MAX_ATTEMPTS = 3;

    private MediaMessageService mediaMessageService;

    @Autowired
    public void setMediaMessageService(@Qualifier("mediaLimits") MediaMessageService mediaMessageService) {
        this.mediaMessageService = mediaMessageService;
    }

    @Override
    public EditMediaResult editMessageMedia(EditMessageMedia editMediaContext) {
        return mediaMessageService.editMessageMedia(editMediaContext);
    }

    @Override
    public SendFileResult sendSticker(SendSticker sendSticker, Progress progress) {
        return mediaMessageService.sendSticker(sendSticker, progress);
    }

    @Override
    public SendFileResult sendDocument(SendDocument sendDocument, Progress progress) {
        return forceSend(() -> mediaMessageService.sendDocument(sendDocument, progress));
    }

    @Override
    public void sendFile(long chatId, String fileId) {
        mediaMessageService.sendFile(chatId, fileId);
    }

    @Override
    public SendFileResult sendPhoto(SendPhoto sendPhoto) {
        return mediaMessageService.sendPhoto(sendPhoto);
    }

    @Override
    public SendFileResult sendVideo(SendVideo sendVideo, Progress progress) {
        return mediaMessageService.sendVideo(sendVideo, progress);
    }

    @Override
    public SendFileResult sendAudio(SendAudio sendAudio, Progress progress) {
        return mediaMessageService.sendAudio(sendAudio, progress);
    }

    @Override
    public SendFileResult sendVoice(SendVoice sendVoice, Progress progress) {
        return mediaMessageService.sendVoice(sendVoice, progress);
    }

    private SendFileResult forceSend(Supplier<SendFileResult> sender) {
        int attempts = 1;
        int sleepTime = SLEEP_TIME_BEFORE_ATTEMPT;
        Throwable lastEx = null;
        while (attempts <= MAX_ATTEMPTS) {
            ++attempts;
            try {
                return sender.get();
            } catch (Throwable ex) {
                lastEx = ex;
                if (shouldTryToUploadAgain(ex)) {
                    LOGGER.debug("Attemp({}, {}, {})", attempts, ex.getMessage(), sleepTime);
                    ThreadUtils.sleep(sleepTime, RuntimeException::new);
                    ++attempts;
                    sleepTime += SLEEP_TIME_BEFORE_ATTEMPT;
                } else {
                    throw ex;
                }
            }
        }

        throw new TelegramApiException(lastEx);
    }

    public static boolean shouldTryToUploadAgain(Throwable ex) {
        int socketException = ExceptionUtils.indexOfThrowable(ex, SocketException.class);
        int floodWaitExceptionIndexOf = ExceptionUtils.indexOfThrowable(ex, FloodWaitException.class);

        return socketException != -1 || floodWaitExceptionIndexOf != -1;
    }
}
