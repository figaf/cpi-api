package com.figaf.integration.cpi.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.client.mapper.ObjectMapperFactory;
import com.figaf.integration.cpi.entity.runtime_artifacts.*;
import com.figaf.integration.cpi.utils.CpiApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.slf4j.Logger;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Kostas Charalambous
 */
@Slf4j
public class IntegrationContentEdgeRuntimeClient extends CpiBaseClient {

    private static final String OPERATIONS_PATH_FOR_TOKEN = "/Operations";

    private static final String INTEGRATION_COMPONENTS_LIST = "/Operations/com.sap.it.op.tmn.commands.dashboard.webui.IntegrationComponentsListCommand?runtimeLocationId=%s";

    private final String runtimeLocationId;

    public IntegrationContentEdgeRuntimeClient(HttpClientsFactory httpClientsFactory, String runtimeLocationId) {
        super(httpClientsFactory);
        this.runtimeLocationId = runtimeLocationId;
    }

    public List<IntegrationContent> getAllIntegrationRuntimeArtifacts(RequestContext requestContext) {
        log.debug("#getIntegrationContents(RequestContext requestContext): {}", requestContext);

        return executeMethod(
            requestContext,
            OPERATIONS_PATH_FOR_TOKEN,
            String.format(INTEGRATION_COMPONENTS_LIST, runtimeLocationId),
            (url, token, restTemplateWrapper) -> callIntegrationComponentsList(url, token, restTemplateWrapper.getRestTemplate())
        );
    }

    private List<IntegrationContent> callIntegrationComponentsList(String url, String csrfToken, RestTemplate restTemplate) {
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("X-CSRF-Token", csrfToken);
            httpHeaders.setContentType(MediaType.APPLICATION_XML);
            HttpEntity<Void> requestEntity = new HttpEntity<>(httpHeaders);

            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
            if (HttpStatus.OK.equals(responseEntity.getStatusCode())) {
                IntegrationComponentsListResponse integrationComponentsListResponse = ObjectMapperFactory.getXmlObjectMapper().readValue(responseEntity.getBody(), IntegrationComponentsListResponse.class);
                return integrationComponentsListResponse.getArtifactInformations()
                    .stream()
                    .filter(Objects::nonNull)
                    .map(this::fillIntegrationContent)
                    .collect(Collectors.toList());
            } else {
                throw new ClientIntegrationException(String.format(
                    "Couldn't get callIntegrationComponentsList. Code: %d, Message: %s",
                    responseEntity.getStatusCode().value(),
                    requestEntity.getBody())
                );
            }

        } catch (Exception ex) {
            throw new ClientIntegrationException("Error occurred while getting callIntegrationComponentsList: " + ex.getMessage(), ex);
        }
    }

    private IntegrationContent fillIntegrationContent(ArtifactInformation artifactInformation) throws JSONException {
        IntegrationContent integrationContent = new IntegrationContent();
        integrationContent.setId(artifactInformation.getSymbolicName());
        integrationContent.setVersion(artifactInformation.getVersion());
        integrationContent.setName(artifactInformation.getName());
        integrationContent.setType(artifactInformation.getNodeType());
        integrationContent.setDeployedBy(artifactInformation.getDeployedBy());
        integrationContent.setDeployedOn(CpiApiUtils.parseDate(artifactInformation.getDeployedOn()));
        integrationContent.setStatus(artifactInformation.getSemanticState());
        return integrationContent;
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}