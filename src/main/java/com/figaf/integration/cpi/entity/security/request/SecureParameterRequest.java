package com.figaf.integration.cpi.entity.security.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true, exclude = "secureParam")
public class SecureParameterRequest extends SecurityContentRequest {

    @JsonProperty("SecureParam")
    private String secureParam;
}
