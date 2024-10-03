package com.figaf.integration.cpi.entity.runtime_artifacts;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
@NoArgsConstructor
public  class ArtifactInformation {

    @JacksonXmlProperty(localName = "taskId")
    private String taskId;

    @JacksonXmlProperty(localName = "deployState")
    private String deployState;

    @JacksonXmlProperty(localName = "deployedBy")
    private String deployedBy;

    @JacksonXmlProperty(localName = "deployedOn")
    private String deployedOn;

    @JacksonXmlProperty(localName = "id")
    private String id;

    @JacksonXmlProperty(localName = "name")
    private String name;

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
}