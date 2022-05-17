package com.figaf.integration.cpi.entity.designtime_artifacts;

import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ArtifactResources {

    private List<ArtifactResource> resourceList;

    private List<ArtifactReference> referenceList;
}
