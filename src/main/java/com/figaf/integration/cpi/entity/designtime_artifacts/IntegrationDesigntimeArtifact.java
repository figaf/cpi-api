package com.figaf.integration.cpi.entity.designtime_artifacts;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Arsenii Istlentev
 */
@Getter
@Setter
@ToString
public class IntegrationDesigntimeArtifact {

    private String id;
    private String version;
    private String packageId;
    private String name;
    private String description;
}
