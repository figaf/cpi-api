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
public class AlternativePartnerCreationRequest {

    @JsonProperty("Agency")
    private String agency;

    @JsonProperty("Scheme")
    private String scheme;

    @JsonProperty("Id")
    private String id;

    @JsonProperty("Pid")
    private String pid;
}
