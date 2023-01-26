package com.figaf.integration.cpi.entity.cpi_api_package.document;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.springframework.stereotype.Service;

@Getter
@Service
@SuperBuilder
public class DocumentUploadMetaData {

    private String name;

    private String description;

    private String type;

}
