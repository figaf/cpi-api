package com.figaf.integration.cpi.entity.cpi_api_package.document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateDocumentResponse {

    private String id;

    private String technicalName;
}
