package ru.gadjini.telegram.smart.bot.commons.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.gadjini.telegram.smart.bot.commons.configuration.SmartBotConfiguration;
import ru.gadjini.telegram.smart.bot.commons.io.SmartTempFile;
import ru.gadjini.telegram.smart.bot.commons.model.DeleteContentRequest;
import ru.gadjini.telegram.smart.bot.commons.service.TokenValidator;
import ru.gadjini.telegram.smart.bot.commons.service.file.temp.TempFileService;

import java.io.File;

@RestController
@RequestMapping("/contents")
@Profile({SmartBotConfiguration.PROFILE_DEV_PRIMARY, SmartBotConfiguration.PROFILE_PROD_PRIMARY})
public class ContentsController {

    private TempFileService tempFileService;

    private TokenValidator tokenValidator;

    @Autowired
    public ContentsController(TempFileService tempFileService, TokenValidator tokenValidator) {
        this.tempFileService = tempFileService;
        this.tokenValidator = tokenValidator;
    }

    @DeleteMapping
    public ResponseEntity<?> delete(@RequestHeader(HttpHeaders.AUTHORIZATION) String token, @RequestBody DeleteContentRequest deleteContentRequest) {
        if (tokenValidator.isInvalid(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (StringUtils.isBlank(deleteContentRequest.getFilePath())) {
            return ResponseEntity.badRequest().build();
        }
        SmartTempFile smartTempFile = new SmartTempFile(new File(deleteContentRequest.getFilePath()), deleteContentRequest.isDeleteParentDir());
        tempFileService.delete(smartTempFile);

        return ResponseEntity.ok().build();
    }
}
