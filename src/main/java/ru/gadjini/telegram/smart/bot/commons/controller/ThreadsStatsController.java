package ru.gadjini.telegram.smart.bot.commons.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.gadjini.telegram.smart.bot.commons.common.SmartWorkProfiles;
import ru.gadjini.telegram.smart.bot.commons.service.ThreadsStatsService;
import ru.gadjini.telegram.smart.bot.commons.service.TokenValidator;

@RestController
@RequestMapping("/threads/stats")
@Profile({SmartWorkProfiles.PROFILE_DEV_SECONDARY, SmartWorkProfiles.PROFILE_PROD_SECONDARY})
public class ThreadsStatsController {

    private TokenValidator tokenValidator;

    private ThreadsStatsService threadsStatsService;

    @Autowired
    public ThreadsStatsController(TokenValidator tokenValidator, ThreadsStatsService threadsStatsService) {
        this.tokenValidator = tokenValidator;
        this.threadsStatsService = threadsStatsService;
    }

    @GetMapping
    public ResponseEntity<?> threadsStats(@RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        if (tokenValidator.isInvalid(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(threadsStatsService.threadsStats());
    }
}
