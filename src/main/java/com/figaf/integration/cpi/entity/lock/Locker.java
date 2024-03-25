package com.figaf.integration.cpi.entity.lock;

import com.figaf.integration.common.entity.ConnectionProperties;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.cpi.entity.lock.exception.CpiEntityIsLockedException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

import static org.springframework.http.HttpStatus.OK;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Locker {
    private static final String X_CSRF_TOKEN = "X-CSRF-Token";

    public static void lockPackage(ConnectionProperties connectionProperties, String externalPackageId, String csrfToken, RestTemplate restTemplate) {
        try {
            lockOrUnlockPackage(connectionProperties, externalPackageId, "LOCK", true, false, csrfToken, restTemplate);
        } catch (HttpClientErrorException ex) {
            if (HttpStatus.LOCKED.equals(ex.getStatusCode())) {
                LockInfo lockInfo = Locker.requestLockInfoForPackage(connectionProperties, externalPackageId, csrfToken, restTemplate);
                if (lockInfo.isLocked() && !lockInfo.isCurrentUserHasLock()) {
                    throw new CpiEntityIsLockedException(String.format("Locked by another user: %s", lockInfo.getLockedBy()), lockInfo, externalPackageId, true);
                }
                throw new CpiEntityIsLockedException(String.format("Can't lock a package. Lock info: %s", lockInfo), ex, lockInfo, externalPackageId, true);
            } else {
                throw new ClientIntegrationException(String.format("Can't lock a package: %s", ex.getResponseBodyAsString()));
            }
        }
    }

    public static void unlockPackage(ConnectionProperties connectionProperties, String externalPackageId, String csrfToken, RestTemplate restTemplate) {
        lockOrUnlockPackage(connectionProperties, externalPackageId, "UNLOCK", false, false, csrfToken, restTemplate);
    }

    public static void lockCpiObject(
        ConnectionProperties connectionProperties,
        String packageExternalId,
        String artifactExternalId,
        String userApiCsrfToken,
        RestTemplate restTemplate,
        String urlOfLockOrUnlockCpiObject
    ) {
        try {
            lockOrUnlockCpiObject(connectionProperties, packageExternalId, artifactExternalId, "LOCK", false, userApiCsrfToken, restTemplate, urlOfLockOrUnlockCpiObject);
        } catch (HttpClientErrorException ex) {
            if (HttpStatus.LOCKED.equals(ex.getStatusCode())) {
                LockInfo lockInfo = Locker.requestLockInfoForCpiObject(connectionProperties, packageExternalId, artifactExternalId, userApiCsrfToken, restTemplate, urlOfLockOrUnlockCpiObject);
                if (lockInfo.isLocked() && !lockInfo.isCurrentUserHasLock()) {
                    throw new CpiEntityIsLockedException(String.format("Locked by another user: %s", lockInfo.getLockedBy()), lockInfo, artifactExternalId, false);
                }
                if (lockInfo.isLocked() && lockInfo.isCurrentUserHasLock() && !lockInfo.isCurrentSessionHasLock()) {
                    throw new CpiEntityIsLockedException(String.format("Locked by current user (%s) but in another session", lockInfo.getLockedBy()), lockInfo, artifactExternalId, false);
                }
                throw new CpiEntityIsLockedException(String.format("Can't lock an artifact. Lock info: %s", lockInfo), ex, lockInfo, artifactExternalId, false);
            } else {
                throw new ClientIntegrationException(String.format("Can't lock an artifact: %s", ex.getResponseBodyAsString()));
            }
        }
    }

    public static void unlockCpiObject(
        ConnectionProperties connectionProperties,
        String packageExternalId,
        String artifactExternalId,
        String userApiCsrfToken,
        RestTemplate restTemplate,
        String urlOfLockOrUnlockCpiObject
    ) {
        lockOrUnlockCpiObject(connectionProperties, packageExternalId, artifactExternalId, "UNLOCK", false, userApiCsrfToken, restTemplate, urlOfLockOrUnlockCpiObject);
    }

    private static LockInfo requestLockInfoForCpiObject(
        ConnectionProperties connectionProperties,
        String packageExternalId,
        String artifactExternalId,
        String userApiCsrfToken,
        RestTemplate restTemplate,
        String urlOfLockOrUnlockCpiObject
    ) {
        String lockInfoString = lockOrUnlockCpiObject(connectionProperties, packageExternalId, artifactExternalId, "LOCK", true, userApiCsrfToken, restTemplate, urlOfLockOrUnlockCpiObject);
        return getLockInfo(lockInfoString);
    }

    private static LockInfo requestLockInfoForPackage(
        ConnectionProperties connectionProperties,
        String externalPackageId,
        String csrfToken,
        RestTemplate restTemplate
    ) {
        String lockInfoString = lockOrUnlockPackage(connectionProperties, externalPackageId, "LOCK", true, true, csrfToken, restTemplate);
        return getLockInfo(lockInfoString);
    }

    private static LockInfo getLockInfo(String lockInfoString) {
        JSONObject jsonObject = new JSONObject(lockInfoString);
        LockInfo lockInfo = new LockInfo();
        lockInfo.setCurrentSessionHasLock(jsonObject.optBoolean("isCurrentSessionHasLock"));
        lockInfo.setCurrentUserHasLock(jsonObject.optBoolean("isCurrentUserHasLock"));
        lockInfo.setLocked(jsonObject.optBoolean("isLocked"));
        lockInfo.setPublishToCatalogAllowed(jsonObject.optBoolean("isPublishToCatalogAllowed"));
        lockInfo.setResourceEditOpted(jsonObject.optBoolean("isResourceEditOpted"));
        lockInfo.setLockedBy(jsonObject.optString("lockedBy"));
        lockInfo.setLockedTime(jsonObject.optString("lockedTime"));
        return lockInfo;
    }

    private static String lockOrUnlockCpiObject(
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

        uriBuilder.queryParam("webdav", webdav);
        if (lockinfo) {
            uriBuilder.queryParam("lockinfo", "true");
        }

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
            throw new ClientIntegrationException("Couldn't lock or unlock cpiObject\n" + responseEntity.getBody());
        }

        return responseEntity.getBody();
    }

    private static String lockOrUnlockPackage(ConnectionProperties connectionProperties, String externalPackageId, String webdav, boolean forceLock, boolean lockinfo, String csrfToken, RestTemplate restTemplate) {
        log.debug("#lockOrUnlockPackage(ConnectionProperties connectionProperties, String externalPackageId, String webdav, boolean forceLock, boolean lockinfo, String csrfToken, RestTemplate restTemplate): " +
            "{}, {}, {}, {}, {}", connectionProperties, externalPackageId, webdav, forceLock, lockinfo);

        Assert.notNull(connectionProperties, "connectionProperties must be not null!");
        Assert.notNull(externalPackageId, "externalPackageId must be not null!");
        Assert.notNull(csrfToken, "csrfToken must be not null!");
        Assert.notNull(restTemplate, "restTemplate must be not null!");

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
        if (lockinfo) {
            uriBuilder.queryParam("lockinfo", true);
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

        return responseEntity.getBody();
    }
}
