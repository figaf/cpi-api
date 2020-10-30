package com.figaf.integration.cpi.response_parser;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.figaf.integration.cpi.entity.designtime_artifacts.IFlowResource;
import com.figaf.integration.cpi.entity.designtime_artifacts.IFlowResources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Arsenii Istlentev
 */
public class IFlowResourcesParser {

    private final static TypeReference<List<IFlowResources>> LIST_IFLOW_RESOURCES_TYPE_REFERENCE = new TypeReference<List<IFlowResources>>() {};

    public static List<IFlowResource> buildIFlowResources(String body, ObjectMapper objectMapper) throws IOException {
        List<IFlowResources> iFlowResourcesResponse = objectMapper.readValue(body, LIST_IFLOW_RESOURCES_TYPE_REFERENCE);
        List<IFlowResource> iFlowResourcesList = new ArrayList<>();
        for (IFlowResources iFlowResources: iFlowResourcesResponse) {
            iFlowResourcesList.addAll(iFlowResources.getResourceList());
        }
        return iFlowResourcesList;
    }
}
