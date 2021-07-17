package ru.gadjini.telegram.smart.bot.commons.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import ru.gadjini.telegram.smart.bot.commons.io.SmartTempFile;
import ru.gadjini.telegram.smart.bot.commons.model.Progress;
import ru.gadjini.telegram.smart.bot.commons.service.telegram.TelegramBotApiService;

import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

@Component
@SuppressWarnings("PMD")
public class RemoteFileDownloader {

    private TelegramBotApiService telegramBotApiService;

    private RestTemplate restTemplate;

    @Autowired
    public RemoteFileDownloader(TelegramBotApiService telegramBotApiService, RestTemplate restTemplate) {
        this.telegramBotApiService = telegramBotApiService;
        this.restTemplate = restTemplate;
    }

    public String download(String url, SmartTempFile file, Progress progress) {
        updateProgressBeforeStart(progress);
        RequestCallback requestCallback = request -> request
                .getHeaders()
                .setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM, MediaType.ALL));

        ResponseExtractor<Void> responseExtractor = response -> {
            Files.copy(response.getBody(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);

            return null;
        };
        restTemplate.execute(url, HttpMethod.GET, requestCallback, responseExtractor);
        updateProgressAfterComplete(progress);

        return file.getAbsolutePath();
    }

    private void updateProgressBeforeStart(Progress progress) {
        if (progress != null) {
            try {
                EditMessageText editMessageText = new EditMessageText();
                editMessageText.setChatId(progress.getChatId());
                editMessageText.setText(progress.getProgressMessage());
                editMessageText.setReplyMarkup(progress.getProgressReplyMarkup());
                editMessageText.setMessageId(progress.getProgressMessageId());
                editMessageText.setParseMode(ParseMode.HTML);
                telegramBotApiService.execute(editMessageText);
            } catch (Exception var3) {
            }

        }
    }

    private void updateProgressAfterComplete(Progress progress) {
        if (progress != null && !StringUtils.isBlank(progress.getAfterProgressCompletionMessage())) {
            try {
                EditMessageText editMessageText = new EditMessageText();
                editMessageText.setChatId(progress.getChatId());
                editMessageText.setText(progress.getAfterProgressCompletionMessage());
                editMessageText.setReplyMarkup(progress.getAfterProgressCompletionReplyMarkup());
                editMessageText.setMessageId(progress.getProgressMessageId());
                editMessageText.setParseMode(ParseMode.HTML);
                telegramBotApiService.execute(editMessageText);
            } catch (Exception var3) {
            }
        }
    }
}
