package com.figaf.integration.cpi.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.event_mesh.QueueMetadata;
import com.figaf.integration.cpi.utils.RequestContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class EventMeshClientTest {

    private static EventMeshClient eventMeshClient;

    @BeforeAll
    static void setUp() {
        eventMeshClient = new EventMeshClient(new HttpClientsFactory());
    }

    @Test
    void test_getQueuesMetaData() {
        RequestContext requestContextForWebApiWithIntegrationSuiteUrl = RequestContextUtils.createRequestContextForWebApiWithIntegrationSuiteUrl();

        List<QueueMetadata> allQueuesMetadata = eventMeshClient.getAllQueuesMetadata(requestContextForWebApiWithIntegrationSuiteUrl);

        assertThat(allQueuesMetadata).as("response of queues metadata shouldn't be null").isNotNull();
    }
}
