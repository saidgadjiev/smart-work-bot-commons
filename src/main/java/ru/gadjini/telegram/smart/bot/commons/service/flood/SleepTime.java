package ru.gadjini.telegram.smart.bot.commons.service.flood;

import java.time.LocalDateTime;

class SleepTime {
    private final LocalDateTime startedAt;

    private final long sleepTime;

    SleepTime(LocalDateTime startedAt, long sleepTime) {
        this.startedAt = startedAt;
        this.sleepTime = sleepTime;
    }

    long getSleepTime() {
        return sleepTime;
    }

    LocalDateTime getStartedAt() {
        return startedAt;
    }
}
