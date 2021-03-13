package ru.gadjini.telegram.smart.bot.commons.utils;

import java.util.function.Supplier;

public class ThreadUtils {

    private ThreadUtils() {

    }

    public static void sleep(int sleepTime, Supplier<RuntimeException> exceptionSupplier) {
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            throw exceptionSupplier.get();
        }
    }
}
