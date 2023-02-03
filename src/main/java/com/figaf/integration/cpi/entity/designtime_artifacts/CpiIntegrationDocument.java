package com.figaf.integration.cpi.entity.designtime_artifacts;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Klochkov Sergey
 */
@Getter
@Setter
@ToString(of = {"externalId", "technicalName", "displayedName", "version"})
public class CpiIntegrationDocument implements Serializable {

    private String externalId;
    private String technicalName;
    private String displayedName;
    private String version;
    private Date creationDate;
    private String createdBy;
    private Date modificationDate;
    private String modifiedBy;
    private String description;
    private String trackedObjectType;

    //for file document
    private String fileName;
    private String contentType;

    //for url document
    private String url;

}
