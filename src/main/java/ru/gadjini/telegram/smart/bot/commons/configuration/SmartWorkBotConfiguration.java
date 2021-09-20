package ru.gadjini.telegram.smart.bot.commons.configuration;

import org.apache.http.client.config.RequestConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import ru.gadjini.telegram.smart.bot.commons.annotation.botapi.LocalBotApi;
import ru.gadjini.telegram.smart.bot.commons.annotation.botapi.TelegramBotApi;
import ru.gadjini.telegram.smart.bot.commons.property.BotApiProperties;
import ru.gadjini.telegram.smart.bot.commons.property.BotProperties;
import ru.gadjini.telegram.smart.bot.commons.service.Jackson;
import ru.gadjini.telegram.smart.bot.commons.service.file.temp.TempFileService;
import ru.gadjini.telegram.smart.bot.commons.service.queue.QueueJobConfigurator;
import ru.gadjini.telegram.smart.bot.commons.service.telegram.CancelableTelegramBotApiMediaService;
import ru.gadjini.telegram.smart.bot.commons.service.telegram.TelegramBotApiMethodExecutor;
import ru.gadjini.telegram.smart.bot.commons.service.telegram.TelegramMediaService;

@Configuration
public class SmartWorkBotConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public QueueJobConfigurator queueJobConfigurator() {
        return new QueueJobConfigurator() {
        };
    }

    @Bean
    @LocalBotApi
    public TelegramMediaService localTelegramBotApiMediaService(BotProperties botProperties,
                                                                Jackson jackson,
                                                                @LocalBotApi DefaultBotOptions options,
                                                                BotApiProperties botApiProperties,
                                                                TelegramBotApiMethodExecutor exceptionHandler,
                                                                TempFileService tempFileService,
                                                                @Qualifier("downloadRequestConfig") RequestConfig downloadRequestConfig) {
        return new CancelableTelegramBotApiMediaService(botProperties, jackson, options, botApiProperties,
                tempFileService, exceptionHandler, downloadRequestConfig);
    }

    @Bean
    @TelegramBotApi
    public TelegramMediaService telegramBotApiMediaService(BotProperties botProperties,
                                                           Jackson jackson,
                                                           @TelegramBotApi DefaultBotOptions options, BotApiProperties botApiProperties,
                                                           TelegramBotApiMethodExecutor exceptionHandler,
                                                           TempFileService tempFileService,
                                                           @Qualifier("downloadRequestConfig") RequestConfig downloadRequestConfig) {
        return new CancelableTelegramBotApiMediaService(botProperties, jackson, options, botApiProperties,
                tempFileService, exceptionHandler, downloadRequestConfig);
    }

}
