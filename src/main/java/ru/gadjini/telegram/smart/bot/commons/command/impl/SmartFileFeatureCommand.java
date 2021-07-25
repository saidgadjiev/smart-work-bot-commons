package ru.gadjini.telegram.smart.bot.commons.command.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.telegram.smart.bot.commons.annotation.KeyboardHolder;
import ru.gadjini.telegram.smart.bot.commons.annotation.TgMessageLimitsControl;
import ru.gadjini.telegram.smart.bot.commons.command.api.BotCommand;
import ru.gadjini.telegram.smart.bot.commons.command.api.NavigableBotCommand;
import ru.gadjini.telegram.smart.bot.commons.common.CommandNames;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.common.SmartWorkCommandNames;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.command.navigator.CommandNavigator;
import ru.gadjini.telegram.smart.bot.commons.service.keyboard.ReplyKeyboardService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.bot.commons.service.settings.UserSettingsService;

import java.util.Locale;

@Component
public class SmartFileFeatureCommand implements BotCommand, NavigableBotCommand {

    private UserSettingsService userSettingsService;

    private MessageService messageService;

    private UserService userService;

    private ReplyKeyboardService replyKeyboardService;

    private CommandNavigator commandNavigator;

    private LocalisationService localisationService;

    @Autowired
    public SmartFileFeatureCommand(UserSettingsService userSettingsService,
                                   @TgMessageLimitsControl MessageService messageService,
                                   UserService userService, @KeyboardHolder ReplyKeyboardService replyKeyboardService,
                                   LocalisationService localisationService) {
        this.userSettingsService = userSettingsService;
        this.messageService = messageService;
        this.userService = userService;
        this.replyKeyboardService = replyKeyboardService;
        this.localisationService = localisationService;
    }

    @Autowired
    public void setCommandNavigator(CommandNavigator commandNavigator) {
        this.commandNavigator = commandNavigator;
    }

    @Override
    public void processMessage(Message message, String[] params) {
        boolean smartFileFeatureEnabled = userSettingsService.isSmartFileFeatureEnabled(message.getFrom().getId());
        Locale locale = userService.getLocaleOrDefault(message.getFrom().getId());
        messageService.sendMessage(
                SendMessage.builder().chatId(String.valueOf(message.getChatId()))
                        .text(getSmartFileFeatureMessage(smartFileFeatureEnabled, locale))
                        .parseMode(ParseMode.HTML)
                        .replyMarkup(replyKeyboardService.smartFileFeatureKeyboard(message.getChatId(), locale))
                        .build()
        );
    }

    @Override
    public void processNonCommandUpdate(Message message, String text) {
        Locale locale = userService.getLocaleOrDefault(message.getFrom().getId());
        String enableCommand = localisationService.getMessage(MessagesProperties.ENABLE_COMMAND_NAME, locale);
        String disableCommand = localisationService.getMessage(MessagesProperties.DISABLE_COMMAND_NAME, locale);

        if (enableCommand.equals(text)) {
            userSettingsService.smartFileFeature(message.getFrom().getId(), true);
            statusUpdated(message.getChatId(), true, locale);
        } else if (disableCommand.equals(text)) {
            userSettingsService.smartFileFeature(message.getFrom().getId(), false);
            statusUpdated(message.getChatId(), false, locale);
        }
    }

    @Override
    public String getCommandIdentifier() {
        return SmartWorkCommandNames.SMART_FILE_FEATURE;
    }

    @Override
    public String getParentCommandName(long chatId) {
        return CommandNames.START_COMMAND_NAME;
    }

    @Override
    public String getHistoryName() {
        return SmartWorkCommandNames.SMART_FILE_FEATURE;
    }

    private void statusUpdated(long chatId, boolean enabled, Locale locale) {
        CommandNavigator.SilentPop silentPop = commandNavigator.silentPop(chatId);
        messageService.sendMessage(
                SendMessage.builder()
                        .chatId(String.valueOf(chatId))
                        .text(getSmartFileFeatureUpdatedMessage(enabled, locale))
                        .replyMarkup(silentPop.getReplyKeyboardMarkup())
                        .parseMode(ParseMode.HTML)
                        .build()
        );
    }

    private String getSmartFileFeatureUpdatedMessage(boolean enabled, Locale locale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_SMART_FILE_FEATURE, new Object[]{
                localisationService.getMessage(enabled ? MessagesProperties.MESSAGE_ENABLED : MessagesProperties.MESSAGE_DISABLED, locale)
        }, locale);
    }

    private String getSmartFileFeatureMessage(boolean enabled, Locale locale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_SMART_FILE_FEATURE, new Object[]{
                localisationService.getMessage(enabled ? MessagesProperties.MESSAGE_ENABLED : MessagesProperties.MESSAGE_DISABLED, locale)
        }, locale) + "\n\n" + localisationService.getMessage(MessagesProperties.MESSAGE_SMART_FILE_FEATURE_CHOOSE_STATUS, locale);
    }
}
