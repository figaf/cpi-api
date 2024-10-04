package com.figaf.integration.cpi.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.criteria.MessageProcessingLogRunStepSearchCriteria;
import com.figaf.integration.cpi.entity.message_processing.MessageProcessingLog;
import com.figaf.integration.cpi.entity.message_processing.MessageProcessingLogRun;
import com.figaf.integration.cpi.entity.message_processing.MessageProcessingLogRunStep;
import com.figaf.integration.cpi.entity.monitoring.RuntimeLocationsResponse;
import com.figaf.integration.cpi.utils.RequestContextUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class MessageProcessingLogEdgeRuntimeClientTest {

    private static MessageProcessingLogEdgeRuntimeClient messageProcessingLogEdgeRuntimeClient;

    @BeforeAll
    static void setUp() {
        messageProcessingLogEdgeRuntimeClient = new MessageProcessingLogEdgeRuntimeClient(new HttpClientsFactory(), "azureedge2");
    }

    @Test
    void test_getRuntimeLocationsForIntegrationSuiteAgent_usingIntegrationSuiteUrl() {
        RequestContext requestContextForWebApiWithIntegrationSuiteUrl = RequestContextUtils.createRequestContextForWebApiWithIntegrationSuiteUrl();

        List<MessageProcessingLogRun>  messageProcessingLogRuns = messageProcessingLogEdgeRuntimeClient.getRunsMetadata(requestContextForWebApiWithIntegrationSuiteUrl,"0w35emUI2NV9mn0G1npTKA");
        MessageProcessingLogRunStepSearchCriteria messageProcessingLogRunStepSearchCriteria = new MessageProcessingLogRunStepSearchCriteria();
        messageProcessingLogRunStepSearchCriteria.setInitTraceMessagePayload(true);
        MessageProcessingLogRunStep messageProcessingLogRunStep = new MessageProcessingLogRunStep();
        messageProcessingLogRunStep.setRunId(messageProcessingLogRuns.get(0).getId());
        messageProcessingLogRunStep.setChildCount(1);
        messageProcessingLogEdgeRuntimeClient.getTraceMessage(
            requestContextForWebApiWithIntegrationSuiteUrl,
            messageProcessingLogRunStepSearchCriteria,
            messageProcessingLogRunStep
        );

    }
}
