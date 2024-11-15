package com.figaf.integration.cpi.entity.runtime_artifacts;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ilya Nesterov
 */
@Getter
@Setter
@ToString
@JacksonXmlRootElement(localName = "com.sap.it.op.tmn.commands.dashboard.webui.IntegrationComponentDetailResponse")
public class IntegrationComponentDetailResponse {

    private ArtifactInformation artifactInformation;

    // There are multiple ComponentInformation objects when instance has multiple nodes.
    // Error details in that case are initialized in each ComponentInformation instead of ArtifactInformation
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "componentInformations")
    private List<ComponentInformation> componentInformations;

    private LogConfiguration logConfiguration;

    public List<ComponentInformation> getComponentInformations() {
        if (componentInformations == null) {
            componentInformations = new ArrayList<>();
        }
        return componentInformations;
    }
}
