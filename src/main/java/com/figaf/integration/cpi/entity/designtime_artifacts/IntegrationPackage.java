package com.figaf.integration.cpi.entity.designtime_artifacts;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Nesterov Ilya
 */
@Getter
@Setter
@ToString(of = {"externalId", "technicalName", "version"})
public class IntegrationPackage implements Serializable {

    private String externalId;
    private String technicalName;
    private String displayedName;
    private String version;
    private Date creationDate;
    private String createdBy;
    private Date modificationDate;
    private String modifiedBy;
    private String vendor;
    private String shortDescription;

}
