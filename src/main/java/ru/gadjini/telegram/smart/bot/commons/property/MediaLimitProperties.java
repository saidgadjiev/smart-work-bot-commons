package ru.gadjini.telegram.smart.bot.commons.property;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("media")
public class MediaLimitProperties {

    @Value("${light.file.max.weight:52428800}")
    private long lightFileMaxWeight;

    public long getLightFileMaxWeight() {
        return lightFileMaxWeight;
    }

    public void setLightFileMaxWeight(long lightFileMaxWeight) {
        this.lightFileMaxWeight = lightFileMaxWeight;
    }
}
