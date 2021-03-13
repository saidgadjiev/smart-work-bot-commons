package ru.gadjini.telegram.smart.bot.commons.service.cleaner;

import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.utils.SmartFileUtils;

import java.io.File;

@Component
public class G1 implements GarbageAlgorithm {

    @Override
    public boolean accept(File file) {
        return true;
    }

    @Override
    public boolean isGarbage(File file) {
        return SmartFileUtils.isExpired(file, 1);
    }
}
