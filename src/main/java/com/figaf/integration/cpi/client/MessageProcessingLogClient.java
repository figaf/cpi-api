package com.figaf.integration.cpi.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.criteria.MessageProcessingLogRunStepSearchCriteria;
import com.figaf.integration.cpi.entity.message_processing.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Date;
import java.util.List;
import java.util.Set;


/**
 * @author Kostas Charalambous
 */

@Slf4j
public class MessageProcessingLogClient {

    private final MessageProcessingLogDefaultRuntimeClient defaultRuntimeClient;

    private final MessageProcessingLogEdgeRuntimeClient edgeRuntimeClient;

    public MessageProcessingLogClient(HttpClientsFactory httpClientsFactory) {
        this.defaultRuntimeClient = new MessageProcessingLogDefaultRuntimeClient(httpClientsFactory);
        this.edgeRuntimeClient = new MessageProcessingLogEdgeRuntimeClient(httpClientsFactory);
    }

    public MessageProcessingLogClient(
        MessageProcessingLogDefaultRuntimeClient defaultRuntimeClient,
        MessageProcessingLogEdgeRuntimeClient edgeRuntimeClient
    ) {
        this.defaultRuntimeClient = defaultRuntimeClient;
        this.edgeRuntimeClient = edgeRuntimeClient;
    }

    public Pair<List<MessageProcessingLog>, Integer> getMessageProcessingLogsByCustomHeader(
        RequestContext requestContext,
        int top,
        int skip,
        String filter
    ) {
        return this.withRuntime(requestContext).getMessageProcessingLogsByCustomHeader(
            requestContext,
            top,
            skip,
            filter
        );
    }

    public List<MessageProcessingLog> getMessageProcessingLogs(RequestContext requestContext, String integrationFlowName, Date startDate) {
        return this.withRuntime(requestContext).getMessageProcessingLogs(requestContext, integrationFlowName, startDate);
    }

    public List<MessageProcessingLog> getFinishedMessageProcessingLogsWithTraceLevel(
        RequestContext requestContext,
        String integrationFlowName,
        Date startDate
    ) {
        return this.withRuntime(requestContext).getFinishedMessageProcessingLogsWithTraceLevel(
            requestContext,
            integrationFlowName,
            startDate
        );
    }

    public List<MessageProcessingLog> getFinishedMessageProcessingLogsWithTraceLevelByIFlowTechnicalNames(
        RequestContext requestContext,
        List<String> technicalNames,
        Date startDate
    ) {
        return this.withRuntime(requestContext).getFinishedMessageProcessingLogsWithTraceLevelByIFlowTechnicalNames(
            requestContext,
            technicalNames,
            startDate
        );
    }

    public List<MessageProcessingLog> getMessageProcessingLogsByMessageGuids(
        RequestContext requestContext,
        Set<String> messageGuids,
        boolean expandCustomHeaders
    ) {
        return this.withRuntime(requestContext).getMessageProcessingLogsByMessageGuids(
            requestContext,
            messageGuids,
            expandCustomHeaders
        );
    }

    public List<MessageProcessingLog> getMessageProcessingLogsByCorrelationIds(RequestContext requestContext, Set<String> correlationIds) {
        return this.withRuntime(requestContext).getMessageProcessingLogsByCorrelationIds(
            requestContext,
            correlationIds
        );
    }

    public List<MessageProcessingLog> getMessageProcessingLogsByFilter(RequestContext requestContext, String filter, Date leftBoundDate) {
        return this.withRuntime(requestContext).getMessageProcessingLogsByFilter(requestContext, filter, leftBoundDate);
    }

    public List<MessageProcessingLog> getMessageProcessingLogsByFilter(
        RequestContext requestContext,
        int top,
        int skip,
        String filter,
        Date leftBoundDate,
        boolean expandCustomHeaders
    ) {
        return this.withRuntime(requestContext).getMessageProcessingLogsByFilter(
            requestContext,
            top,
            skip,
            filter,
            leftBoundDate,
            expandCustomHeaders
        );
    }

    public List<MessageProcessingLog> getMessageProcessingLogsByFilter(RequestContext requestContext, int top, String filter) {
        return this.withRuntime(requestContext).getMessageProcessingLogsByFilter(requestContext, top, filter);
    }

    public List<MessageProcessingLog> getMessageProcessingLogsByCorrelationId(RequestContext requestContext, String correlationId) {
        return this.withRuntime(requestContext).getMessageProcessingLogsByCorrelationId(requestContext, correlationId);
    }

    public List<MessageProcessingLog> getMessageProcessingLogsByCorrelationIdForTesting(RequestContext requestContext, String correlationId) {
        RequestContext runtimeRequestContext = requestContext.createFromCurrentUsingConnectionPropertiesContainer();
        return this.withRuntime(runtimeRequestContext).getMessageProcessingLogsByCorrelationId(runtimeRequestContext, correlationId);
    }

