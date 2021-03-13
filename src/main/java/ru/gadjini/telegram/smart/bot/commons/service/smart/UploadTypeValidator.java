package ru.gadjini.telegram.smart.bot.commons.service.smart;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.model.UploadType;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.format.Format;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UploadTypeValidator {

    private static final int CACHE_TIME_IN_SECONDS = 60 * 60;

    private MessageService messageService;

    private LocalisationService localisationService;

    private Map<UploadType, Validator> validatorMap = Map.of(
            UploadType.DOCUMENT, Validator.DEFAULT,
            UploadType.VIDEO, new Validator() {
                @Override
                public boolean validate(String queryId, Format fileFormat, Locale locale) {
                    if (fileFormat == null || !fileFormat.canBeSentAsVideo()) {
                        messageService.sendAnswerCallbackQuery(
                                AnswerCallbackQuery.builder()
                                        .callbackQueryId(queryId)
                                        .text(getMessage(locale))
                                        .showAlert(true)
                                        .cacheTime(CACHE_TIME_IN_SECONDS)
                                        .build()
                        );

                        return false;
                    }

                    return true;
                }

                private String getMessage(Locale locale) {
                    List<String> videoFormats = Set.of(Format.values()).stream().filter(Format::canBeSentAsVideo)
                            .map(Format::getName).collect(Collectors.toList());
                    return localisationService.getMessage(MessagesProperties.MESSAGE_AS_VIDEO_FORMATS,
                            new Object[]{String.join(", ", videoFormats)}, locale);
                }
            },
            UploadType.STREAMING_VIDEO, new Validator() {
                @Override
                public boolean validate(String queryId, Format fileFormat, Locale locale) {
                    if (fileFormat == null || !fileFormat.supportsStreaming()) {
                        messageService.sendAnswerCallbackQuery(
                                AnswerCallbackQuery.builder()
                                        .callbackQueryId(queryId)
                                        .text(getMessage(locale))
                                        .showAlert(true)
                                        .cacheTime(CACHE_TIME_IN_SECONDS)
                                        .build()
                        );

                        return false;
                    }

                    return true;
                }

                private String getMessage(Locale locale) {
                    List<String> videoFormats = Set.of(Format.values()).stream().filter(Format::supportsStreaming)
                            .map(Format::getName).collect(Collectors.toList());
                    return localisationService.getMessage(MessagesProperties.MESSAGE_AS_STREAMING_VIDEO_FORMATS,
                            new Object[]{String.join(", ", videoFormats)}, locale);
                }
            }
    );

    @Autowired
    public UploadTypeValidator(@Qualifier("messageLimits") MessageService messageService, LocalisationService localisationService) {
        this.messageService = messageService;
        this.localisationService = localisationService;
    }

    public boolean validate(String queryId, Format fileFormat, UploadType uploadType, Locale locale) {
        return validatorMap.getOrDefault(uploadType, Validator.DEFAULT).validate(queryId, fileFormat, locale);
    }

    private interface Validator {

        Validator DEFAULT = (queryId, fileFormat, locale) -> true;

        boolean validate(String queryId, Format fileFormat, Locale locale);
    }
}
