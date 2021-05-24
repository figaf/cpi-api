package com.figaf.integration.cpi.client;

import com.figaf.integration.common.exception.ClientIntegrationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.singletonList;
import static org.springframework.http.HttpStatus.OK;

/**
 * @author Klochkov Sergey
 */
@Slf4j
public class CsrfTokenHolder {

    private static final String X_CSRF_TOKEN = "X-CSRF-Token";

    private static final Map<String, String> keyToCsrfTokenMap = new ConcurrentHashMap<>();

    public String getCsrfToken(String key, RestTemplate restTemplate, String url) {
        return keyToCsrfTokenMap.computeIfAbsent(key, k -> retrieveCsRfToken(restTemplate, url));
    }

    public String getAndSaveNewCsrfTokenIfNeed(
        String key,
        RestTemplate restTemplate,
        String url,
        String oldCsrfToken
    ) {
        synchronized (key.intern()) {
            String csrfToken = keyToCsrfTokenMap.computeIfAbsent(key, k -> retrieveCsRfToken(restTemplate, url));
            if (!csrfToken.equals(oldCsrfToken)) {
                return csrfToken;
            }
            csrfToken = retrieveCsRfToken(restTemplate, url);
            keyToCsrfTokenMap.put(key, csrfToken);
            return csrfToken;
        }
    }

    public void deleteCsrfToken(String key) {
        keyToCsrfTokenMap.remove(key);
    }

    private String retrieveCsRfToken(RestTemplate restTemplate, String url) {
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.put(X_CSRF_TOKEN, singletonList("Fetch"));
            RequestEntity<String> retrieveCsRfTokenRequest = new RequestEntity<>(httpHeaders, HttpMethod.HEAD, new URI(url));
            ResponseEntity<String> responseEntity = restTemplate.exchange(retrieveCsRfTokenRequest, String.class);

            if (OK != responseEntity.getStatusCode()) {
                throw new ClientIntegrationException(String.format(
                    "Couldn't fetch csrf token: Code: %d, Message: %s",
                    responseEntity.getStatusCode().value(),
                    responseEntity.getBody()
                ));
            }
            return responseEntity.getHeaders().getFirst(X_CSRF_TOKEN);
        } catch (Exception ex) {
            log.error("Can't retrieve csrf token: ", ex);
            throw new ClientIntegrationException("Can't retrieve csrf token: " + ex.getMessage(), ex);
        }
    }

}
