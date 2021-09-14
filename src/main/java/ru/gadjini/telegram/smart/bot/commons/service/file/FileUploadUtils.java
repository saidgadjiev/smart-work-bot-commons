package ru.gadjini.telegram.smart.bot.commons.service.file;

import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.objects.InputFile;

public class FileUploadUtils {

    private FileUploadUtils() {
    }

    public static boolean isCaptionSupported(String method, Object body) {
        InputFile inputFile = getInputFile(method, body);
        if (inputFile == null || !inputFile.isNew()) {
            return false;
        }
        switch (method) {
            case SendVoice.PATH:
            case SendVideo.PATH:
            case SendAudio.PATH:
            case SendPhoto.PATH:
            case SendDocument.PATH: {
                return true;
            }
            default:
                return false;
        }
    }

    public static boolean isFileNameSupported(String method, Object body) {
        InputFile inputFile = getInputFile(method, body);
        if (inputFile == null || !inputFile.isNew()) {
            return false;
        }
        switch (method) {
            case SendVideoNote.PATH:
            case SendSticker.PATH:
            case SendVoice.PATH:
            case SendVideo.PATH:
            case SendAudio.PATH:
            case SendPhoto.PATH:
            case SendDocument.PATH: {
                return true;
            }
            default:
                return false;
        }
    }

    public static boolean isThumbSupported(String method, Object body) {
        InputFile inputFile = getInputFile(method, body);
        if (inputFile == null || !inputFile.isNew()) {
            return false;
        }
        switch (method) {
            case SendVideoNote.PATH:
            case SendVideo.PATH:
            case SendAudio.PATH:
            case SendDocument.PATH:
                return true;
            default:
                return false;
        }
    }

