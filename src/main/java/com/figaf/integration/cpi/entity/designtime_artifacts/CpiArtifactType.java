package com.figaf.integration.cpi.entity.designtime_artifacts;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Klochkov Sergey
 */
@Slf4j
public enum CpiArtifactType {

    IFLOW("CPI_IFLOW", "IFlow"),
    VALUE_MAPPING("VALUE_MAPPING", "ValueMapping"),
    REST_API("CPI_REST_API", "RESTAPIProvider"),
    SOAP_API("CPI_SOAP_API", "SOAPAPIProvider"),
    ODATA_API("CPI_ODATA_API", "OData Service"),
    SCRIPT_COLLECTION("SCRIPT_COLLECTION", "ScriptCollection"),
    MESSAGE_MAPPING("CPI_MESSAGE_MAPPING", "MessageMapping"),
    FUNCTION_LIBRARIES("CPI_FUNCTION_LIBRARIES", "FunctionLibraries");

    private final String trackedObjectType;
    private final String queryTitle;

    CpiArtifactType(String trackedObjectType, String queryTitle) {
        this.trackedObjectType = trackedObjectType;
        this.queryTitle = queryTitle;
    }

    public String getTrackedObjectType() {
        return this.trackedObjectType;
    }

    public String getQueryTitle() {
        return this.queryTitle;
    }

    public static CpiArtifactType fromQueryTitle(String queryTitle) {
        for (CpiArtifactType cpiArtifactType : CpiArtifactType.values()) {
            if (cpiArtifactType.getQueryTitle().equals(queryTitle)) {
                return cpiArtifactType;
            }
        }
        log.warn("Not expected artifact type: {}", queryTitle);
        return null;
    }

    public static CpiArtifactType fromTrackedObjectType(String trackedObjectType) {
        for (CpiArtifactType cpiArtifactType : CpiArtifactType.values()) {
            if (cpiArtifactType.getTrackedObjectType().equals(trackedObjectType)) {
                return cpiArtifactType;
            }
        }
        throw new IllegalArgumentException(String.format("Not expected artifact type %s", trackedObjectType));
    }

}
