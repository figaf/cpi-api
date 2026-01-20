package com.figaf.integration.cpi.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.client.mapper.ObjectMapperFactory;
import com.figaf.integration.cpi.entity.runtime_artifacts.*;
import com.figaf.integration.cpi.utils.CpiApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.slf4j.Logger;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.figaf.integration.cpi.utils.CpiApiUtils.normalizeUuid;
import static java.lang.String.format;

/**
 * @author Kostas Charalambous
 */
@Slf4j
public class IntegrationContentWebApiClient extends IntegrationContentAbstractClient {

    private static final String OPERATIONS_PATH_FOR_TOKEN = "/Operations";

    private static final String INTEGRATION_COMPONENTS_LIST_API = "/Operations/com.sap.it.op.tmn.commands.dashboard.webui.IntegrationComponentsListCommand";
    private static final String INTEGRATION_COMPONENT_DETAIL_API = "/Operations/com.sap.it.op.tmn.commands.dashboard.webui.IntegrationComponentDetailCommand?artifactId=%s";
    private static final String DELETE_CONTENT_API = "/Operations/com.sap.it.nm.commands.deploy.DeleteContentCommand";

    private static final Map<String, String> NODE_TYPE_MAPPING = new HashMap<>();

    static {
        NODE_TYPE_MAPPING.put("IFLMAP", "INTEGRATION_FLOW");
    }

    public IntegrationContentWebApiClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    public List<IntegrationContent> getAllIntegrationRuntimeArtifacts(RequestContext requestContext) {
        log.debug("#getAllIntegrationRuntimeArtifacts: requestContext={}", requestContext);
        return executeMethod(
            requestContext.withPreservingIntegrationSuiteUrl(),
            OPERATIONS_PATH_FOR_TOKEN,
            addRuntimeLocationIdToUrlIfNotBlank(INTEGRATION_COMPONENTS_LIST_API, requestContext.getRuntimeLocationId()),
            (url, token, restTemplateWrapper) -> callIntegrationComponentsList(
                url,
                requestContext.getRuntimeLocationId(),
                requestContext.getDefaultRuntimeLocationId(),
                token,
                restTemplateWrapper.getRestTemplate()
            )
        );
    }

    @Override
    public IntegrationContent getIntegrationRuntimeArtifact(RequestContext requestContext, String runtimeArtifactId) {
        log.debug("#getIntegrationRuntimeArtifact: requestContext={}, runtimeArtifactId={}", requestContext, runtimeArtifactId);
        try {
            return executeMethod(
                requestContext.withPreservingIntegrationSuiteUrl(),
                OPERATIONS_PATH_FOR_TOKEN,
                addRuntimeLocationIdToUrlIfNotBlank(format(INTEGRATION_COMPONENT_DETAIL_API, normalizeUuid(runtimeArtifactId)), requestContext.getRuntimeLocationId()),
                (url, token, restTemplateWrapper) -> callIntegrationComponentDetail(
                    url,
                    requestContext.getRuntimeLocationId(),
                    requestContext.getDefaultRuntimeLocationId(),
                    token,
                    restTemplateWrapper.getRestTemplate()
                )
            );
        } catch (Exception ex) {
            String errorMessage = "Failed to retrieve integration runtime artifact %s".formatted(runtimeArtifactId);
            if (ex instanceof HttpClientErrorException.NotFound) {
                errorMessage += " because it wasn't found. Most likely artifact is not deployed or not started yet";
            }
            throw new ClientIntegrationException(errorMessage, ex);
        }
    }

    @Override
    public IntegrationContent getIntegrationRuntimeArtifactWithErrorInformation(
        RequestContext requestContext,
        String runtimeArtifactId
    ) {
        log.debug("#getIntegrationRuntimeArtifactWithErrorInformation: requestContext={}, runtimeArtifactId={}",
            requestContext,
            runtimeArtifactId
        );
        // it's already included to the standard response
        return getIntegrationRuntimeArtifact(requestContext, runtimeArtifactId);
    }

    @Override
    public IntegrationContentErrorInformation getIntegrationRuntimeArtifactErrorInformation(
        RequestContext requestContext,
        IntegrationContent integrationContent
    ) {
        log.debug("#getIntegrationRuntimeArtifactErrorInformation: requestContext={}, integrationContent={}",
            requestContext,
            integrationContent
        );

        // we don't have separate API, error information is a part of standard response
        IntegrationContent actualIntegrationContent = getIntegrationRuntimeArtifact(
            requestContext,
            integrationContent.getExternalId()
        );
        return actualIntegrationContent.getErrorInformation();
    }

