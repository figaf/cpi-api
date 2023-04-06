package com.figaf.integration.cpi.entity.designtime_artifacts;

import lombok.Getter;

/**
 * @author Klochkov Sergey
 */
@Getter
public enum CpiArtifactType {

    IFLOW("CPI_IFLOW", "IFlow", "INTEGRATION_FLOW"),
    VALUE_MAPPING("VALUE_MAPPING", "ValueMapping", "VALUE_MAPPINF"),
    REST_API("CPI_REST_API", "RESTAPIProvider", "REST_API_PROVIDER"),
    SOAP_API("CPI_SOAP_API", "SOAPPIProvider", "SOAP_API_PROVIDER"),
    //TODO check that ODATA_API_PROVIDER is a correct value
    ODATA_API("CPI_ODATA_API", "OData Service", "ODATA_API_PROVIDER"),
    SCRIPT_COLLECTION("SCRIPT_COLLECTION", "ScriptCollection", "SCRIPT_COLLECTION"),
    MESSAGE_MAPPING("CPI_MESSAGE_MAPPING", "MessageMapping", "MESSAGE_MAPPING");

    private final String title;
    private final String queryTitle;
    private final String runtimeIntegrationType;

    CpiArtifactType(String title, String queryTitle, String runtimeIntegrationType) {
        this.title = title;
        this.queryTitle = queryTitle;
        this.runtimeIntegrationType = runtimeIntegrationType;
    }

    public static CpiArtifactType fromTitle(String title) {
        for (CpiArtifactType cpiArtifactType : CpiArtifactType.values()) {
            if (cpiArtifactType.getTitle().equals(title)) {
                return cpiArtifactType;
            }
        }
        throw new IllegalArgumentException(String.format("CpiArtifactType with title '%s' doesn't exist!", title));
    }

}
