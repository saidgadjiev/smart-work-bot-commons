package ru.gadjini.telegram.smart.bot.commons.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.job.WorkQueueJob;

@Component
public class UserBlockedEventListener {

    private WorkQueueJob workQueueJob;

    @Autowired
    public UserBlockedEventListener(WorkQueueJob workQueueJob) {
        this.workQueueJob = workQueueJob;
    }

    @EventListener(UserBlockedEvent.class)
    public void userBlocked(UserBlockedEvent event) {
        workQueueJob.cancelCurrentTasks(event.getUserId());
    }
}
