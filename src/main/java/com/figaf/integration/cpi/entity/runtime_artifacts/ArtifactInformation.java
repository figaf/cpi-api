package com.figaf.integration.cpi.entity.runtime_artifacts;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class ArtifactInformation {

    @JacksonXmlProperty(localName = "id")
    private String id;

    @JacksonXmlProperty(localName = "taskId")
    private String taskId;

    @JacksonXmlProperty(localName = "deployState")
    private String deployState;

    @JacksonXmlProperty(localName = "deployedBy")
    private String deployedBy;

    @JacksonXmlProperty(localName = "deployedOn")
    private String deployedOn;

    @JacksonXmlProperty(localName = "name")
    private String name;

    @JacksonXmlProperty(localName = "tenantId")
    private String tenantId;

    @JacksonXmlProperty(localName = "nodeType")
    private String nodeType;

    @JacksonXmlProperty(localName = "semanticState")
    private String semanticState;

    @JacksonXmlProperty(localName = "symbolicName")
    private String symbolicName;

    @JacksonXmlProperty(localName = "runtimeLocationId")
    private String runtimeLocationId;

    @JacksonXmlProperty(localName = "version")
    private String version;

    @JacksonXmlElementWrapper(localName = "message")
    @JacksonXmlProperty(localName = "message")
    private List<MessageDetail> messageDetails;

    public List<MessageDetail> getMessageDetails() {
        if (messageDetails == null) {
            messageDetails = new ArrayList<>();
        }
        return messageDetails;
    }

    public MessageDetail getFirstMessageDetail() {
        if (getMessageDetails().isEmpty()) {
            return null;
        }
        return getMessageDetails().get(0);
    }
}