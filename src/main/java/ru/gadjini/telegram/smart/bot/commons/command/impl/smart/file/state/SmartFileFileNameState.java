package ru.gadjini.telegram.smart.bot.commons.command.impl.smart.file.state;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.telegram.smart.bot.commons.annotation.TgMessageLimitsControl;
import ru.gadjini.telegram.smart.bot.commons.command.impl.smart.file.SmartFileCommandState;
import ru.gadjini.telegram.smart.bot.commons.common.SmartWorkCommandNames;
import ru.gadjini.telegram.smart.bot.commons.exception.UserException;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.command.CommandStateService;
import ru.gadjini.telegram.smart.bot.commons.service.keyboard.smart.SmartFileInlineKeyboardService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.bot.commons.service.message.SmartWorkMessageProperties;
import ru.gadjini.telegram.smart.bot.commons.service.message.smart.SmartFileMessageBuilder;
import ru.gadjini.telegram.smart.bot.commons.service.queue.UploadQueueService;
import ru.gadjini.telegram.smart.bot.commons.service.utils.SmartFileFeatureUtils;

import java.util.Locale;

@Component
public class SmartFileFileNameState extends BaseSmartFileState {

    private static final int MAX_LENGTH = 256;

    private MessageService messageService;

    private SmartFileInlineKeyboardService smartFileInlineKeyboardService;

    private UserService userService;

    private SmartStateNonCommandUpdateHandler nonCommandUpdateHandler;

    private CommandStateService commandStateService;

    private UploadQueueService uploadQueueService;

    private SmartFileMessageBuilder messageBuilder;

    private LocalisationService localisationService;

    @Autowired
    public SmartFileFileNameState(@TgMessageLimitsControl MessageService messageService,
                                  SmartFileInlineKeyboardService smartFileInlineKeyboardService,
                                  CommandStateService commandStateService, UploadQueueService uploadQueueService,
                                  SmartFileMessageBuilder messageBuilder, LocalisationService localisationService) {
        this.messageService = messageService;
        this.smartFileInlineKeyboardService = smartFileInlineKeyboardService;
        this.commandStateService = commandStateService;
        this.uploadQueueService = uploadQueueService;
        this.messageBuilder = messageBuilder;
        this.localisationService = localisationService;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setNonCommandUpdateHandler(SmartStateNonCommandUpdateHandler nonCommandUpdateHandler) {
        this.nonCommandUpdateHandler = nonCommandUpdateHandler;
    }

    @Override
    public SmartFileStateName getName() {
        return SmartFileStateName.FILENAME;
    }

    @Override
    public void enter(CallbackQuery callbackQuery, SmartFileCommandState currentState) {
        updateMessage(callbackQuery, currentState.getFileName());
        currentState.setPrevCommand(getCommandNavigator().getCurrentCommandName(callbackQuery.getFrom().getId()));
        commandStateService.setState(callbackQuery.getFrom().getId(), SmartWorkCommandNames.SMART_FILE_COMMAND, currentState);
        getCommandNavigator().push(callbackQuery.getFrom().getId(), nonCommandUpdateHandler);
    }

    @Override
    public void update(Message message, String text, SmartFileCommandState currentState) {
        text = validateAndGet(message, text);
        String newFileName = SmartFileFeatureUtils.createNewFileName(text, FilenameUtils.getExtension(currentState.getFileName()));
        uploadQueueService.updateFileName(currentState.getUploadId(), newFileName);
        currentState.setFileName(newFileName);
        currentState.setStateName(SmartFileStateName.FATHER);
        currentState.setPrevCommand(null);
        getFatherState().restore(message.getFrom().getId(), currentState);
        silentPop(message.getFrom().getId());
        commandStateService.setState(message.getFrom().getId(), SmartWorkCommandNames.SMART_FILE_COMMAND, currentState);
    }

    private void updateMessage(CallbackQuery callbackQuery, String fileName) {
        long userId = callbackQuery.getFrom().getId();
        int messageId = callbackQuery.getMessage().getMessageId();
        Locale locale = userService.getLocaleOrDefault(userId);
        messageService.editMessage(
                callbackQuery.getMessage().getText(),
                callbackQuery.getMessage().getReplyMarkup(),
                EditMessageText.builder()
                        .chatId(String.valueOf(userId))
                        .messageId(messageId)
                        .text(messageBuilder.buildFileNameMessage(fileName, locale))
                        .replyMarkup(smartFileInlineKeyboardService.goBackKeyboard(messageId, locale))
                        .build(),
                false
        );
    }

    private String validateAndGet(Message message, String fileName) {
        fileName = getFixedFileName(fileName);

        if (StringUtils.isNotBlank(fileName)) {
            if (fileName.length() > MAX_LENGTH) {
                throw new UserException(localisationService.getMessage(SmartWorkMessageProperties.MESSAGE_FILENAME_MAX_LENGTH,
                        new Object[]{MAX_LENGTH}, userService.getLocaleOrDefault(message.getFrom().getId())));
            }

            return fileName;
        } else {
            throw new UserException(localisationService.getMessage(SmartWorkMessageProperties.MESSAGE_SEND_FILENAME,
                    userService.getLocaleOrDefault(message.getFrom().getId())));
        }
    }

    private static String getFixedFileName(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return "";
        }
        return fileName
                .replace("\"", "")
                .replace("\\", "")
                .replace("/", "")
                .replace(";", "")
                .replace(":", "");
    }
}
