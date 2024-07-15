package com.figaf.integration.cpi.entity.security.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public abstract class SecurityContentRequest {

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Description")
    private String description;

}
