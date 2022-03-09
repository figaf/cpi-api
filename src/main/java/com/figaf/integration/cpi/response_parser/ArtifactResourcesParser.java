package com.figaf.integration.cpi.response_parser;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.figaf.integration.cpi.entity.designtime_artifacts.ArtifactReference;
import com.figaf.integration.cpi.entity.designtime_artifacts.ArtifactReferences;
import com.figaf.integration.cpi.entity.designtime_artifacts.ArtifactResource;
import com.figaf.integration.cpi.entity.designtime_artifacts.ArtifactResources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Arsenii Istlentev
 */
public class ArtifactResourcesParser {

    private ArtifactResourcesParser() {}

    private static final TypeReference<List<ArtifactResources>> LIST_ARTIFACT_RESOURCES_TYPE_REFERENCE = new TypeReference<List<ArtifactResources>>() {};

    private static final TypeReference<List<ArtifactReferences>> LIST_ARTIFACT_REFERENCES_TYPE_REFERENCE = new TypeReference<List<ArtifactReferences>>() {};

    public static List<ArtifactResource> buildIFlowResources(String body, ObjectMapper objectMapper) throws IOException {
        List<ArtifactResources> resourcesResponse = objectMapper.readValue(body, LIST_ARTIFACT_RESOURCES_TYPE_REFERENCE);
        List<ArtifactResource> resourcesList = new ArrayList<>();
        for (ArtifactResources resources: resourcesResponse) {
            resourcesList.addAll(resources.getResourceList());
        }
        return resourcesList;
    }

    public static List<ArtifactReference> buildIFlowReferences(String body, ObjectMapper objectMapper) throws IOException {
        List<ArtifactReferences> referenceResponse = objectMapper.readValue(body, LIST_ARTIFACT_REFERENCES_TYPE_REFERENCE);
        List<ArtifactReference> referenceList = new ArrayList<>();
        for (ArtifactReferences references: referenceResponse) {
            referenceList.addAll(references.getReferenceList());
        }
        return referenceList;
    }
}
