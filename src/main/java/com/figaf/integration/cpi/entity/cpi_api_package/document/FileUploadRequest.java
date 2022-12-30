package com.figaf.integration.cpi.entity.cpi_api_package.document;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class FileUploadRequest {

    private byte[] file;

    private FileMetaData fileMetaData;

}
