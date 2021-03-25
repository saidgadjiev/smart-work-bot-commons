package ru.gadjini.telegram.smart.bot.commons.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.gadjini.telegram.smart.bot.commons.service.queue.QueueJobConfigurator;

@Configuration
public class SmartWorkBotConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public QueueJobConfigurator queueJobConfigurator() {
        return new QueueJobConfigurator() {
        };
    }
}
