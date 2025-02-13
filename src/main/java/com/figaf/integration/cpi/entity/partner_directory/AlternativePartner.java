package com.figaf.integration.cpi.entity.partner_directory;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class AlternativePartner {
    private String hexagency;
    private String hexscheme;
    private String hexid;
    private String agency;
    private String scheme;
    private String id;
    private String pid;
    private String lastModifiedBy;
    private Date lastModifiedTime;
    private String createdBy;
    private Date createdTime;

}
