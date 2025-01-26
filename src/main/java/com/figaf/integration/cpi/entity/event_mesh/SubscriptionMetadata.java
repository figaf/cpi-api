package com.figaf.integration.cpi.entity.event_mesh;

import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionMetadata {

    private String topicPattern;

    private String queueName;
}