package com.figaf.integration.cpi.entity.partner_directory;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class BinaryParameterUpdateRequest {

    @JsonProperty("Value")
    private String value;

    @JsonProperty("ContentType")
    private String contentType;
}
