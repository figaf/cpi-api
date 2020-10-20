package com.figaf.integration.cpi.client;

import com.figaf.integration.common.client.wrapper.CommonClientWrapper;
import com.figaf.integration.common.client.wrapper.RestTemplateWrapper;
import com.figaf.integration.common.entity.CommonClientWrapperEntity;
import com.figaf.integration.common.entity.ConnectionProperties;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.cpi.entity.designtime_artifacts.CreateOrUpdatePackageRequest;
import com.figaf.integration.cpi.entity.designtime_artifacts.IntegrationPackage;
import com.figaf.integration.cpi.response_parser.IntegrationPackageParser;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Arsenii Istlentev
 */
@Slf4j
public class IntegrationPackageClient extends CommonClientWrapper {

    private static final String API_PACKAGES = "/itspaces/odata/1.0/workspace.svc/ContentPackages?$format=json";

    public IntegrationPackageClient(
        String ssoUrl
    ) {
        super(ssoUrl);
    }

    public List<IntegrationPackage> getIntegrationPackages(CommonClientWrapperEntity commonClientWrapperEntity, String filter) {
        log.debug("#getIntegrationPackages(CommonClientWrapperEntity commonClientWrapperEntity, String filter): {}, {}", commonClientWrapperEntity, filter);
        String path = API_PACKAGES + (filter == null ? "" : "&$filter=" + filter.replace(" ", "%20"));
        return executeGet(commonClientWrapperEntity, path, IntegrationPackageParser::buildIntegrationPackages);
    }

    public byte[] downloadPackage(CommonClientWrapperEntity commonClientWrapperEntity, String packageTechnicalName) {
        log.debug("#downloadPackage(CommonClientWrapperEntity commonClientWrapperEntity, String packageTechnicalName): {}, {}", commonClientWrapperEntity, packageTechnicalName);

        String path = String.format("/itspaces/odata/1.0/workspace.svc/ContentPackages('%s')", packageTechnicalName);
        return executeGet(commonClientWrapperEntity, path, resolvedBody -> resolvedBody, byte[].class);
    }

    public String createIntegrationPackage(CommonClientWrapperEntity commonClientWrapperEntity, CreateOrUpdatePackageRequest request) {
        log.debug("#createIntegrationPackage(CommonClientWrapperEntity commonClientWrapperEntity, CreatePackageRequest request): {}, {}", commonClientWrapperEntity, request);

        validateInputParameters(commonClientWrapperEntity, request);

        RestTemplateWrapper restTemplateWrapper = getRestTemplateWrapper(commonClientWrapperEntity);
        String token = retrieveToken(commonClientWrapperEntity, restTemplateWrapper.getRestTemplate());

        return createIntegrationPackage(commonClientWrapperEntity.getConnectionProperties(), request, restTemplateWrapper.getRestTemplate(), token);
    }

    public void updateIntegrationPackage(CommonClientWrapperEntity commonClientWrapperEntity, String externalId, CreateOrUpdatePackageRequest request) {
        log.debug("#updateIntegrationPackage(CommonClientWrapperEntity commonClientWrapperEntity, CreatePackageRequest request): {}, {}", commonClientWrapperEntity, request);

        validateInputParameters(commonClientWrapperEntity, request);

        RestTemplateWrapper restTemplateWrapper = getRestTemplateWrapper(commonClientWrapperEntity);

        String token = retrieveToken(commonClientWrapperEntity, restTemplateWrapper.getRestTemplate());

        updateIntegrationPackage(commonClientWrapperEntity.getConnectionProperties(), externalId, request, restTemplateWrapper.getRestTemplate(), token);
    }

    public void lockPackage(ConnectionProperties connectionProperties, String externalPackageId, String csrfToken, RestTemplate restTemplate, boolean forceLock) {
        lockOrUnlockPackage(connectionProperties, externalPackageId, csrfToken, restTemplate, "LOCK", forceLock);
    }

    public void unlockPackage(ConnectionProperties connectionProperties, String externalPackageId, String csrfToken, RestTemplate restTemplate) {
        lockOrUnlockPackage(connectionProperties, externalPackageId, csrfToken, restTemplate, "UNLOCK", false);
    }

