package ru.gadjini.telegram.smart.bot.commons.service.cleaner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.utils.SmartFileUtils;

import javax.annotation.PostConstruct;
import java.io.File;

@Component
public class G1 implements GarbageAlgorithm {

    private static final Logger LOGGER = LoggerFactory.getLogger(G1.class);

    @Value("${g1.expire.days:2}")
    private int expireDays;

    @PostConstruct
    public void init() {
        LOGGER.debug("G1 expire days({})", expireDays);
    }

    @Override
    public boolean accept(File file) {
        return true;
    }

    @Override
    public boolean isGarbage(File file) {
        return SmartFileUtils.isExpired(file, expireDays);
    }
}
