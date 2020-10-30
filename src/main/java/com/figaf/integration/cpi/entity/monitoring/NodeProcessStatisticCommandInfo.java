package com.figaf.integration.cpi.entity.monitoring;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Arsenii Istlentev
 */
@Getter
@Setter
@ToString
public class NodeProcessStatisticCommandInfo {

    private String id;
    private String name;
    private String nodeState;
    private String version;
    private Double cpu;
    private Double memory;
}
