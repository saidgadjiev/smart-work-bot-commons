package ru.gadjini.telegram.smart.bot.commons.command.impl.smart.file.state;

import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.telegram.smart.bot.commons.command.impl.smart.file.SmartFileCommandState;
import ru.gadjini.telegram.smart.bot.commons.common.SmartWorkCommandNames;
import ru.gadjini.telegram.smart.bot.commons.service.command.CommandStateService;
import ru.gadjini.telegram.smart.bot.commons.service.command.navigator.CommandNavigator;

public abstract class BaseSmartFileState implements SmartFileState {

    private SmartFileFatherState fatherState;

    private CommandNavigator commandNavigator;

    private CommandStateService commandStateService;

    public CommandNavigator getCommandNavigator() {
        return commandNavigator;
    }

    public CommandStateService getCommandStateService() {
        return commandStateService;
    }

    public SmartFileFatherState getFatherState() {
        return fatherState;
    }

    @Autowired
    public final void setCommandStateService(CommandStateService commandStateService) {
        this.commandStateService = commandStateService;
    }

    @Autowired
    public final void setFatherState(SmartFileFatherState fatherState) {
        this.fatherState = fatherState;
    }

    @Autowired
    public final void setCommandNavigator(CommandNavigator commandNavigator) {
        this.commandNavigator = commandNavigator;
    }

    @Override
    public final void goBack(CallbackQuery callbackQuery, SmartFileCommandState currentState) {
        fatherState.enter(callbackQuery, currentState);
        currentState.setStateName(SmartFileStateName.FATHER);
        currentState.setPrevCommand(null);
        silentPop(callbackQuery.getFrom().getId());
        commandStateService.setState(callbackQuery.getFrom().getId(), SmartWorkCommandNames.SMART_FILE_COMMAND, currentState);
    }

    protected final void silentPop(long userId) {
        String currentCommandName = commandNavigator.getCurrentCommandName(userId);
        if (SmartWorkCommandNames.SMART_FILE_COMMAND.equals(currentCommandName)) {
            commandNavigator.silentPopFromCallback(userId);
        }
    }
}
