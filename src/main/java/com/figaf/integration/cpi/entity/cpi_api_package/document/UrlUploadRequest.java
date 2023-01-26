package com.figaf.integration.cpi.entity.cpi_api_package.document;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class UrlUploadRequest extends DocumentUploadMetaData {

    private AdditionalAttributes additionalAttrs;
}
