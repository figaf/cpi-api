package com.figaf.integration.cpi.entity.tags;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@EqualsAndHashCode
public class CustomTagsConfiguration {

    private String tagName;
    private boolean isMandatory;
}
