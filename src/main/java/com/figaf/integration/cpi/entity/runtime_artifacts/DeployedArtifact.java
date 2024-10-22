package com.figaf.integration.cpi.entity.runtime_artifacts;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * @author Ilya Nesterov
 */
// This DTO belongs to web API /api/1.0/deployedartifacts, it produces similar but not the same result as the Web API
// that returns ArtifactInformation. We can't reuse it here
@Getter
@Setter
@ToString
@NoArgsConstructor
public class DeployedArtifact {

    private String id;                  // "8359fb84-5a7b-45a2-a1e4-366246f47ff2"
    private String name;                // "Sample2406 1 ERP INVOIC.INVOIC02"
    private String bundleSymbolicName;  // "Sample2406_1_ERP_INVOIC.INVOIC02"
    private String bundleType;          // "IntegrationFlow"
    private String version;             // "1.0.1"
    private String deployedBy;          // "sb-695644d6-d4ea-4e9f-86f4-412c294b0c23!b157978|it!b117912"
    // the reason why it's disabled - value is locale-specific, not sure, if it's possible to get other locales instead of english here from CPI
    //@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MMM d, yyyy, h:mm:ss a", timezone = "UTC", locale = "ENGLISH")
    //private Date deployedOn;          // source string: "Oct 14, 2024, 2:32:50 PM"
    private String deployedOn;          // "Oct 14, 2024, 2:32:50 PM"
    private String deployState;         // "DEPLOYED"
    private String semanticState;       // "STARTED" Date(number in millis)
}
