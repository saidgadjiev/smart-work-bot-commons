package ru.gadjini.telegram.smart.bot.commons.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.gadjini.telegram.smart.bot.commons.job.WorkQueueJob;
import ru.gadjini.telegram.smart.bot.commons.service.TokenValidator;

@RestController
@RequestMapping("/user/{userId}/tasks")
public class TasksController {

    private TokenValidator tokenValidator;

    private WorkQueueJob workQueueJob;

    @Autowired
    public TasksController(TokenValidator tokenValidator, WorkQueueJob workQueueJob) {
        this.tokenValidator = tokenValidator;
        this.workQueueJob = workQueueJob;
    }

    @PostMapping("/{taskId}/cancel")
    public ResponseEntity<?> cancel(@PathVariable("taskId") int taskId, @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        if (tokenValidator.isInvalid(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        workQueueJob.cancel(taskId);

        return ResponseEntity.ok().build();
    }
}
