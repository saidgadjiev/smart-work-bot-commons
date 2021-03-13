package ru.gadjini.telegram.smart.bot.commons.property;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("file.limit")
public class FileLimitProperties {

    @Value("${light.file.max.weight:104857600}")
    private long lightFileMaxWeight;

    public long getLightFileMaxWeight() {
        return lightFileMaxWeight;
    }

    public void setLightFileMaxWeight(long lightFileMaxWeight) {
        this.lightFileMaxWeight = lightFileMaxWeight;
    }
}
