package ru.gadjini.telegram.smart.bot.commons.service.keyboard.smart;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.gadjini.telegram.smart.bot.commons.service.keyboard.SmartInlineKeyboardService;

import java.util.List;
import java.util.Locale;

@Service
public class SmartFileInlineKeyboardService {

    private SmartFileButtonFactory smartButtonFactory;

    private SmartInlineKeyboardService smartInlineKeyboardService;

    @Autowired
    public SmartFileInlineKeyboardService(SmartFileButtonFactory smartButtonFactory,
                                          SmartInlineKeyboardService smartInlineKeyboardService) {
        this.smartButtonFactory = smartButtonFactory;
        this.smartInlineKeyboardService = smartInlineKeyboardService;
    }

    public InlineKeyboardMarkup goBackKeyboard(int uploadId, Locale locale) {
        InlineKeyboardMarkup inlineKeyboardMarkup = smartInlineKeyboardService.inlineKeyboardMarkup();
        inlineKeyboardMarkup.getKeyboard().add(List.of(smartButtonFactory.goBackButton(uploadId, locale)));

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getSmartUploadKeyboard(int uploadId, Locale locale) {
        InlineKeyboardMarkup inlineKeyboardMarkup = smartInlineKeyboardService.inlineKeyboardMarkup();
        inlineKeyboardMarkup.getKeyboard().add(List.of(smartButtonFactory.captionButton(uploadId, locale)));
        inlineKeyboardMarkup.getKeyboard().add(List.of(smartButtonFactory.thumbButton(uploadId, locale)));
        inlineKeyboardMarkup.getKeyboard().add(List.of(smartButtonFactory.doSmartUpload(uploadId, locale)));

        return inlineKeyboardMarkup;
    }
}