    private String createIntegrationPackage(ConnectionProperties connectionProperties, CreateOrUpdatePackageRequest request, RestTemplate restTemplate, String userApiCsrfToken) {

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance()
                .scheme(connectionProperties.getProtocol())
                .host(connectionProperties.getHost())
                .path("/itspaces/odata/1.0/workspace.svc/ContentEntities.ContentPackages");

        if (connectionProperties.getPort() != null) {
            uriBuilder.port(connectionProperties.getPort());
        }
        URI uri = uriBuilder.build().toUri();

        Map<String, String> requestBody = prepareRequestBodyForPackageUpload(request);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("X-CSRF-Token", userApiCsrfToken);
        httpHeaders.add("Accept", "application/json");
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, httpHeaders);

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                uri,
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        if (HttpStatus.CREATED.equals(responseEntity.getStatusCode())) {
            JSONObject createdPackage = new JSONObject(responseEntity.getBody()).getJSONObject("d");
            return createdPackage.getString("reg_id");
        } else {
            throw new ClientIntegrationException(String.format(
                    "Couldn't create package %s: Code: %d, Message: %s",
                    request.getTechnicalName(),
                    responseEntity.getStatusCode().value(),
                    responseEntity.getBody())
            );
        }

    }

    private void updateIntegrationPackage(ConnectionProperties connectionProperties, String externalId, CreateOrUpdatePackageRequest request, RestTemplate restTemplate, String userApiCsrfToken) {
        boolean locked = false;
        try {

            lockPackage(connectionProperties, externalId, userApiCsrfToken, restTemplate, true);
            locked = true;

            UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance()
                    .scheme(connectionProperties.getProtocol())
                    .host(connectionProperties.getHost())
                    .path(String.format(
                            "/itspaces/odata/1.0/workspace.svc/ContentEntities.ContentPackages('%s')",
                            request.getTechnicalName())
                    );

            if (connectionProperties.getPort() != null) {
                uriBuilder.port(connectionProperties.getPort());
            }
            URI uri = uriBuilder.build().toUri();

            Map<String, String> requestBody = prepareRequestBodyForPackageUpload(request);

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("X-CSRF-Token", userApiCsrfToken);
            httpHeaders.add("Accept", "application/json");
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> httpEntity = new HttpEntity<>(requestBody, httpHeaders);

            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    uri,
                    HttpMethod.PUT,
                    httpEntity,
                    String.class
            );


            if (!HttpStatus.NO_CONTENT.equals(responseEntity.getStatusCode())) {
                throw new ClientIntegrationException(String.format(
                        "Couldn't update package %s: Code: %d, Message: %s",
                        request.getTechnicalName(),
                        responseEntity.getStatusCode().value(),
                        responseEntity.getBody())
                );
            }

        } finally {
            if (locked) {
                unlockPackage(connectionProperties, externalId, userApiCsrfToken, restTemplate);
            }
        }
    }

    private Map<String, String> prepareRequestBodyForPackageUpload(CreateOrUpdatePackageRequest request) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("TechnicalName", request.getTechnicalName());
        requestBody.put("DisplayName", request.getDisplayName());
        requestBody.put("ShortText", request.getShortDescription());
        requestBody.put("Vendor", request.getVendor());
        requestBody.put("Version", request.getVersion());
        requestBody.put("Category", "Integration");
        requestBody.put("SupportedPlatforms", "SAP HANA Cloud Integration");
        return requestBody;
    }

    private void lockOrUnlockPackage(ConnectionProperties connectionProperties, String externalPackageId, String csrfToken, RestTemplate restTemplate, String webdav, boolean forceLock) {
        log.debug("#lockPackage(ConnectionProperties connectionProperties, String externalPackageId, String csrfToken, RestTemplate restTemplate, boolean forceLock): " +
                "{}, {}, {}, {}, {}", connectionProperties, externalPackageId, csrfToken, restTemplate, forceLock);

        Assert.notNull(connectionProperties, "connectionProperties must be not null!");
        Assert.notNull(externalPackageId, "externalPackageId must be not null!");
        Assert.notNull(csrfToken, "csrfToken must be not null!");
        Assert.notNull(restTemplate, "restTemplate must be not null!");

        try {

            UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance()
                    .scheme(connectionProperties.getProtocol())
                    .host(connectionProperties.getHost())
                    .path("/itspaces/api/1.0/workspace/{0}");

            if (connectionProperties.getPort() != null) {
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
            httpHeaders.add("X-CSRF-Token", csrfToken);

            HttpEntity<Void> requestEntity = new HttpEntity<>(null, httpHeaders);

            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    uri,
                    HttpMethod.PUT,
                    requestEntity,
                    String.class
            );

            if (!HttpStatus.OK.equals(responseEntity.getStatusCode())) {
                throw new ClientIntegrationException(String.format(
                        "Couldn't lock package %s: Code: %d, Message: %s",
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

    private void validateInputParameters(CommonClientWrapperEntity commonClientWrapperEntity, CreateOrUpdatePackageRequest request) {
        Assert.notNull(commonClientWrapperEntity, "commonClientWrapperEntity must be not null!");
        Assert.notNull(request, "request must be not null!");
        Assert.notNull(request.getTechnicalName(), "package technical name must be not null!");
        Assert.notNull(request.getDisplayName(), "package display name must be not null!");
    }

}
