package com.figaf.integration.cpi.entity.designtime_artifacts;

/**
 * @author Klochkov Sergey
 */
public enum CpiArtifactType {

    IFLOW("CPI_IFLOW", "IFlow"),
    VALUE_MAPPING("VALUE_MAPPING", "ValueMapping"),
    REST_API("CPI_REST_API", "RESTAPIProvider"),
    SOAP_API("CPI_SOAP_API", "SOAPPIProvider"),
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

}
