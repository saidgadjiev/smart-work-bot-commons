package ru.gadjini.telegram.smart.bot.commons.service.keyboard.smart;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.gadjini.telegram.smart.bot.commons.service.file.FileUploadUtils;
import ru.gadjini.telegram.smart.bot.commons.service.keyboard.SmartInlineKeyboardService;

import java.util.ArrayList;
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

    public InlineKeyboardMarkup captionKeyboard(int uploadId, Locale locale) {
        InlineKeyboardMarkup inlineKeyboardMarkup = smartInlineKeyboardService.inlineKeyboardMarkup();
        inlineKeyboardMarkup.getKeyboard().add(List.of(smartButtonFactory.removeCaptionButton(uploadId, locale)));
        inlineKeyboardMarkup.getKeyboard().add(List.of(smartButtonFactory.goBackButton(uploadId, locale)));

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup goBackKeyboard(int uploadId, Locale locale) {
        InlineKeyboardMarkup inlineKeyboardMarkup = smartInlineKeyboardService.inlineKeyboardMarkup();
        inlineKeyboardMarkup.getKeyboard().add(List.of(smartButtonFactory.goBackButton(uploadId, locale)));

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getSmartUploadKeyboard(int uploadId, String method, Object body, Locale locale) {
        InlineKeyboardMarkup inlineKeyboardMarkup = smartInlineKeyboardService.inlineKeyboardMarkup();
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        if (FileUploadUtils.isFileNameSupported(method, body)) {
            buttons.add(smartButtonFactory.fileNameButton(uploadId, locale));
        }
        if (FileUploadUtils.isCaptionSupported(method, body)) {
            buttons.add(smartButtonFactory.captionButton(uploadId, locale));
        }
        if (FileUploadUtils.isThumbSupported(method, body)) {
            buttons.add(smartButtonFactory.thumbButton(uploadId, locale));
        }
        List<List<InlineKeyboardButton>> partition = Lists.partition(buttons, 2);
        for (List<InlineKeyboardButton> list : partition) {
            inlineKeyboardMarkup.getKeyboard().add(list);
        }
        inlineKeyboardMarkup.getKeyboard().add(List.of(smartButtonFactory.doSmartUpload(uploadId, locale)));

        return inlineKeyboardMarkup;
    }
}
