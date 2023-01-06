package com.figaf.integration.cpi.entity.tags;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CustomTag {

    private String name;
    private String value;
}
