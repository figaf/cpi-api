package com.figaf.integration.cpi.client;

import com.figaf.integration.common.entity.CloudPlatformType;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.client.mapper.ObjectMapperFactory;
import com.figaf.integration.cpi.entity.DeleteAndUndeployIFlowResult;
import com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifact;
import com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifactFromPublicApi;
import com.figaf.integration.cpi.entity.designtime_artifacts.CreateIFlowRequest;
import com.figaf.integration.cpi.entity.designtime_artifacts.UpdateIFlowRequest;
import com.figaf.integration.cpi.entity.runtime_artifacts.DeployedArtifact;
import com.figaf.integration.cpi.entity.runtime_artifacts.IFlowRuntimeData;
import com.figaf.integration.cpi.entity.runtime_artifacts.RuntimeArtifactIdentifier;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.*;

import static com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifactType.IFLOW;
import static com.figaf.integration.cpi.utils.CpiApiUtils.loadXMLFromString;
import static java.lang.String.format;

/**
 * @author Arsenii Istlentev
 * @author Klochkov Sergey
 */
@Slf4j
public class CpiIntegrationFlowClient extends CpiRuntimeArtifactClient {

    private static final String API_IFLOW_DEPLOYED_ARTIFACT_INFO = "%s/api/1.0/deployedartifacts/%s?bundleType=IntegrationFlow%s";
    private static final String API_UPLOAD_IFLOW = "/itspaces/api/1.0/workspace/%s/iflows/";
    private static final String API_DEPLOY_IFLOW = "/itspaces/api/1.0/workspace/%s/artifacts/%s/entities/%s/iflows/%s?webdav=DEPLOY";
    private static final String API_SET_TRACE_LOG_LEVEL_FOR_IFLOWS = "/itspaces/Operations/com.sap.it.op.tmn.commands.dashboard.webui.IntegrationComponentSetMplLogLevelCommand";

    private final IntegrationContentClient integrationContentClient;