    public List<MessageProcessingLog> getMessageProcessingLogsByCorrelationIdsAndIFlowNames(RequestContext requestContext, List<String> correlationIds, List<String> technicalNames) {
        return this.withRuntime(requestContext).getMessageProcessingLogsByCorrelationIdsAndIFlowNames(requestContext, correlationIds, technicalNames);
    }

    public Pair<List<MessageProcessingLog>, Integer> getMessageProcessingLogsByFilter(
        RequestContext requestContext,
        int top,
        int skip,
        String filter,
        boolean expandCustomHeaders
    ) {
        return this.withRuntime(requestContext).getMessageProcessingLogsByFilter(
            requestContext,
            top,
            skip,
            filter,
            expandCustomHeaders
        );
    }

    public Pair<List<MessageProcessingLog>, Integer> getMessageProcessingLogsByFilterWithSelectedResponseFields(
        RequestContext requestContext,
        int top,
        int skip,
        String filter,
        String responseFields
    ) {
        return this.withRuntime(requestContext).getMessageProcessingLogsByFilterWithSelectedResponseFields(
            requestContext,
            top,
            skip,
            filter,
            responseFields
        );
    }

    public List<CustomHeaderProperty> getCustomHeaderProperties(RequestContext requestContext, String messageGuid) {
        return this.withRuntime(requestContext).getCustomHeaderProperties(
            requestContext,
            messageGuid
        );
    }

    public int getCountOfMessageProcessingLogsByFilter(RequestContext requestContext, String filter) {
        return this.withRuntime(requestContext).getCountOfMessageProcessingLogsByFilter(
            requestContext,
            filter
        );
    }

    public List<MessageProcessingLog> getMessageProcessingLogs(RequestContext requestContext, String resourcePath) {
        if (!requestContext.isDefaultRuntime()) {
            log.debug(
                "#getMessageProcessingLogs edge: requestContext={}, resourcePath={}",
                requestContext,
                resourcePath
            );
            failDueToUnsupportedOperationInEdgeIntegrationCell(requestContext.getRuntimeLocationId());
        }
        return defaultRuntimeClient.getMessageProcessingLogs(requestContext, resourcePath);
    }

    public MessageProcessingLog getMessageProcessingLogByGuid(RequestContext requestContext, String messageGuid) {
        return this.withRuntime(requestContext).getMessageProcessingLogByGuid(
            requestContext,
            messageGuid
        );
    }

    public List<MessageProcessingLogAttachment> getAttachmentsMetadata(RequestContext requestContext, String messageGuid) {
        return this.withRuntime(requestContext).getAttachmentsMetadata(
            requestContext,
            messageGuid
        );
    }

    public List<MessageProcessingLogAttachment> getMessageStoreEntriesPayloads(RequestContext requestContext, String messageGuid) {
        return this.withRuntime(requestContext).getMessageStoreEntriesPayloads(
            requestContext,
            messageGuid
        );
    }

    public MessageProcessingLogErrorInformation getErrorInformation(RequestContext requestContext, String messageId) {
        return this.withRuntime(requestContext).getErrorInformation(requestContext, messageId);
    }

    public String getErrorInformationValue(RequestContext requestContext, String messageGuid) {
        return this.withRuntime(requestContext).getErrorInformationValue(
            requestContext,
            messageGuid
        );
    }

    public byte[] getAttachment(RequestContext requestContext, String attachmentId) {
        return this.withRuntime(requestContext).getAttachment(
            requestContext,
            attachmentId
        );
    }

    public byte[] getPersistedAttachment(RequestContext requestContext, String attachmentId) {
        return this.withRuntime(requestContext).getPersistedAttachment(
            requestContext,
            attachmentId
        );
    }

    public List<MessageProcessingLogRun> getRunsMetadata(RequestContext requestContext, String messageGuid) {
        return this.withRuntime(requestContext).getRunsMetadata(
            requestContext,
            messageGuid
        );
    }

    public List<MessageProcessingLogRunStep> getRunSteps(RequestContext requestContext, String runId) {
        return this.withRuntime(requestContext).getRunSteps(
            requestContext,
            runId
        );
    }

    public MessageProcessingLogRunStep.TraceMessage getTraceMessage(
        RequestContext requestContext,
        MessageProcessingLogRunStepSearchCriteria runStepSearchCriteria,
        MessageProcessingLogRunStep runStep
    ) {
        return this.withRuntime(requestContext).getTraceMessage(
            requestContext,
            runStepSearchCriteria,
            runStep
        );
    }

    public byte[] getPayloadForMessage(RequestContext requestContext, String traceId) {
        return this.withRuntime(requestContext).getPayloadForMessage(
            requestContext,
            traceId
        );
    }

    private MessageProcessingLogAbstractClient withRuntime(RequestContext requestContext) {
        if (requestContext.isDefaultRuntime()) {
            return defaultRuntimeClient;
        }
        return edgeRuntimeClient;
    }

    private void failDueToUnsupportedOperationInEdgeIntegrationCell(String runtimeLocationId) {
        throw new UnsupportedOperationException(String.format(
            "Operation is not supported for edge integration cell with runtimeLocationId %s",
            runtimeLocationId
        ));
    }
}
