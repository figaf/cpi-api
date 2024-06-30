package com.figaf.integration.cpi.entity.security;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class OAuth2ClientCredentialsCreationRequest {

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Description")
    private String description;

    @JsonProperty("TokenServiceUrl")
    private String tokenServiceUrl;

    @JsonProperty("ClientId")
    private String clientId;

    @JsonProperty("ClientSecret")
    private String clientSecret;

    @JsonProperty("ClientAuthentication")
    private String clientAuthentication;

    @JsonProperty("Scope")
    private String scope;

    @JsonProperty("ScopeContentType")
    private String scopeContentType;

    @JsonProperty("Resource")
    private String resource;

    @JsonProperty("Audience")
    private String audience;
}
