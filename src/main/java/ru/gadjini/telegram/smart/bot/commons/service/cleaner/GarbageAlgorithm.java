package ru.gadjini.telegram.smart.bot.commons.service.cleaner;

import java.io.File;

public interface GarbageAlgorithm {

    boolean accept(File file);

    boolean isGarbage(File file);
}
