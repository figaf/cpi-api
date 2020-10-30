package com.figaf.integration.cpi.entity.message_processing;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Arsenii Istlentev
 */
@Getter
@Setter
@ToString
public class CustomHeaderProperty {

    private String id;
    private String name;
    private String value;
}
