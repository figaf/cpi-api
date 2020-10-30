package com.figaf.integration.cpi.entity.security;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * @author Arsenii Istlentev
 */
@Getter
@Setter
@ToString
public class UserCredentials {

    private String name;
    private String kind;
    private String description;
    private String user;
    private String password;
    private String companyId;
    private String type;
    private String deployedBy;
    private Date deployedOn;
    private String status;
}
