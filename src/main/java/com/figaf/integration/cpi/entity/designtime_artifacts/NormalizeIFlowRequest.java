package com.figaf.integration.cpi.entity.designtime_artifacts;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class NormalizeIFlowRequest {

    private String packageExternalId;
    private String iflowExternalId;
    private String iflowTechnicalName;
}
