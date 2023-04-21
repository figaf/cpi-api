package com.figaf.integration.cpi.entity.designtime_artifacts;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CpiArtifactFromPublicApi {

    private String technicalName;
    private String displayedName;
    private String version;
    private String packageTechnicalName;
    private String description;
    private String sender;
    private String receiver;
    private String createdBy;
    private String createdAt;
    private String modifiedBy;
    private String modifiedAt;
}
