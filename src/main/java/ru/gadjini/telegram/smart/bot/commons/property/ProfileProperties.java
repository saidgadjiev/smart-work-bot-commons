package ru.gadjini.telegram.smart.bot.commons.property;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@ConfigurationProperties
public class ProfileProperties {

    @Value("${spring.profiles.active}")
    private String active;

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public boolean isActive(String ... profiles) {
        return Set.of(profiles).contains(active);
    }
}
