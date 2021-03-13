package ru.gadjini.telegram.smart.bot.commons.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.gadjini.telegram.smart.bot.commons.domain.ThreadsStats;
import ru.gadjini.telegram.smart.bot.commons.property.AuthProperties;
import ru.gadjini.telegram.smart.bot.commons.property.ServerProperties;

@Service
public class ThreadsStatsApi {

    private ServerProperties serverProperties;

    private AuthProperties authProperties;

    private RestTemplate restTemplate = new RestTemplate();

    @Autowired
    public ThreadsStatsApi(ServerProperties serverProperties, AuthProperties authProperties) {
        this.serverProperties = serverProperties;
        this.authProperties = authProperties;
    }

    public ThreadsStats threadsStats(int serverNumber) {
        ResponseEntity<ThreadsStats> response = restTemplate.exchange(buildThreadsStatsUrl(serverProperties.getServer(serverNumber)),
                HttpMethod.GET, new HttpEntity<>(authHeaders()), ThreadsStats.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            return new ThreadsStats();
        }

        return response.getBody();
    }

    private HttpHeaders authHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.AUTHORIZATION, authProperties.getToken());

        return httpHeaders;
    }

    private String buildThreadsStatsUrl(String server) {
        return UriComponentsBuilder.fromHttpUrl(server)
                .path("/threads/stats")
                .build()
                .toUriString();
    }
}
