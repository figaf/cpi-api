package com.figaf.integration.cpi.entity.event_mesh;

import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class QueueMetadata {

    private String name;
    private String accessType;
    private long maxDeliveredUnackedMsgsPerFlow;
    private long maxMessageSizeInBytes;
    private long maxQueueMessageCount;
    private long maxQueueSizeInBytes;
    private long messageCount;
    private long queueSizeInBytes;
    private boolean respectTtl;
    private long maxRedeliveryCount;
    private long maxTtl;
    private boolean deadMsgQueue;
    private boolean egressDisabled;
    private boolean ingressDisabled;
    private long unacknowledgedMessageCount;
}
