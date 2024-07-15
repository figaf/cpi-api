package com.figaf.integration.cpi.entity.security;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true, exclude = "clientSecret")
public class OAuth2ClientCredentials extends SecurityContent {

    private String tokenServiceUrl;
    private String clientId;
    private String clientSecret;
    private String clientAuthentication;
    private String scope;
    private String scopeContentType;
    private String resource;
    private String audience;
    private String type;
}
