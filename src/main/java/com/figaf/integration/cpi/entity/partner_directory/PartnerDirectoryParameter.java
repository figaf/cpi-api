package com.figaf.integration.cpi.entity.partner_directory;

import com.figaf.integration.cpi.entity.partner_directory.enums.TypeOfParam;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class PartnerDirectoryParameter {

    private String pid;
    private String id;
    private String value;
    private Date createdTime;
    private String createdBy;
    private Date modificationDate;
    private TypeOfParam type;
    private String modifiedBy;
    private String contentType;
}
