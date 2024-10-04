package com.figaf.integration.cpi.entity.monitoring;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class RuntimeLocation {

    private String id;
    private String displayName;
    private String type;
    private String typeId;
    private String state;
}
