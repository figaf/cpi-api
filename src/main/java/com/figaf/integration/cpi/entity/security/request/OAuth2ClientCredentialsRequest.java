package com.figaf.integration.cpi.entity.security.request;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true, exclude = "clientSecret")
public class OAuth2ClientCredentialsRequest extends SecurityContentRequest {

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
