package ru.gadjini.telegram.smart.bot.commons.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties("server")
public class ServerProperties {

    private static final int PRIMARY_SERVER_NUMBER = 1;

    private int number = PRIMARY_SERVER_NUMBER;

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
        return servers.get(PRIMARY_SERVER_NUMBER);
    }

    public String getServer(int number) {
        return servers.get(number);
    }

    public boolean isSecondaryServer(int serverNumber) {
        return serverNumber == PRIMARY_SERVER_NUMBER;
    }

    public boolean isPrimaryServer(int serverNumber) {
        return serverNumber == PRIMARY_SERVER_NUMBER;
    }

    public boolean isPrimaryServer() {
        return number == PRIMARY_SERVER_NUMBER;
    }

    public boolean isMe(int serverNumber) {
        return number == serverNumber;
    }
}
