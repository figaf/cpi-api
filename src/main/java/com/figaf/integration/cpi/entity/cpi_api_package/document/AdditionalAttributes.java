package com.figaf.integration.cpi.entity.cpi_api_package.document;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class AdditionalAttributes {

    private List<String> url;
}
