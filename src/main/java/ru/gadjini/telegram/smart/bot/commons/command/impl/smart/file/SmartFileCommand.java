package ru.gadjini.telegram.smart.bot.commons.command.impl.smart.file;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import ru.gadjini.telegram.smart.bot.commons.command.api.CallbackBotCommand;
import ru.gadjini.telegram.smart.bot.commons.command.impl.smart.file.state.SmartFileState;
import ru.gadjini.telegram.smart.bot.commons.command.impl.smart.file.state.SmartFileStateName;
import ru.gadjini.telegram.smart.bot.commons.command.impl.smart.file.state.StateRestorer;
import ru.gadjini.telegram.smart.bot.commons.common.SmartFileArg;
import ru.gadjini.telegram.smart.bot.commons.common.SmartWorkCommandNames;
import ru.gadjini.telegram.smart.bot.commons.domain.UploadQueueItem;
import ru.gadjini.telegram.smart.bot.commons.request.Arg;
import ru.gadjini.telegram.smart.bot.commons.service.command.CommandStateService;
import ru.gadjini.telegram.smart.bot.commons.service.command.navigator.CommandNavigator;
import ru.gadjini.telegram.smart.bot.commons.service.file.FileUploadUtils;
import ru.gadjini.telegram.smart.bot.commons.service.queue.UploadQueueService;
import ru.gadjini.telegram.smart.bot.commons.service.request.RequestParams;

import java.util.Set;

@Component
public class SmartFileCommand implements CallbackBotCommand {

    private UploadQueueService uploadQueueService;

    private CommandStateService commandStateService;

    private Set<SmartFileState> smartFileStates;

    private StateRestorer stateRestorer;

    private CommandNavigator commandNavigator;

    @Autowired
    public SmartFileCommand(UploadQueueService uploadQueueService, CommandStateService commandStateService,
                            StateRestorer stateRestorer, CommandNavigator commandNavigator) {
        this.uploadQueueService = uploadQueueService;
        this.commandStateService = commandStateService;
        this.stateRestorer = stateRestorer;
        this.commandNavigator = commandNavigator;
    }

    @Autowired
    public void setSmartFileStates(Set<SmartFileState> smartFileStates) {
        this.smartFileStates = smartFileStates;
    }

    @Override
    public String getName() {
        return SmartWorkCommandNames.SMART_FILE_COMMAND;
    }

    @Override
    public void processCallbackQuery(CallbackQuery callbackQuery, RequestParams requestParams) {

    }

    @Override
    public void processNonCommandCallbackQuery(CallbackQuery callbackQuery, RequestParams requestParams) {
        if (requestParams.contains(SmartFileArg.STATE.getKey())) {
            SmartFileCommandState state = commandStateService.getState(callbackQuery.getFrom().getId(),
                    getName(), false, SmartFileCommandState.class);
            int uploadId = requestParams.getInt(Arg.QUEUE_ITEM_ID.getKey());
            if (state == null || state.getUploadId() != uploadId) {
                state = createState(requestParams.get(SmartFileArg.STATE.getKey(), SmartFileStateName::valueOf),
                        uploadId, callbackQuery.getMessage().getMessageId());
                commandStateService.setState(callbackQuery.getFrom().getId(), getName(), state);
                getState(state.getStateName()).enter(callbackQuery, state);
            } else {
                getState(state.getStateName()).callbackUpdate(callbackQuery, requestParams, state);
            }
        } else if (requestParams.contains(SmartFileArg.GO_BACK.getKey())) {
            int uploadId = requestParams.getInt(Arg.QUEUE_ITEM_ID.getKey());
            SmartFileCommandState state = commandStateService.getState(callbackQuery.getFrom().getId(),
                    getName(), false, SmartFileCommandState.class, () -> {
                        commandNavigator.silentPop(callbackQuery.getFrom().getId());
                        return stateRestorer.restoreState(uploadId,
                                callbackQuery.getMessage().getMessageId());
                    });
            getState(state.getStateName()).goBack(callbackQuery, state);
        }
    }

    private SmartFileState getState(SmartFileStateName stateName) {
        return smartFileStates.stream().filter(f -> f.getName().equals(stateName)).findFirst().orElseThrow();
    }

    private SmartFileCommandState createState(SmartFileStateName stateName, int uploadId, int messageId) {
        SmartFileCommandState commandState = new SmartFileCommandState();
        commandState.setStateName(stateName);
        UploadQueueItem uploadQueueItem = uploadQueueService.getById(uploadId);
        commandState.setUploadId(uploadId);

        commandState.setMethod(uploadQueueItem.getMethod());
        commandState.setBody(uploadQueueItem.getBody());
        commandState.setCaption(FileUploadUtils.getCaption(uploadQueueItem.getMethod(), uploadQueueItem.getBody()));

        InputFile thumbFile = FileUploadUtils.getThumbFile(uploadQueueItem.getMethod(), uploadQueueItem.getBody());
        if (thumbFile != null) {
            commandState.setThumb(thumbFile.getMediaName());
        }
        commandState.setFileName(FileUploadUtils.getFileName(uploadQueueItem.getMethod(), uploadQueueItem.getBody()));

        commandState.setMessageId(messageId);

        return commandState;
    }
}
