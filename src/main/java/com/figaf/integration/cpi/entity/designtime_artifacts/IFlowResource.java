package com.figaf.integration.cpi.entity.designtime_artifacts;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class IFlowResource {

    private String resourceName;

    private String resourceLocation;

    private String resourceType;

    private String resourceExtension;

    private String resourceCategory;
}
