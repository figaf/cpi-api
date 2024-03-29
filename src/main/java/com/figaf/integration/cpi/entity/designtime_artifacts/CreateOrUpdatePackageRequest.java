package com.figaf.integration.cpi.entity.designtime_artifacts;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Nesterov Ilya
 */

@Getter
@Setter
public class CreateOrUpdatePackageRequest {

    private String technicalName;
    private String displayName;
    private String shortDescription;
    private String vendor;
    private String version;
    private String keyword;
    private String product;
    private String industry;
    private String country;
    private String lineOfBusiness;
}
