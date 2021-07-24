package ru.gadjini.telegram.smart.bot.commons.service.file;

import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.objects.InputFile;

public class FileUploadUtils {

    private FileUploadUtils() {
    }

    public static String getCaption(String method, Object body) {
        String caption = null;

        switch (method) {
            case SendDocument.PATH: {
                SendDocument sendDocument = (SendDocument) body;
                caption = sendDocument.getCaption();
                break;
            }
            case SendAudio.PATH: {
                SendAudio sendAudio = (SendAudio) body;
                caption = sendAudio.getCaption();
                break;
            }
            case SendVideo.PATH: {
                SendVideo sendVideo = (SendVideo) body;
                caption = sendVideo.getCaption();
                break;
            }
            case SendVoice.PATH: {
                SendVoice sendVoice = (SendVoice) body;
                caption = sendVoice.getCaption();
                break;
            }
        }

        return caption;
    }

    public static InputFile getInputFile(String method, Object body) {
        InputFile inputFile = null;
        switch (method) {
            case SendDocument.PATH: {
                SendDocument sendDocument = (SendDocument) body;
                inputFile = sendDocument.getDocument();
                break;
            }
            case SendAudio.PATH: {
                SendAudio sendAudio = (SendAudio) body;
                inputFile = sendAudio.getAudio();
                break;
            }
            case SendVideo.PATH: {
                SendVideo sendVideo = (SendVideo) body;
                inputFile = sendVideo.getVideo();
                break;
            }
            case SendVoice.PATH: {
                SendVoice sendVoice = (SendVoice) body;
                inputFile = sendVoice.getVoice();
                break;
            }
            case SendSticker.PATH: {
                SendSticker sendSticker = (SendSticker) body;
                inputFile = sendSticker.getSticker();
                break;
            }
            case SendVideoNote.PATH: {
                SendVideoNote sendVideoNote = (SendVideoNote) body;
                inputFile = sendVideoNote.getVideoNote();
                break;
            }
        }
        if (inputFile == null) {
            throw new IllegalArgumentException("Null input file " + body);
        }

        return inputFile;
    }

    public static String getFilePath(String method, Object body) {
        InputFile inputFile = getInputFile(method, body);

        if (inputFile.isNew()) {
            return inputFile.getNewMediaFile().getAbsolutePath();
        }

        return null;
    }
}
