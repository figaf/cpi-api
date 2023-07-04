package com.figaf.integration.cpi.entity.message_processing;

import lombok.*;

import java.io.Serializable;

/**
 * @author Arsenii Istlentev
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CustomHeaderProperty implements Serializable {

    private String id;
    private String name;
    private String value;
}
