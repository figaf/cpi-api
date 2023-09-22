package com.figaf.integration.cpi.entity.configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CpiConfigurations {

    private String isBuildNumber;
    private String ciBuildNumber;
    private String ciRuntimeBuildNumber;
    private String iaBuildNumber;
    private String apiManagementBuildNumber;
    private String nonISBuildNumber;
}
