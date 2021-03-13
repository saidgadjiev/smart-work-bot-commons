package ru.gadjini.telegram.smart.bot.commons.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("bot.api")
public class BotApiProperties {

    private String endpoint;

    private String workDir;

    private String localWorkDir;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getWorkDir() {
        return workDir;
    }

    public void setWorkDir(String workDir) {
        this.workDir = workDir;
    }

    public String getLocalWorkDir() {
        return localWorkDir;
    }

    public void setLocalWorkDir(String localWorkDir) {
        this.localWorkDir = localWorkDir;
    }
}
