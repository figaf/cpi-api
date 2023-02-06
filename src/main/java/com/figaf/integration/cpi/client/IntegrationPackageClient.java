package com.figaf.integration.cpi.client;

import com.figaf.integration.common.client.BaseClient;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.entity.ConnectionProperties;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.designtime_artifacts.CreateOrUpdatePackageRequest;
import com.figaf.integration.cpi.entity.designtime_artifacts.IntegrationPackage;
import com.figaf.integration.cpi.entity.lock.Locker;
import com.figaf.integration.cpi.response_parser.IntegrationPackageParser;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpStatus.NO_CONTENT;

/**
 * @author Arsenii Istlentev
 */
@Slf4j
public class IntegrationPackageClient extends BaseClient {

    private static final String API_PACKAGES = "/itspaces/odata/1.0/workspace.svc/ContentPackages?$format=json";
    private static final String API_PACKAGES_WITH_NAME = "/itspaces/odata/1.0/workspace.svc/ContentPackages('%s')";

    public IntegrationPackageClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    public List<IntegrationPackage> getIntegrationPackages(RequestContext requestContext, String filter) {
        log.debug("#getIntegrationPackages(RequestContext requestContext, String filter): {}, {}", requestContext, filter);
        String path = API_PACKAGES + (filter == null ? "" : "&$filter=" + filter.replace(" ", "%20"));
        return executeGet(requestContext, path, IntegrationPackageParser::buildIntegrationPackages);
    }

    public byte[] downloadPackage(RequestContext requestContext, String packageTechnicalName) {
        log.debug("#downloadPackage(RequestContext requestContext, String packageTechnicalName): {}, {}", requestContext, packageTechnicalName);

        String path = String.format(API_PACKAGES_WITH_NAME, packageTechnicalName);
        return executeGet(requestContext, path, resolvedBody -> resolvedBody, byte[].class);
    }

    public String createIntegrationPackage(RequestContext requestContext, CreateOrUpdatePackageRequest request) {
        log.debug("#createIntegrationPackage(RequestContext requestContext, CreatePackageRequest request): {}, {}", requestContext, request);

        validateInputParameters(requestContext, request);

        return executeMethod(
            requestContext,
            "/itspaces/odata/1.0/workspace.svc/ContentEntities.ContentPackages",
            (url, token, restTemplateWrapper) -> createIntegrationPackage(request, url, token, restTemplateWrapper.getRestTemplate())
        );
    }

    public void updateIntegrationPackage(RequestContext requestContext, String externalId, CreateOrUpdatePackageRequest request) {
        log.debug("#updateIntegrationPackage(RequestContext requestContext, CreatePackageRequest request): {}, {}", requestContext, request);

        validateInputParameters(requestContext, request);

        executeMethod(
            requestContext,
            String.format("/itspaces/odata/1.0/workspace.svc/ContentEntities.ContentPackages('%s')", request.getTechnicalName()),
            (url, token, restTemplateWrapper) -> {
                updateIntegrationPackage(requestContext.getConnectionProperties(), externalId, request, url, token, restTemplateWrapper.getRestTemplate());
                return null;
            }
        );

    }


    public void deletePackage(String packageName, RequestContext requestContext) {
        log.debug("#deletePackage(String packageName, RequestContext requestContext): {}, {}", packageName, requestContext);

        executeMethod(
            requestContext,
            API_PACKAGES,
            format(API_PACKAGES_WITH_NAME, packageName),
            (url, token, restTemplateWrapper) -> {
                deletePackage(packageName, url, token, restTemplateWrapper.getRestTemplate());
                return null;
            }
        );
    }

    private String createIntegrationPackage(CreateOrUpdatePackageRequest request, String url, String userApiCsrfToken, RestTemplate restTemplate) {

        Map<String, String> requestBody = prepareRequestBodyForPackageUpload(request);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("X-CSRF-Token", userApiCsrfToken);
        httpHeaders.add("Accept", "application/json");
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, httpHeaders);

        ResponseEntity<String> responseEntity = restTemplate.exchange(
            url,
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

    private void updateIntegrationPackage(ConnectionProperties connectionProperties, String externalId, CreateOrUpdatePackageRequest request, String url, String userApiCsrfToken, RestTemplate restTemplate) {
        boolean locked = false;
        try {

            Locker.lockPackage(connectionProperties, externalId, userApiCsrfToken, restTemplate, true);
            locked = true;

            Map<String, String> requestBody = prepareRequestBodyForPackageUpload(request);

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("X-CSRF-Token", userApiCsrfToken);
            httpHeaders.add("Accept", "application/json");
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> httpEntity = new HttpEntity<>(requestBody, httpHeaders);

            ResponseEntity<String> responseEntity = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                httpEntity,
                String.class
            );


            if (!NO_CONTENT.equals(responseEntity.getStatusCode())) {
                throw new ClientIntegrationException(String.format(
                    "Couldn't update package %s: Code: %d, Message: %s",
                    request.getTechnicalName(),
                    responseEntity.getStatusCode().value(),
                    responseEntity.getBody())
                );
            }

        } finally {
            if (locked) {
                Locker.unlockPackage(connectionProperties, externalId, userApiCsrfToken, restTemplate);
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
        requestBody.put("Products", request.getProduct());
        requestBody.put("Industries", request.getIndustry());
        requestBody.put("LineOfBusiness", request.getLineOfBusiness());
        requestBody.put("Keywords", request.getKeyword());
        requestBody.put("Countries", request.getCountry());
        return requestBody;
    }

    private void validateInputParameters(RequestContext requestContext, CreateOrUpdatePackageRequest request) {
        Assert.notNull(requestContext, "requestContext must be not null!");
        Assert.notNull(request, "request must be not null!");
        Assert.notNull(request.getTechnicalName(), "package technical name must be not null!");
        Assert.notNull(request.getDisplayName(), "package display name must be not null!");
    }

    private void deletePackage(
        String packageName,
        String url,
        String token,
        RestTemplate restTemplate
    ) {
        HttpHeaders httpHeaders = createHttpHeadersWithCSRFToken(token);
        HttpEntity<Void> httpEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, DELETE, httpEntity, String.class);

        if (!NO_CONTENT.equals(responseEntity.getStatusCode())) {
            throw new ClientIntegrationException(format(
                "Couldn't delete package %s: Code: %d, Message: %s",
                packageName,
                responseEntity.getStatusCode().value(),
                responseEntity.getBody())
            );
        }
    }

}
