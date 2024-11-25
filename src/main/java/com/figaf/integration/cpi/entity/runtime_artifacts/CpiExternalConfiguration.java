package com.figaf.integration.cpi.entity.runtime_artifacts;

import lombok.*;

import java.io.Serializable;

/**
 * @author Arsenii Istlentev
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class CpiExternalConfiguration implements Serializable {

    private String parameterKey;
    private String parameterValue;
    private String dataType;
}
