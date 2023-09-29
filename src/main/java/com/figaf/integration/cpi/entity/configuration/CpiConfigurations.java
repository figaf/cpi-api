package com.figaf.integration.cpi.entity.configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CpiConfigurations {

    // Build number - global version of the whole Integration Suite tenant
    private String integrationSuiteTenantGlobalBuildNumber;
    // version of the IS part related to CPI
    private String cloudIntegrationBuildNumber;
    // version of CPI runtime
    private String cloudIntegrationRunTimeBuildNumber;
    private String integrationAdvisorBuildNumber;
    private String apiManagementBuildNumber;
}
