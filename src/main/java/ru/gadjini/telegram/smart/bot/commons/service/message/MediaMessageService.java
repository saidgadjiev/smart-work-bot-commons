package ru.gadjini.telegram.smart.bot.commons.service.message;

import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import ru.gadjini.telegram.smart.bot.commons.model.EditMediaResult;
import ru.gadjini.telegram.smart.bot.commons.model.Progress;
import ru.gadjini.telegram.smart.bot.commons.model.SendFileResult;

public interface MediaMessageService {
    EditMediaResult editMessageMedia(EditMessageMedia editMediaContext);

    default SendFileResult sendSticker(SendSticker sendSticker) {
        return sendSticker(sendSticker, null);
    }

    SendFileResult sendSticker(SendSticker sendSticker, Progress progress);

    void sendFile(long chatId, String fileId);

    SendFileResult sendPhoto(SendPhoto sendPhoto);

    default SendFileResult sendDocument(SendDocument sendDocumentContext) {
        return sendDocument(sendDocumentContext, null);
    }

    SendFileResult sendDocument(SendDocument sendDocumentContext, Progress progress);

    default SendFileResult sendVideo(SendVideo sendVideo) {
        return sendVideo(sendVideo, null);
    }

    SendFileResult sendVideo(SendVideo sendVideo, Progress progress);

    default SendFileResult sendAudio(SendAudio sendAudio) {
        return sendAudio(sendAudio, null);
    }

    SendFileResult sendAudio(SendAudio sendAudio, Progress progress);

    default SendFileResult sendVoice(SendVoice sendVoice) {
        return sendVoice(sendVoice, null);
    }

    SendFileResult sendVoice(SendVoice sendVoice, Progress progress);

}
