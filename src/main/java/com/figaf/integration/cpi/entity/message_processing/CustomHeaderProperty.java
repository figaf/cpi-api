package com.figaf.integration.cpi.entity.message_processing;

import lombok.*;

/**
 * @author Arsenii Istlentev
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CustomHeaderProperty {

    private String id;
    private String name;
    private String value;
}
