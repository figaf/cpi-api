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
    MESSAGE_MAPPING("CPI_MESSAGE_MAPPING", "MessageMapping");

    private final String title;
    private final String queryTitle;

    CpiArtifactType(String title, String queryTitle) {
        this.title = title;
        this.queryTitle = queryTitle;
    }

    public String getTitle() {
        return this.title;
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

}
