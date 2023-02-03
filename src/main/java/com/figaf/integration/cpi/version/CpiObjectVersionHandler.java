package com.figaf.integration.cpi.version;

import com.figaf.integration.common.entity.ConnectionProperties;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.http.HttpStatus.OK;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CpiObjectVersionHandler {
    private static final String X_CSRF_TOKEN = "X-CSRF-Token";

    public static void setVersionToCpiObject(
        ConnectionProperties connectionProperties,
        String packageExternalId,
        String artifactExternalId,
        String version,
        String userApiCsrfToken,
        String comment,
        RestTemplate restTemplate,
        String updateUrl
    ) {
        try {

            UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance()
                .scheme(connectionProperties.getProtocol())
                .host(connectionProperties.getHost())
                .path(updateUrl)
                .queryParam("notifications", "true")
                .queryParam("webdav", "CHECKIN");

            if (StringUtils.isNotEmpty(connectionProperties.getPort())) {
                uriBuilder.port(connectionProperties.getPort());
            }

            URI lockOrUnlockArtifactUri = uriBuilder
                .buildAndExpand(packageExternalId, artifactExternalId)
                .encode()
                .toUri();

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("comment", !isBlank(comment) ? comment : "");
            requestBody.put("semanticVersion", version);

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add(X_CSRF_TOKEN, userApiCsrfToken);
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, httpHeaders);

            ResponseEntity<String> responseEntity = restTemplate.exchange(
                lockOrUnlockArtifactUri,
                HttpMethod.PUT,
                entity,
                String.class
            );

            if (!OK.equals(responseEntity.getStatusCode())) {
                throw new RuntimeException("Couldn't set version to CpiObject:\n" + responseEntity.getBody());

            }

        } catch (Exception ex) {
            throw new RuntimeException("Error occurred while setting version CpiObject: " + ex.getMessage(), ex);
        }
    }
}
