package ru.gadjini.telegram.smart.bot.commons.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashSet;
import java.util.Set;

@ConfigurationProperties("admin")
public class AdminProperties {
    
    private Set<Long> whiteList = new HashSet<>();

    public Set<Long> getWhiteList() {
        return whiteList;
    }

    public void setWhiteList(Set<Long> whiteList) {
        this.whiteList = whiteList;
    }
}
