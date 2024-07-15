package com.figaf.integration.cpi.entity.security;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UserCredentialsRequest {

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Kind")
    private UserCredentialsKind kind;

    @JsonProperty("Description")
    private String description;

    @JsonProperty("User")
    private String user;

    @JsonProperty("Password")
    private String password;

    @JsonProperty("CompanyId")
    private String companyId;
}
