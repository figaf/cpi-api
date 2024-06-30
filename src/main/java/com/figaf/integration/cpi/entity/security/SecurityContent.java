package com.figaf.integration.cpi.entity.security;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
@Getter
@Setter
@ToString
public abstract class SecurityContent {

    private String name;
    private String description;
    private String deployedBy;
    private Date deployedOn;
    private String status;
}
