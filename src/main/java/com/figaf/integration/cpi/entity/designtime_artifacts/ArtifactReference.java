package com.figaf.integration.cpi.entity.designtime_artifacts;

import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArtifactReference {

    private String packageName;

    private String packageTechnicalName;

    private String type;

    private Object[] resourceTypes;

    private Object[] resources;

    private String name;

    private String bundleSymbolicName;
}
