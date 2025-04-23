package com.figaf.integration.cpi.entity.designtime_artifacts;

import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ilya Nesterov
 */
@Getter
@ToString
public class AdditionalAttributes implements Serializable {

    private final List<String> source = new ArrayList<>();
    private final List<String> target = new ArrayList<>();
}
