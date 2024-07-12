package com.figaf.integration.cpi.entity.security.request;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.figaf.integration.cpi.entity.security.UserCredentialsKind;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true, exclude = "password")
public class UserCredentialsRequest extends SecurityContentRequest {

    @JsonProperty("Kind")
    private UserCredentialsKind kind;

    @JsonProperty("User")
    private String user;

    @JsonProperty("Password")
    private String password;

    @JsonProperty("CompanyId")
    private String companyId;
}
