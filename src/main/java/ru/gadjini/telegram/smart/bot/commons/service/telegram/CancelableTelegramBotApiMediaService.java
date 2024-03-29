package ru.gadjini.telegram.smart.bot.commons.service.telegram;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import ru.gadjini.telegram.smart.bot.commons.exception.DownloadCanceledException;
import ru.gadjini.telegram.smart.bot.commons.exception.FloodWaitException;
import ru.gadjini.telegram.smart.bot.commons.exception.botapi.TelegramApiException;
import ru.gadjini.telegram.smart.bot.commons.io.SmartTempFile;
import ru.gadjini.telegram.smart.bot.commons.model.Progress;
import ru.gadjini.telegram.smart.bot.commons.property.BotApiProperties;
import ru.gadjini.telegram.smart.bot.commons.property.BotProperties;
import ru.gadjini.telegram.smart.bot.commons.service.Jackson;
import ru.gadjini.telegram.smart.bot.commons.service.file.temp.TempFileService;
import ru.gadjini.telegram.smart.bot.commons.utils.MemoryUtils;
import ru.gadjini.telegram.smart.bot.commons.utils.NetSpeedUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class CancelableTelegramBotApiMediaService extends TelegramBotApiMediaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CancelableTelegramBotApiMediaService.class);

    private final Map<String, SmartTempFile> downloading = new ConcurrentHashMap<>();

    private final Map<String, HttpPost> downloadingRequests = new ConcurrentHashMap<>();

    private final Map<String, SmartTempFile> uploading = new ConcurrentHashMap<>();

    private final BotProperties botProperties;

    private Jackson jackson;

    private Method sendHttpPostRequestMethod;

    private TempFileService tempFileService;

    private TelegramBotApiMethodExecutor exceptionHandler;

    private RequestConfig downloadRequestConfig;

    public CancelableTelegramBotApiMediaService(BotProperties botProperties, Jackson jackson,
                                                DefaultBotOptions botOptions, BotApiProperties botApiProperties,
                                                TempFileService tempFileService,
                                                TelegramBotApiMethodExecutor exceptionHandler,
                                                RequestConfig downloadRequestConfig) {
        super(botProperties, botOptions, botApiProperties, exceptionHandler);
        this.botProperties = botProperties;
        this.jackson = jackson;
        this.tempFileService = tempFileService;
        this.exceptionHandler = exceptionHandler;
        this.downloadRequestConfig = downloadRequestConfig;
        try {
            this.sendHttpPostRequestMethod = DefaultAbsSender.class.getDeclaredMethod("sendHttpPostRequest", HttpPost.class);
            this.sendHttpPostRequestMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String downloadFileByFileId(String fileId, long fileSize, Progress progress, SmartTempFile outputFile) {
        downloading.put(fileId, outputFile);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        AtomicLong resultFileSize = new AtomicLong(fileSize);
        try {
            LOGGER.debug("Start downloadFileByFileId({}, {}, {})", isLocal(), fileId, MemoryUtils.humanReadableByteCount(fileSize));

            return exceptionHandler.executeWithResult(null, () -> {
                updateProgressBeforeStart(progress);
                GetFile gf = new GetFile();
                gf.setFileId(fileId);
                HttpPost downloadingRequest = createDownloadingRequest(gf);
                downloadingRequests.put(fileId, downloadingRequest);
                String filePath;
                try {
                    String responseContent = (String) sendHttpPostRequestMethod.invoke(this, downloadingRequest);
                    org.telegram.telegrambots.meta.api.objects.File file = gf.deserializeResponse(responseContent);
                    resultFileSize.set(file.getFileSize());

                    if (downloadingRequest.isAborted()) {
                        throw new DownloadCanceledException("Download canceled " + fileId);
                    }
                    if (!isLocal()) {
                        filePath = getLocalFilePath(file.getFilePath());

                        if (outputFile != null) {
                            try {
                                if (!outputFile.getParentFile().exists() && !outputFile.getParentFile().mkdirs()) {
                                    LOGGER.debug("Error mkdirs({})", outputFile.getParentFile().getAbsolutePath());
                                }
                                Files.move(Path.of(filePath), outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            filePath = outputFile.getAbsolutePath();
                        } else {
                            LOGGER.debug("Directly downloaded file may be deleted!({}, {})", filePath, fileId);
                        }
                    } else {
                        downloadFile(file, outputFile.getFile());
                        filePath = outputFile.getAbsolutePath();
                    }
                } finally {
                    downloadingRequests.remove(fileId);
                }
                updateProgressAfterComplete(progress);

                LOGGER.debug("Finished successfully downloadFileByFileId({}, {}, {})", isLocal(), fileId,
                        MemoryUtils.humanReadableByteCount(resultFileSize.get()));

                return filePath;
            });
        } catch (TelegramApiException | FloodWaitException e) {
            LOGGER.error(e.getMessage() + "({}, {}, {})", isLocal(), fileId, MemoryUtils.humanReadableByteCount(fileSize));
            throw e;
        } finally {
            downloading.remove(fileId);
            stopWatch.stop();
            long time = stopWatch.getTime(TimeUnit.SECONDS);
            LOGGER.debug("Finish downloadFileByFileId({}, {}, {}, {}, {})", isLocal(), fileId,
                    MemoryUtils.humanReadableByteCount(resultFileSize.get()), time,
                    NetSpeedUtils.toSpeed(resultFileSize.get() / Math.max(1, time)));
        }
    }

    @Override
    public void cancelUploading(String filePath) {
        if (StringUtils.isBlank(filePath)) {
            return;
        }
        try {
            SmartTempFile tempFile = uploading.get(filePath);
            if (tempFile != null) {
                try {
                    tempFileService.delete(tempFile);
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        } finally {
            uploading.remove(filePath);
        }
    }

    @Override
    public void cancelDownloading(String fileId) {
        if (StringUtils.isBlank(fileId)) {
            return;
        }
        try {
            SmartTempFile tempFile = downloading.get(fileId);
            if (tempFile != null) {
                try {
                    tempFileService.delete(tempFile);
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
            HttpPost httpPost = downloadingRequests.get(fileId);
            if (httpPost != null) {
                try {
                    httpPost.abort();
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        } finally {
            downloading.remove(fileId);
            downloadingRequests.remove(fileId);
        }
    }

    @Override
    public void cancelDownloads() {
        try {
            for (Map.Entry<String, SmartTempFile> entry : downloading.entrySet()) {
                try {
                    tempFileService.delete(entry.getValue());
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
            for (Map.Entry<String, HttpPost> entry : downloadingRequests.entrySet()) {
                try {
                    entry.getValue().abort();
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        } finally {
            downloading.clear();
        }
        LOGGER.debug("Downloads canceled");
    }

    @Override
    public String getBotToken() {
        return botProperties.getToken();
    }

    private HttpPost createDownloadingRequest(GetFile method) throws org.telegram.telegrambots.meta.exceptions.TelegramApiException {
        try {
            method.validate();
            String url = getBaseUrl() + method.getMethod();
            HttpPost httppost = new HttpPost(url);
            httppost.setConfig(downloadRequestConfig);
            httppost.addHeader("charset", StandardCharsets.UTF_8.name());
            httppost.setEntity(new StringEntity(jackson.writeValueAsString(method), ContentType.APPLICATION_JSON));

            return httppost;
        } catch (Exception e) {
            throw new org.telegram.telegrambots.meta.exceptions.TelegramApiException(e);
        }
    }
}
