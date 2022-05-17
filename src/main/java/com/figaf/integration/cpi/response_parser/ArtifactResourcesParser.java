package com.figaf.integration.cpi.response_parser;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    public static ArtifactResources buildArtifactResources(String body, ObjectMapper objectMapper) throws IOException {
        List<ArtifactResources> resourcesResponse = objectMapper.readValue(body, LIST_ARTIFACT_RESOURCES_TYPE_REFERENCE);
        ArtifactResources allResources = new ArtifactResources(new ArrayList<>(), new ArrayList<>());
        for (ArtifactResources resources: resourcesResponse) {
            allResources.getResourceList().addAll(resources.getResourceList());
            allResources.getReferenceList().addAll(resources.getReferenceList());
        }
        return allResources;
    }

}
