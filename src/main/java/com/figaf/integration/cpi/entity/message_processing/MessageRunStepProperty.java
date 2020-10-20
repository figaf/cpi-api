package com.figaf.integration.cpi.entity.message_processing;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Nesterov Ilya
 */

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MessageRunStepProperty {

    private PropertyType propertyType;
    private String name;
    private String value;
}
