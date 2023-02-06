package com.figaf.integration.cpi.entity.designtime_artifacts;

import lombok.*;

import java.util.*;

/**
 * @author Nesterov Ilya
 */
@Getter
@Setter
@ToString(of = {"externalId", "technicalName", "version"})
public class CpiArtifact {

    private String externalId;
    private String packageName; //displayedName
    private String technicalName;
    private String displayedName;
    private String version;
    private Date creationDate;
    private String createdBy;
    private Date modificationDate;
    private String modifiedBy;
    private Date deploymentDate;
    private String deployedBy;
    private String status;
    private String description;
    private boolean deployed = false;
    private String packageTechnicalName;
    private String packageExternalId;
    private String trackedObjectType;

}
