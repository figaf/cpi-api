package com.figaf.integration.cpi.entity.runtime_artifacts;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@JacksonXmlRootElement(localName = "com.sap.it.op.tmn.commands.dashboard.webui.IntegrationComponentsListResponse")
public class IntegrationComponentsListResponse {

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "artifactInformations")
    private List<ArtifactInformation> artifactInformations;

    public List<ArtifactInformation> getArtifactInformations() {
        if (artifactInformations == null) {
            artifactInformations = new ArrayList<>();
        }
        return artifactInformations;
    }
}