    public CpiIntegrationFlowClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
        this.integrationContentClient = new IntegrationContentClient(httpClientsFactory);
    }

    public List<CpiArtifact> getIFlowsByPackage(
        RequestContext requestContext,
        String packageTechnicalName,
        String packageDisplayedName,
        String packageExternalId
    ) {
        log.debug("#getIFlowsByPackage(RequestContext requestContext, String packageTechnicalName, String packageDisplayedName, " +
                "String packageExternalId): {}, {}, {}, {}",
            requestContext, packageTechnicalName, packageDisplayedName, packageExternalId);
        return getArtifactsByPackage(
            requestContext,
            packageTechnicalName,
            packageDisplayedName,
            packageExternalId,
            IFLOW
        );
    }

    public byte[] downloadIFlow(
        RequestContext requestContext,
        String packageExternalId,
        String iFlowExternalId
    ) {
        log.debug("#downloadIFlow(RequestContext requestContext, String packageExternalId, String iFlowExternalId): {}, {}, {}",
            requestContext, packageExternalId, iFlowExternalId
        );
        return downloadArtifact(requestContext, packageExternalId, iFlowExternalId);
    }

    public void createIFlow(RequestContext requestContext, CreateIFlowRequest request) {
        log.debug("#createIFlow(RequestContext requestContext, CreateIFlowRequest request): {}, {}", requestContext, request);
        executeMethod(
            requestContext,
            String.format(API_UPLOAD_IFLOW, request.getPackageExternalId()),
            (url, token, restTemplateWrapper) -> {
                createArtifact(
                    requestContext.getConnectionProperties(),
                    request,
                    "iflowBrowse-data",
                    url,
                    token,
                    restTemplateWrapper
                );
                return null;
            }
        );

    }

    public void updateIFlow(RequestContext requestContext, UpdateIFlowRequest request) {
        log.debug("#updateIFlow(RequestContext requestContext, UpdateIFlowRequest request): {}, {}", requestContext, request);
        updateArtifact(requestContext, request);
    }

    public String deployIFlow(
        RequestContext requestContext,
        String packageExternalId,
        String iFlowExternalId,
        String iFlowTechnicalName
    ) {
        log.debug("#deployIFlow(RequestContext requestContext, String packageExternalId, String iFlowExternalId, String iFlowTechnicalName): {}, {}, {}, {}",
            requestContext, packageExternalId, iFlowExternalId, iFlowTechnicalName
        );
        return executeMethod(
            requestContext,
            String.format(API_DEPLOY_IFLOW, packageExternalId, iFlowExternalId, iFlowExternalId, iFlowTechnicalName),
            (url, token, restTemplateWrapper) -> deployArtifact(
                requestContext.getConnectionProperties(),
                packageExternalId,
                IFLOW,
                url,
                token,
                restTemplateWrapper.getRestTemplate()
            )
        );
    }

    public String deployIFlowViaPublicApi(RequestContext requestContext, String iFlowTechnicalName) {
        log.debug("#deployIFlowViaPublicApi(RequestContext requestContext, String iFlowTechnicalName): {}, {}", requestContext, iFlowTechnicalName);
        return executeMethodPublicApi(
            requestContext,
            format("/api/v1/DeployIntegrationDesigntimeArtifact?Id='%s'&Version='active'", iFlowTechnicalName),
            null,
            HttpMethod.POST,
            HttpEntity::getBody
        );
    }

    // that API has parameter bundleType but it works only for IntegrationFlow type, so for now it's not in parent class
    public DeployedArtifact getDeployedArtifactInfo(RequestContext requestContext, String iFlowTechnicalName) {
        log.debug("#getDeployedArtifactInfo: iFlowTechnicalName={}, requestContext={}", iFlowTechnicalName, requestContext);
        String runtimeLocationIdParameter = StringUtils.isNotBlank(requestContext.getRuntimeLocationId())
            ? format("&runtimeLocationId=%s", requestContext.getRuntimeLocationId())
            : "";

        return executeGet(
            requestContext,
            format(API_IFLOW_DEPLOYED_ARTIFACT_INFO,
                resolveApiPrefix(requestContext.getConnectionProperties().getHost()),
                iFlowTechnicalName,
                runtimeLocationIdParameter
            ),
            body -> ObjectMapperFactory.getJsonObjectMapper().readValue(body, DeployedArtifact.class)
        );
    }

    public void setTraceLogLevelForIFlows(RequestContext requestContext, Collection<IFlowRuntimeData> iFlowRuntimeDataCollection) {
        log.debug("#setTraceLogLevelForIFlows(RequestContext requestContext, Collection<IFlowRuntimeData> iFlowRuntimeDataCollection): {}, {}", requestContext, iFlowRuntimeDataCollection);

        executeMethod(
            requestContext,
            API_SET_TRACE_LOG_LEVEL_FOR_IFLOWS,
            (url, token, restTemplateWrapper) -> {
                setTraceLogLevelForIFlows(iFlowRuntimeDataCollection, url, token, restTemplateWrapper.getRestTemplate());
                return null;
            }
        );
    }

    public boolean deleteIFlow(RequestContext requestContext, String iFlowTechnicalName) {
        log.debug("#deleteIFlow(RequestContext requestContext, String iFlowTechnicalName): {}, {}", iFlowTechnicalName, requestContext);
        return executeDeletePublicApi(
            requestContext,
            format("/api/v1/IntegrationDesigntimeArtifacts(Id='%s',Version='active')", iFlowTechnicalName),
            Objects::nonNull
        );
    }

    public CpiArtifactFromPublicApi getIFlowByTechnicalName(RequestContext requestContext, String iFlowTechnicalName) {
        log.debug("#getIFlowByTechnicalName(RequestContext requestContext, String iFlowTechnicalName): {}, {}", iFlowTechnicalName, requestContext);
        return executeMethodPublicApi(
            requestContext,
            format("/api/v1/IntegrationDesigntimeArtifacts(Id='%s',Version='active')", iFlowTechnicalName),
            "",
            HttpMethod.GET,
            responseEntity -> parseCpiArtifact(responseEntity.getBody())
        );
    }

    private CpiArtifactFromPublicApi parseCpiArtifact(String xml) {
        Document document = loadXMLFromString(xml);
        NodeList entries = document.getElementsByTagName("m:properties");

        NodeList propertiesList = entries.item(0).getChildNodes();
        Map<String, String> properties = new HashMap<>();
        for (int i = 0; i < propertiesList.getLength(); i++) {
            Node propertyNode = propertiesList.item(i);
            String propertyName = propertyNode.getLocalName();
            String propertyValue = propertyNode.getTextContent();
            properties.put(propertyName, propertyValue);
        }
        CpiArtifactFromPublicApi cpiArtifact = new CpiArtifactFromPublicApi();
        cpiArtifact.setTechnicalName(properties.get("Id"));
        cpiArtifact.setDisplayedName(properties.get("Name"));
        cpiArtifact.setVersion(properties.get("Version"));
        cpiArtifact.setPackageTechnicalName(properties.get("PackageId"));
        cpiArtifact.setDescription(properties.get("Description"));
        cpiArtifact.setSender(properties.get("Sender"));
        cpiArtifact.setReceiver(properties.get("Receiver"));
        cpiArtifact.setCreatedBy(properties.get("CreatedBy"));
        cpiArtifact.setCreatedAt(properties.get("CreatedAt"));
        cpiArtifact.setModifiedBy(properties.get("ModifiedBy"));
        cpiArtifact.setModifiedAt(properties.get("ModifiedAt"));
        return cpiArtifact;
    }

    public DeleteAndUndeployIFlowResult deleteAndUndeployIFlow(RequestContext requestContext, String iFlowTechnicalName) {
        log.debug("#deleteAndUndeployIFlow(RequestContext requestContext, String iFlowTechnicalName): {}, {}", requestContext, iFlowTechnicalName);
        DeleteAndUndeployIFlowResult deleteAndUndeployIFlowResult = new DeleteAndUndeployIFlowResult(iFlowTechnicalName);

        boolean deletedSuccessfully = deleteIFlow(requestContext, iFlowTechnicalName);
        deleteAndUndeployIFlowResult.setDeleted(deletedSuccessfully);

        //It doesn't work for NEO due to a SAP bug
        if (CloudPlatformType.CLOUD_FOUNDRY.equals(requestContext.getCloudPlatformType())) {
            RuntimeArtifactIdentifier runtimeArtifactIdentifier = RuntimeArtifactIdentifier
                .builder()
                .technicalName(iFlowTechnicalName)
                .build();
            integrationContentClient.undeployIntegrationRuntimeArtifact(requestContext, runtimeArtifactIdentifier);
            deleteAndUndeployIFlowResult.setUndeployed(true);
        }

        return deleteAndUndeployIFlowResult;
    }

    private void setTraceLogLevelForIFlows(
        Collection<IFlowRuntimeData> iflowRuntimeDataCollection,
        String url,
        String token,
        RestTemplate restTemplate
    ) {

        HttpHeaders setTraceLogLevelRequestHttpHeaders = new HttpHeaders();
        setTraceLogLevelRequestHttpHeaders.set("X-CSRF-Token", token);
        setTraceLogLevelRequestHttpHeaders.setContentType(MediaType.APPLICATION_JSON);

        iflowRuntimeDataCollection.forEach(iflowRuntimeData -> {
            try {
                Map<String, String> request = new HashMap<>();
                request.put("artifactSymbolicName", iflowRuntimeData.technicalName());
                request.put("mplLogLevel", "TRACE");
                request.put("nodeType", "IFLMAP");
                request.put("runtimeLocationId", getDefaultRuntimeLocationIdIfBlank(iflowRuntimeData.runtimeLocationId()));

                HttpEntity<Map<String, String>> setTraceLogLevelRequestHttpEntity = new HttpEntity<>(request, setTraceLogLevelRequestHttpHeaders);

                restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    setTraceLogLevelRequestHttpEntity,
                    String.class
                );
            } catch (Exception ex) {
                log.error(
                    String.format(
                        "Error occurred while setting TRACE level for iflow %s: %s",
                        iflowRuntimeData,
                        ex.getMessage()
                    ),
                    ex
                );
            }
        });
    }

}
