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
@ToString(callSuper = true)
public class UserCredentials extends SecurityContent  {

    private String kind;
    private String user;
    private String password;
    private String companyId;
    private String type;
}
