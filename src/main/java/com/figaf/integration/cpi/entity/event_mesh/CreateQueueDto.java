package com.figaf.integration.cpi.entity.event_mesh;

import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateQueueDto {

    private String accessType;
    private boolean egressDisabled;
    private boolean ingressDisabled;
    private int maxDeliveredUnackedMessagesPerFlow;
    private long maxMessageSizeInBytes;
    private long maxMessageTimeToLiveInSeconds;
    private long maxQueueSizeInBytes;
    private int maxRedeliveryCount;
    private boolean respectTimeToLiveInSeconds;
}
