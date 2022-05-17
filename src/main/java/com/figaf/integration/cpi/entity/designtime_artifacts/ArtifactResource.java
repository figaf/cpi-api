package com.figaf.integration.cpi.entity.designtime_artifacts;

import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArtifactResource {

    private String resourceName;

    private String resourceLocation;

    private String resourceType;

    private String resourceExtension;

    private String resourceCategory;
}
