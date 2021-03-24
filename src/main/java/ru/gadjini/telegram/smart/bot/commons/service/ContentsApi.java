package ru.gadjini.telegram.smart.bot.commons.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.gadjini.telegram.smart.bot.commons.io.SmartTempFile;
import ru.gadjini.telegram.smart.bot.commons.model.DeleteContentRequest;
import ru.gadjini.telegram.smart.bot.commons.property.AuthProperties;
import ru.gadjini.telegram.smart.bot.commons.property.ServerProperties;

@Service
public class ContentsApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContentsApi.class);

    private ServerProperties serverProperties;

    private AuthProperties authProperties;

    private RestTemplate restTemplate;

    @Autowired
    public ContentsApi(ServerProperties serverProperties, AuthProperties authProperties, RestTemplate restTemplate) {
        this.serverProperties = serverProperties;
        this.authProperties = authProperties;
        this.restTemplate = restTemplate;
    }

    public void delete(SmartTempFile tempFile) {
        try {
            DeleteContentRequest deleteContentRequest = new DeleteContentRequest(tempFile.getAbsolutePath(), tempFile.isDeleteParentDir());
            HttpEntity<DeleteContentRequest> entity = new HttpEntity<>(deleteContentRequest, authHeaders());

            ResponseEntity<Void> response = restTemplate.exchange(buildDeleteContentUrl(), HttpMethod.DELETE, entity, Void.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                LOGGER.error("Error delete content({})", tempFile.getAbsolutePath());
            }
        } catch (Exception e) {
            LOGGER.error("Error delete content(" + tempFile.getAbsolutePath() + ") \n" + e.getMessage(), e);
        }
    }

    private HttpHeaders authHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.AUTHORIZATION, authProperties.getToken());

        return httpHeaders;
    }

    private String buildDeleteContentUrl() {
        return UriComponentsBuilder.fromHttpUrl(serverProperties.getPrimaryServer())
                .path("/contents")
                .build()
                .toUriString();
    }
}
