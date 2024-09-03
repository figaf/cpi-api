package com.figaf.integration.cpi.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.cpi_api_package.document.UrlUploadRequest;
import com.figaf.integration.cpi.entity.design_guidelines.DesignGuidelines;
import com.figaf.integration.cpi.entity.design_guidelines.constants.DesignGuidelinesConstants;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.OK;

@Slf4j
public class DesignGuidelinesClient extends CpiBaseClient {

    private static final String X_CSRF_TOKEN = "X-CSRF-Token";

    private static final String API_EXECUTE_DESIGN_GUIDELINES_TEMPLATE = "/api/1.0/workspace/%s/artifacts/%s/designguidelinerules/%s?artifactType=IFlow&webdav=VALIDATE";

    public DesignGuidelinesClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    public DesignGuidelines executeDesignGuidelines(
        RequestContext requestContext,
        String packageExternalId,
        String artifactExternalId,
        String iFlowTechnicalName
    ) {
        log.debug(
            "#executeDesignGuidelines: requestContext={}, packageExternalId={}, artifactExternalId={}, iFlowTechnicalName={}",
            requestContext,
            packageExternalId,
            artifactExternalId,
            iFlowTechnicalName
        );

        String path;
        String apiRetrieveDesignGuidelines = String.format(
            API_EXECUTE_DESIGN_GUIDELINES_TEMPLATE,
            packageExternalId,
            artifactExternalId,
            iFlowTechnicalName
        );
        if (isIntegrationSuiteHost(requestContext.getConnectionProperties().getHost())) {
            path = apiRetrieveDesignGuidelines;
        } else {
            path = "/itspaces" + apiRetrieveDesignGuidelines;
        }

        return executeMethod(
            requestContext,
            path,
            (url, token, restTemplateWrapper) -> executeDesignGuidelines(restTemplateWrapper.getRestTemplate(), url, token)
        );
    }

    private DesignGuidelines executeDesignGuidelines(
        RestTemplate restTemplate,
        String designGuidelinesUri,
        String userApiCsrfToken
    ) {
        log.debug("start executeDesignGuideGuidelines");

        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add(X_CSRF_TOKEN, userApiCsrfToken);
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<UrlUploadRequest> requestEntity = new HttpEntity<>(httpHeaders);
            ResponseEntity<DesignGuidelines> designGuidelinesResponseEntity = restTemplate.exchange(
                designGuidelinesUri,
                HttpMethod.PUT,
                requestEntity,
                DesignGuidelines.class
            );

            if (OK.equals(designGuidelinesResponseEntity.getStatusCode()) && Optional.ofNullable(designGuidelinesResponseEntity.getBody()).isPresent()) {
                DesignGuidelines designGuidelines = designGuidelinesResponseEntity.getBody();
                designGuidelines.getRulesResult()
                    .forEach(designGuideline -> {
                        designGuideline.setRulesetIdLabel(DesignGuidelinesConstants.RULE_LABELS.get(designGuideline.getRulesetId()));
                        designGuideline.setRuleIdLabel(DesignGuidelinesConstants.RULE_LABELS.get(designGuideline.getRuleId()));
                    });
                return designGuidelines;
            } else {
                throw new ClientIntegrationException(
                    String.format("Couldn't execute guidelines:%n Code: %d, Message: %s",
                        designGuidelinesResponseEntity.getStatusCode().value(),
                        designGuidelinesResponseEntity.getBody()
                    )
                );
            }

        } catch (ClientIntegrationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ClientIntegrationException("Error occurred while executing guidelines: " + ex.getMessage(), ex);
        }
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}