    public static String getCaption(String method, Object body) {
        String caption = null;

        switch (method) {
            case SendDocument.PATH: {
                SendDocument sendDocument = (SendDocument) body;
                caption = sendDocument.getCaption();
                break;
            }
            case SendPhoto.PATH: {
                SendPhoto sendPhoto = (SendPhoto) body;
                caption = sendPhoto.getCaption();
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

    public static String getFileName(String method, Object body) {
        String fileName = null;

        switch (method) {
            case SendDocument.PATH: {
                SendDocument sendDocument = (SendDocument) body;
                fileName = sendDocument.getDocument().getMediaName();
                break;
            }
            case SendPhoto.PATH: {
                SendPhoto sendPhoto = (SendPhoto) body;
                fileName = sendPhoto.getPhoto().getMediaName();
                break;
            }
            case SendAudio.PATH: {
                SendAudio sendAudio = (SendAudio) body;
                fileName = sendAudio.getAudio().getMediaName();
                break;
            }
            case SendVideo.PATH: {
                SendVideo sendVideo = (SendVideo) body;
                fileName = sendVideo.getVideo().getMediaName();
                break;
            }
            case SendVoice.PATH: {
                SendVoice sendVoice = (SendVoice) body;
                fileName = sendVoice.getVoice().getMediaName();
                break;
            }
            case SendSticker.PATH: {
                SendSticker sendSticker = (SendSticker) body;
                fileName = sendSticker.getSticker().getMediaName();
                break;
            }
            case SendVideoNote.PATH: {
                SendVideoNote sendVideoNote = (SendVideoNote) body;
                fileName = sendVideoNote.getVideoNote().getMediaName();
                break;
            }
        }

        return fileName;
    }

    public static InputFile getThumbFile(String method, Object body) {
        InputFile thumb = null;
        switch (method) {
            case SendDocument.PATH: {
                SendDocument sendDocument = (SendDocument) body;
                thumb = sendDocument.getThumb();
                break;
            }
            case SendAudio.PATH: {
                SendAudio sendAudio = (SendAudio) body;
                thumb = sendAudio.getThumb();
                break;
            }
            case SendVideo.PATH: {
                SendVideo sendVideo = (SendVideo) body;
                thumb = sendVideo.getThumb();
                break;
            }
            case SendVideoNote.PATH: {
                SendVideoNote sendVideoNote = (SendVideoNote) body;
                thumb = sendVideoNote.getThumb();
                break;
            }
        }

        return thumb;
    }

    public static void setFileName(String method, Object body, String fileName) {
        switch (method) {
            case SendDocument.PATH: {
                SendDocument sendDocument = (SendDocument) body;
                sendDocument.getDocument().setMedia(sendDocument.getDocument().getNewMediaFile(), fileName);
                break;
            }
            case SendPhoto.PATH: {
                SendPhoto sendPhoto = (SendPhoto) body;
                sendPhoto.getPhoto().setMedia(sendPhoto.getPhoto().getNewMediaFile(), fileName);
                break;
            }
            case SendAudio.PATH: {
                SendAudio sendAudio = (SendAudio) body;
                sendAudio.getAudio().setMedia(sendAudio.getAudio().getNewMediaFile(), fileName);
                break;
            }
            case SendVideo.PATH: {
                SendVideo sendVideo = (SendVideo) body;
                sendVideo.getVideo().setMedia(sendVideo.getVideo().getNewMediaFile(), fileName);
                break;
            }
            case SendVoice.PATH: {
                SendVoice sendVoice = (SendVoice) body;
                sendVoice.getVoice().setMedia(sendVoice.getVoice().getNewMediaFile(), fileName);
                break;
            }
            case SendSticker.PATH: {
                SendSticker sendSticker = (SendSticker) body;
                sendSticker.getSticker().setMedia(sendSticker.getSticker().getNewMediaFile(), fileName);
                break;
            }
            case SendVideoNote.PATH: {
                SendVideoNote sendVideoNote = (SendVideoNote) body;
                sendVideoNote.getVideoNote().setMedia(sendVideoNote.getVideoNote().getNewMediaFile(), fileName);
                break;
            }
        }
    }

    public static void setCaption(String method, Object body, String caption) {
        switch (method) {
            case SendDocument.PATH: {
                SendDocument sendDocument = (SendDocument) body;
                sendDocument.setCaption(caption);
                break;
            }
            case SendPhoto.PATH: {
                SendPhoto sendPhoto = (SendPhoto) body;
                sendPhoto.setCaption(caption);
                break;
            }
            case SendAudio.PATH: {
                SendAudio sendAudio = (SendAudio) body;
                sendAudio.setCaption(caption);
                break;
            }
            case SendVideo.PATH: {
                SendVideo sendVideo = (SendVideo) body;
                sendVideo.setCaption(caption);
                break;
            }
            case SendVoice.PATH: {
                SendVoice sendVoice = (SendVoice) body;
                sendVoice.setCaption(caption);
                break;
            }
        }
    }

    public static void setThumbFile(String method, Object body, InputFile thumb) {
        switch (method) {
            case SendDocument.PATH: {
                SendDocument sendDocument = (SendDocument) body;
                sendDocument.setThumb(thumb);
                break;
            }
            case SendAudio.PATH: {
                SendAudio sendAudio = (SendAudio) body;
                sendAudio.setThumb(thumb);
                break;
            }
            case SendVideo.PATH: {
                SendVideo sendVideo = (SendVideo) body;
                sendVideo.setThumb(thumb);
                break;
            }
            case SendVideoNote.PATH: {
                SendVideoNote sendVideoNote = (SendVideoNote) body;
                sendVideoNote.setThumb(thumb);
                break;
            }
        }
    }

    public static InputFile getInputFile(String method, Object body) {
        InputFile inputFile = null;
        switch (method) {
            case SendDocument.PATH: {
                SendDocument sendDocument = (SendDocument) body;
                inputFile = sendDocument.getDocument();
                break;
            }
            case SendPhoto.PATH: {
                SendPhoto sendDocument = (SendPhoto) body;
                inputFile = sendDocument.getPhoto();
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
