package ru.gadjini.telegram.smart.bot.commons.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.property.AuthProperties;

@Service
public class TokenValidator {

    private AuthProperties authProperties;

    @Autowired
    public TokenValidator(AuthProperties authProperties) {
        this.authProperties = authProperties;
    }

    public boolean isInvalid(String token) {
        return !authProperties.getToken().equals(token);
    }
}
