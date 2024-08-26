package com.figaf.integration.cpi.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.design_guidelines.DesignGuidelines;
import com.figaf.integration.cpi.response_parser.DesignGuidelinesParser;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

@Slf4j
public class DesignGuidelinesClient extends CpiBaseClient {

    private static final String API_RETRIEVE_DESIGN_GUIDELINES_TEMPLATE = "/api/1.0/workspace/%s/artifacts/%s/designguidelinerules/%s?artifactType=IFlow";

    private final DesignGuidelinesParser designGuidelinesParser;

    public DesignGuidelinesClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
        this.designGuidelinesParser = new DesignGuidelinesParser();
    }

    public DesignGuidelines getDesignGuidelines(
        String packageExternalId,
        String artifactExternalId,
        String iFlowTechnicalName,
        RequestContext requestContext
    ) {
        log.debug("#getConfigurations(RequestContext requestContext): {}", requestContext);

        String path;
        String apiRetrieveDesignGuidelines = String.format(
            API_RETRIEVE_DESIGN_GUIDELINES_TEMPLATE,
            packageExternalId,
            artifactExternalId,
            iFlowTechnicalName
        );
        if (isIntegrationSuiteHost(requestContext.getConnectionProperties().getHost())) {
            path = apiRetrieveDesignGuidelines;
        } else {
            path = "/itspaces" + apiRetrieveDesignGuidelines;
        }

        return executeGet(requestContext, path, designGuidelinesParser::parseDesignGuidelinesFromJsonString);
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}
