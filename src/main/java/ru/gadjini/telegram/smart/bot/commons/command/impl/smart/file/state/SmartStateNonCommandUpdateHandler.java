package ru.gadjini.telegram.smart.bot.commons.command.impl.smart.file.state;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.telegram.smart.bot.commons.command.api.NavigableBotCommand;
import ru.gadjini.telegram.smart.bot.commons.command.impl.smart.file.SmartFileCommandState;
import ru.gadjini.telegram.smart.bot.commons.common.CommandNames;
import ru.gadjini.telegram.smart.bot.commons.common.SmartWorkCommandNames;
import ru.gadjini.telegram.smart.bot.commons.service.command.CommandStateService;
import ru.gadjini.telegram.smart.bot.commons.service.command.navigator.CommandNavigator;

import java.util.Set;

@Component
public class SmartStateNonCommandUpdateHandler implements NavigableBotCommand {

    private Set<SmartFileState> smartFileStates;

    private CommandStateService commandStateService;

    private CommandNavigator commandNavigator;

    @Autowired
    public SmartStateNonCommandUpdateHandler(CommandStateService commandStateService) {
        this.commandStateService = commandStateService;
    }

    @Autowired
    public void setCommandNavigator(CommandNavigator commandNavigator) {
        this.commandNavigator = commandNavigator;
    }

    @Autowired
    public void setSmartFileStates(Set<SmartFileState> smartFileStates) {
        this.smartFileStates = smartFileStates;
    }

    @Override
    public String getParentCommandName(long l) {
        SmartFileCommandState state = commandStateService.getState(l,
                SmartWorkCommandNames.SMART_FILE_COMMAND, false, SmartFileCommandState.class);

        return StringUtils.defaultIfBlank(state == null ? null : state.getPrevCommand(), CommandNames.START_COMMAND_NAME);
    }

    @Override
    public String getHistoryName() {
        return SmartWorkCommandNames.SMART_FILE_COMMAND;
    }

    @Override
    public void processNonCommandUpdate(Message message, String text) {
        SmartFileCommandState state = commandStateService.getState(message.getFrom().getId(),
                SmartWorkCommandNames.SMART_FILE_COMMAND, false, SmartFileCommandState.class);
        if (state == null) {
            commandNavigator.silentPop(message.getChatId());
        } else {
            getState(state.getStateName()).update(message, text, state);

        }
    }

    private SmartFileState getState(SmartFileStateName stateName) {
        return smartFileStates.stream().filter(f -> f.getName().equals(stateName)).findFirst().orElseThrow();
    }
}
