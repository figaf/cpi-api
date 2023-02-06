package com.figaf.integration.cpi.entity.lock;

import com.figaf.integration.common.entity.ConnectionProperties;
import com.figaf.integration.common.exception.ClientIntegrationException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.*;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

import static org.springframework.http.HttpStatus.OK;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Locker {
    private static final String X_CSRF_TOKEN = "X-CSRF-Token";

    public static void lockPackage(ConnectionProperties connectionProperties, String externalPackageId, String csrfToken, RestTemplate restTemplate, boolean forceLock) {
        lockOrUnlockPackage(connectionProperties, externalPackageId, "LOCK", forceLock, csrfToken, restTemplate);
    }

    public static void unlockPackage(ConnectionProperties connectionProperties, String externalPackageId, String csrfToken, RestTemplate restTemplate) {
        lockOrUnlockPackage(connectionProperties, externalPackageId, "UNLOCK", false, csrfToken, restTemplate);
    }

    public static void lockOrUnlockCpiObject(
        ConnectionProperties connectionProperties,
        String packageExternalId,
        String artifactExternalId,
        String webdav,
        boolean lockinfo,
        String userApiCsrfToken,
        RestTemplate restTemplate,
        String urlOfLockOrUnlockCpiObject
    ) {

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance()
            .scheme(connectionProperties.getProtocol())
            .host(connectionProperties.getHost())
            .path(urlOfLockOrUnlockCpiObject);
        if (lockinfo) {
            uriBuilder.queryParam("lockinfo", "true");
        }
        uriBuilder.queryParam("webdav", webdav);

        if (StringUtils.isNotEmpty(connectionProperties.getPort())) {
            uriBuilder.port(connectionProperties.getPort());
        }

        URI lockOrUnlockArtifactUri = uriBuilder
            .buildAndExpand(packageExternalId, artifactExternalId)
            .encode()
            .toUri();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(X_CSRF_TOKEN, userApiCsrfToken);

        HttpEntity<Void> requestEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(
            lockOrUnlockArtifactUri,
            HttpMethod.PUT,
            requestEntity,
            String.class
        );

        if (!OK.equals(responseEntity.getStatusCode())) {
            throw new RuntimeException("Couldn't lock or unlock cpiObject\n" + responseEntity.getBody());
        }
    }

    private static void lockOrUnlockPackage(ConnectionProperties connectionProperties, String externalPackageId, String webdav, boolean forceLock, String csrfToken, RestTemplate restTemplate) {
        log.debug("#lockOrUnlockPackage(ConnectionProperties connectionProperties, String externalPackageId, String webdav, boolean forceLock, String csrfToken, RestTemplate restTemplate): " +
            "{}, {}, {}, {}", connectionProperties, externalPackageId, webdav, forceLock);

        Assert.notNull(connectionProperties, "connectionProperties must be not null!");
        Assert.notNull(externalPackageId, "externalPackageId must be not null!");
        Assert.notNull(csrfToken, "csrfToken must be not null!");
        Assert.notNull(restTemplate, "restTemplate must be not null!");

        try {

            UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance()
                .scheme(connectionProperties.getProtocol())
                .host(connectionProperties.getHost())
                .path("/itspaces/api/1.0/workspace/{0}");

            if (StringUtils.isNotEmpty(connectionProperties.getPort())) {
                uriBuilder.port(connectionProperties.getPort());
            }

            uriBuilder.queryParam("webdav", webdav);
            if (forceLock) {
                uriBuilder.queryParam("forcelock", true);
            }

            URI uri = uriBuilder
                .buildAndExpand(externalPackageId)
                .encode()
                .toUri();

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add(X_CSRF_TOKEN, csrfToken);

            HttpEntity<Void> requestEntity = new HttpEntity<>(null, httpHeaders);

            ResponseEntity<String> responseEntity = restTemplate.exchange(
                uri,
                HttpMethod.PUT,
                requestEntity,
                String.class
            );

            if (!HttpStatus.OK.equals(responseEntity.getStatusCode())) {
                throw new ClientIntegrationException(String.format(
                    "Couldn't lock or unlock package %s: Code: %d, Message: %s",
                    externalPackageId,
                    responseEntity.getStatusCode().value(),
                    responseEntity.getBody())
                );
            }

        } catch (Exception ex) {
            log.error("Error occurred while locking package: " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while locking package: " + ex.getMessage(), ex);
        }
    }
}
