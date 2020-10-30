package com.figaf.integration.cpi.entity.runtime_artifacts;

import lombok.*;

/**
 * @author Arsenii Istlentev
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class CpiExternalConfiguration {

    private String parameterKey;
    private String parameterValue;
    private String dataType;
}