    @Override
    public void undeployIntegrationRuntimeArtifact(RequestContext requestContext, String runtimeArtifactId) {
        log.debug("#undeployRuntimeArtifact: runtimeArtifactId={}, requestContext={}", runtimeArtifactId, requestContext);

        try {
            IntegrationContent runtimeArtifact = getIntegrationRuntimeArtifact(requestContext, runtimeArtifactId);
            executeMethod(
                requestContext.withPreservingIntegrationSuiteUrl(),
                OPERATIONS_PATH_FOR_TOKEN,
                DELETE_CONTENT_API,
                (url, token, restTemplateWrapper) -> callDeleteContent(
                    url,
                    runtimeArtifact,
                    token,
                    restTemplateWrapper.getRestTemplate()
                )
            );
        } catch (ClientIntegrationException ex) {
            if (ex.getMessage().contains("404 Not Found")) {
                log.debug("Integration runtime artifact with id {} wasn't found. Runtime location id: {}",
                    runtimeArtifactId,
                    requestContext.getRuntimeLocationId()
                );
                return;
            }
            throw ex;
        }
    }

    private List<IntegrationContent> callIntegrationComponentsList(
        String url,
        String runtimeLocationId,
        String defaultRuntimeLocationId,
        String csrfToken,
        RestTemplate restTemplate
    ) {
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("X-CSRF-Token", csrfToken);
            httpHeaders.setContentType(MediaType.APPLICATION_XML);
            HttpEntity<Void> requestEntity = new HttpEntity<>(httpHeaders);

            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                IntegrationComponentsListResponse integrationComponentsListResponse = ObjectMapperFactory
                    .getXmlObjectMapper()
                    .readValue(responseEntity.getBody(), IntegrationComponentsListResponse.class);
                return integrationComponentsListResponse.getArtifactInformations()
                    .stream()
                    .filter(Objects::nonNull)
                    .map(artifactInformation -> fillIntegrationContent(artifactInformation, runtimeLocationId, defaultRuntimeLocationId))
                    .collect(Collectors.toList());
            }

