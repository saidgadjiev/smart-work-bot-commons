package ru.gadjini.telegram.smart.bot.commons.service.file;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.NoHttpResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.exception.botapi.TelegramApiRequestException;
import ru.gadjini.telegram.smart.bot.commons.io.SmartTempFile;
import ru.gadjini.telegram.smart.bot.commons.model.Progress;
import ru.gadjini.telegram.smart.bot.commons.service.flood.DownloadFloodWaitController;
import ru.gadjini.telegram.smart.bot.commons.service.telegram.CancelableTelegramBotApiMediaService;

import java.net.SocketException;
import java.util.Objects;

@Service
public class FileDownloader {

    public static final String FILE_ID_TEMPORARILY_UNAVAILABLE = "Bad Request: wrong file_id or the file is temporarily unavailable";

    private CancelableTelegramBotApiMediaService telegramLocalBotApiService;

    private DownloadFloodWaitController floodWaitController;

    @Autowired
    public FileDownloader(CancelableTelegramBotApiMediaService telegramLocalBotApiService, DownloadFloodWaitController floodWaitController) {
        this.telegramLocalBotApiService = telegramLocalBotApiService;
        this.floodWaitController = floodWaitController;
    }

    public void downloadFileByFileId(String fileId, long fileSize, SmartTempFile outputFile) {
        downloadFileByFileId(fileId, fileSize, null, outputFile, true);
    }

    public void downloadFileByFileId(String fileId, long fileSize, SmartTempFile outputFile, boolean withFloodControl) {
        downloadFileByFileId(fileId, fileSize, null, outputFile, withFloodControl);
    }

    public void downloadFileByFileId(String fileId, long fileSize, Progress progress, SmartTempFile outputFile, boolean withFloodControl) {
        if (withFloodControl) {
            downloadWithFloodControl(fileId, fileSize, progress, outputFile);
        } else {
            downloadWithoutFloodControl(fileId, fileSize, progress, outputFile);
        }
    }

    public void cancelDownloading(String fileId, long fileSize) {
        floodWaitController.cancelDownloading(fileId, fileSize);

        telegramLocalBotApiService.cancelDownloading(fileId);
    }

    public void cancelDownloads() {
        telegramLocalBotApiService.cancelDownloads();
    }

    public static boolean isNoneCriticalDownloadingException(Throwable ex) {
        int indexOfNoResponseException = ExceptionUtils.indexOfThrowable(ex, NoHttpResponseException.class);
        int socketException = ExceptionUtils.indexOfThrowable(ex, SocketException.class);

        return indexOfNoResponseException != -1 || socketException != -1;
    }

    private void downloadWithoutFloodControl(String fileId, long fileSize, Progress progress, SmartTempFile outputFile) {
        try {
            telegramLocalBotApiService.downloadFileByFileId(fileId, fileSize, progress, outputFile);
        } catch (TelegramApiRequestException ex) {
            if (isWrongFileIdException(ex)) {
                floodWaitController.downloadingFloodWait();
            } else {
                throw ex;
            }
        }
    }

    private void downloadWithFloodControl(String fileId, long fileSize, Progress progress, SmartTempFile outputFile) {
        floodWaitController.startDownloading(fileId);
        try {
            try {
                telegramLocalBotApiService.downloadFileByFileId(fileId, fileSize, progress, outputFile);
            } catch (TelegramApiRequestException ex) {
                if (isWrongFileIdException(ex)) {
                    floodWaitController.downloadingFloodWait();
                } else {
                    throw ex;
                }
            }
        } finally {
            floodWaitController.finishDownloading(fileId, fileSize);
        }
    }

    private boolean isWrongFileIdException(TelegramApiRequestException ex) {
        int telegramApiRequestExceptionIndexOf = ExceptionUtils.indexOfThrowable(ex, TelegramApiRequestException.class);

        if (telegramApiRequestExceptionIndexOf != -1) {
            TelegramApiRequestException apiRequestException = (TelegramApiRequestException) ExceptionUtils.getThrowables(ex)[telegramApiRequestExceptionIndexOf];

            return Objects.equals(apiRequestException.getApiResponse(), FILE_ID_TEMPORARILY_UNAVAILABLE);
        }

        return false;
    }
}
