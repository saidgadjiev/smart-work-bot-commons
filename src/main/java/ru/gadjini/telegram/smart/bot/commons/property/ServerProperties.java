package ru.gadjini.telegram.smart.bot.commons.property;

import org.springframework.boot.context.properties.ConfigurationProperties;
import ru.gadjini.telegram.smart.bot.commons.configuration.SmartBotConfiguration;

import java.util.Map;

@ConfigurationProperties("server")
public class ServerProperties {

    private int number = SmartBotConfiguration.PRIMARY_SERVER_NUMBER;

    private Map<Integer, String> servers;

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public Map<Integer, String> getServers() {
        return servers;
    }

    public void setServers(Map<Integer, String> servers) {
        this.servers = servers;
    }

    public String getPrimaryServer() {
        return servers.get(SmartBotConfiguration.PRIMARY_SERVER_NUMBER);
    }

    public String getServer(int number) {
        return servers.get(number);
    }

    public boolean isSecondaryServer(int serverNumber) {
        return serverNumber == SmartBotConfiguration.PRIMARY_SERVER_NUMBER;
    }

    public boolean isPrimaryServer(int serverNumber) {
        return serverNumber == SmartBotConfiguration.PRIMARY_SERVER_NUMBER;
    }

    public boolean isPrimaryServer() {
        return number == SmartBotConfiguration.PRIMARY_SERVER_NUMBER;
    }

    public boolean isMe(int serverNumber) {
        return number == serverNumber;
    }
}