            throw new ClientIntegrationException(format("Code: %d, Message: %s",
                responseEntity.getStatusCode().value(),
                requestEntity.getBody()
            ));
        } catch (Exception ex) {
            throw new ClientIntegrationException(format("Error occurred during fetching runtime artifacts from runtime %s: %s",
                runtimeLocationId,
                ex.getMessage()
            ), ex);
        }
    }

    private IntegrationContent callIntegrationComponentDetail(
        String url,
        String runtimeLocationId,
        String defaultRuntimeLocationId,
        String csrfToken,
        RestTemplate restTemplate
    ) {
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("X-CSRF-Token", csrfToken);
            httpHeaders.setContentType(MediaType.APPLICATION_XML);
            HttpEntity<Void> requestEntity = new HttpEntity<>(httpHeaders);

            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);

            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                IntegrationComponentDetailResponse integrationComponentDetailResponse = ObjectMapperFactory
                    .getXmlObjectMapper()
                    .readValue(responseEntity.getBody(), IntegrationComponentDetailResponse.class);
                return fillIntegrationContentWithErrorInformation(integrationComponentDetailResponse, runtimeLocationId, defaultRuntimeLocationId);
            }

            throw new ClientIntegrationException(format("Code: %d, Message: %s",
                responseEntity.getStatusCode().value(),
                requestEntity.getBody()
            ));
        } catch (Exception ex) {
            throw new ClientIntegrationException(format("Error occurred during fetching runtime artifact from runtime %s: %s",
                runtimeLocationId,
                ex.getMessage()
            ), ex);
        }
    }

    private String callDeleteContent(
        String url,
        IntegrationContent runtimeArtifact,
        String csrfToken,
        RestTemplate restTemplate
    ) {
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("X-CSRF-Token", csrfToken);
            httpHeaders.add("Accept", MediaType.APPLICATION_XML_VALUE);
            httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
            requestBody.add("artifactIds", runtimeArtifact.getExternalId());
            requestBody.add("tenantId", runtimeArtifact.getTenantId());
            if (!StringUtils.isBlank(runtimeArtifact.getRuntimeLocationId())) {
                requestBody.add("runtimeLocationId", runtimeArtifact.getRuntimeLocationId());
            }

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(requestBody, httpHeaders);

            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                if (responseEntity.getBody() != null) {
                    JsonNode jsonNode = ObjectMapperFactory.getXmlObjectMapper().readTree(responseEntity.getBody());
                    return jsonNode.elements().next().asText();
                }
                return null;
            }

            throw new ClientIntegrationException(format("Code: %d, Message: %s",
                responseEntity.getStatusCode().value(),
                requestEntity.getBody()
            ));
        } catch (Exception ex) {
            throw new ClientIntegrationException(format("Error occurred during undeploying runtime artifact %s (id = %s) from runtime %s: %s",
                runtimeArtifact.getId(),
                runtimeArtifact.getExternalId(),
                runtimeArtifact.getRuntimeLocationId(),
                ex.getMessage()
            ), ex);
        }
    }

    private IntegrationContent fillIntegrationContent(
        ArtifactInformation artifactInformation,
        String runtimeLocationId,
        String defaultRuntimeLocationId
    ) throws JSONException {
        IntegrationContent integrationContent = new IntegrationContent();
        integrationContent.setId(artifactInformation.getSymbolicName());
        integrationContent.setExternalId(artifactInformation.getId());
        integrationContent.setVersion(artifactInformation.getVersion());
        integrationContent.setName(artifactInformation.getName());
        integrationContent.setType(artifactInformation.getType());
        integrationContent.setTenantId(artifactInformation.getTenantId());
        integrationContent.setDeployedBy(artifactInformation.getDeployedBy());
        integrationContent.setDeployedOn(CpiApiUtils.parseDate(artifactInformation.getDeployedOn()));
        integrationContent.setStatus(artifactInformation.getSemanticState());
        //integrationContent.runtimeLocationId equals to null indicates that it's a default runtime. It is saved as null in the tracked object.
        integrationContent.setRuntimeLocationId(!CpiApiUtils.isDefaultRuntime(runtimeLocationId, defaultRuntimeLocationId) ? runtimeLocationId : null);
        return integrationContent;
    }

    private IntegrationContent fillIntegrationContentWithErrorInformation(
        IntegrationComponentDetailResponse integrationComponentDetailResponse,
        String runtimeLocationId,
        String defaultRuntimeLocationId
    ) throws JSONException {
        IntegrationContent integrationContent = fillIntegrationContent(
            integrationComponentDetailResponse.getArtifactInformation(),
            runtimeLocationId,
            defaultRuntimeLocationId
        );
        integrationContent.setLogConfiguration(integrationComponentDetailResponse.getLogConfiguration());

        // Error information is available either in artifact information (if single node)
        // or in component informations (if multiple nodes).
        // We always take the first item in MessageDetails collection because it seems there can't be more items.
        // As for components, for now we take first failed (i.e. error message from one node).
        if (CollectionUtils.isNotEmpty(integrationComponentDetailResponse.getArtifactInformation().getMessageDetails())) {
            integrationContent.setErrorInformation(fillIntegrationContentErrorInformation(integrationComponentDetailResponse
                .getArtifactInformation()
                .getFirstMessageDetail()
            ));
        } else {
            integrationComponentDetailResponse.getComponentInformations().stream()
                .filter(componentInformation -> "ERROR".equals(componentInformation.getState()))
                .findFirst()
                .ifPresent(firstComponentInformationWithError ->
                    integrationContent.setErrorInformation(fillIntegrationContentErrorInformation(
                        firstComponentInformationWithError.getFirstMessageDetail()
                    ))
                );
        }

        return integrationContent;
    }

    private IntegrationContentErrorInformation fillIntegrationContentErrorInformation(MessageDetail messageDetail) {
        if (messageDetail == null) {
            return null;
        }

        IntegrationContentErrorInformation integrationContentErrorInformation = new IntegrationContentErrorInformation();
        integrationContentErrorInformation.getParameter().add(messageDetail.getMessageText());

        IntegrationContentErrorInformation.IntegrationContentErrorInformationMessage message = new IntegrationContentErrorInformation.IntegrationContentErrorInformationMessage();
        message.setSubsystemName(messageDetail.getSubsystemName());
        message.setSubsystemPartName(messageDetail.getSubsystemPartName());
        message.setMessageId(messageDetail.getMessageId());
        integrationContentErrorInformation.setMessage(message);

        return integrationContentErrorInformation;
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}