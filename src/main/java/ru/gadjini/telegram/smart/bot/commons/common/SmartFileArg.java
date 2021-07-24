package ru.gadjini.telegram.smart.bot.commons.common;

public enum SmartFileArg {

    STATE("stt"),
    GO_BACK("sfgb");

    private String key;

    SmartFileArg(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
