package com.figaf.integration.cpi.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.message_processing.CustomHeaderProperty;
import com.figaf.integration.cpi.entity.message_processing.MessageProcessingLog;
import com.figaf.integration.cpi.entity.message_processing.MessageProcessingLogAttachment;
import com.figaf.integration.cpi.entity.message_processing.MessageProcessingLogRun;
import org.apache.commons.lang3.tuple.Pair;
import java.util.List;

/**
 * @author Kostas Charalambous
 */
public abstract class AbstractMessageProcessingLogClient extends CpiBaseClient {

    protected final static String LOCATION = "/location/";

    protected static final String API_MSG_PROC_LOGS = "/api/v1/MessageProcessingLogs";

    protected static final String API_MSG_PROC_LOGS_WITH_PARAMS = "/api/v1/MessageProcessingLogs?$format=json&$orderby=LogEnd&$filter=%s";

    protected static final String API_MSG_PROC_LOG_CUSTOM_HEADER = "/api/v1/MessageProcessingLogCustomHeaderProperties";

    protected static final String QUERY_PARAMS = "$inlinecount=allpages&$format=json&$top=%d&$skip=%d&$orderby=LogEnd desc&$filter=%s";

    protected static final String QUERY_PARAMS_WITH_SELECT = "$inlinecount=allpages&$format=json&$top=%d&$skip=%d&$orderby=LogEnd desc&$filter=%s&$select=%s";

    protected static final String QUERY_PARAMS_ORDERED = "$inlinecount=allpages&$format=json&$top=%d&$orderby=LogEnd&$filter=%s";

    protected static final String QUERY_PARAMS_CUSTOM_HEADER = "$inlinecount=allpages&$format=json&$top=%d&$skip=%d&$expand=Log&$filter=%s";

    protected static final String API_MSG_PROC_LOGS_CUSTOM_HEADER = "/api/v1/MessageProcessingLogs('%s')/CustomHeaderProperties?$format=json";

    protected final static String FILTER = "$filter=%s";

    protected static final String API_MSG_PROC_LOGS_COUNT = "/api/v1/MessageProcessingLogs/$count";

    protected static final String API_MSG_PROC_LOGS_ID = "/api/v1/MessageProcessingLogs('%s')?$format=json";

    protected static final String API_MSG_PROC_LOGS_ORDERED = "/api/v1/MessageProcessingLogs?$inlinecount=allpages&$format=json&$top=%d&$orderby=LogEnd&$filter=%s";

    protected static final String API_MSG_PROC_LOGS_PAGINATED_WITH_SELECTED_RESPONSE_FIELDS = "/api/v1/MessageProcessingLogs?$inlinecount=allpages&$format=json&$top=%d&$skip=%d&$orderby=LogEnd desc&$filter=%s&$select=%s";

    protected static final String API_MSG_PROC_LOGS_PAGINATED = "/api/v1/MessageProcessingLogs?$inlinecount=allpages&$format=json&$top=%d&$skip=%d&$orderby=LogEnd desc&$filter=%s";

    protected static final String API_MSG_PROC_LOGS_ATTACHMENTS = "/api/v1/MessageProcessingLogs('%s')/Attachments?$format=json";

    protected static final String API_MSG_PROC_LOGS_MESSAGE_STORE_ENTRIES = "/api/v1/MessageProcessingLogs('%s')/MessageStoreEntries?$format=json";

    protected static final String API_MSG_PROC_LOGS_ERROR_INFORMATION = "/api/v1/MessageProcessingLogs('%s')/ErrorInformation?$format=json";

    protected static final String API_MSG_PROC_LOGS_ERROR_INFORMATION_VALUE = "/api/v1/MessageProcessingLogs('%s')/ErrorInformation/$value";

    protected static final String API_MSG_PROC_LOG_ATTACHMENT = "/api/v1/MessageProcessingLogAttachments('%s')/$value";

    protected static final String API_MSG_PROC_LOGS_RUNS = "/api/v1/MessageProcessingLogs('%s')/Runs?$format=json";

    protected static final String API_MSG_PROC_LOG_RUN_STEPS = "/api/v1/MessageProcessingLogRuns('%s')/RunSteps?$format=json&$expand=RunStepProperties&$inlinecount=allpages&$top=%d&$skip=%d";

    protected static final String API_MSG_PROC_LOG_RUN_STEP_TRACE_MESSAGES = "/api/v1/MessageProcessingLogRunSteps(RunId='%s',ChildCount=%d)/TraceMessages?$format=json";

    protected static final String API_TRACE_MESSAGE_PAYLOAD = "/api/v1/TraceMessages(%sL)/$value";

    protected static final String API_TRACE_MESSAGE_EXCHANGE_PROPERTIES = "/api/v1/TraceMessages(%sL)/ExchangeProperties?$format=json";

    protected static final String API_TRACE_MESSAGE_PROPERTIES = "/api/v1/TraceMessages(%sL)/Properties?$format=json";

    protected static final String API_MSG_STORE_ENTRIES_VALUE = "/api/v1/MessageStoreEntries('%s')/$value";

    public AbstractMessageProcessingLogClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    public abstract Pair<List<MessageProcessingLog>, Integer> getMessageProcessingLogsByFilter(
        RequestContext requestContext,
        int top,
        int skip,
        String filter,
        boolean expandCustomHeaders
    );

    public abstract List<MessageProcessingLogAttachment> getAttachmentsMetadata(RequestContext requestContext, String messageGuid);

    public abstract List<MessageProcessingLogAttachment> getMessageStoreEntriesPayloads(RequestContext requestContext, String messageGuid);

    public abstract MessageProcessingLog getMessageProcessingLogByGuid(RequestContext requestContext, String messageGuid);

    public abstract String getErrorInformationValue(RequestContext requestContext, String messageGuid);

    public abstract List<CustomHeaderProperty> getCustomHeaderProperties(RequestContext requestContext, String messageGuid);

    public abstract List<MessageProcessingLogRun> getRunsMetadata(RequestContext requestContext, String messageGuid);

    public abstract int getCountOfMessageProcessingLogsByFilter(RequestContext requestContext, String filter);

    public abstract Pair<List<MessageProcessingLog>, Integer> getMessageProcessingLogsByCustomHeader(RequestContext requestContext, int top, int skip, String filter);

    public abstract Pair<List<MessageProcessingLog>, Integer> getMessageProcessingLogsByFilterWithSelectedResponseFields(
        RequestContext requestContext,
        int top,
        int skip,
        String filter,
        String responseFields
    );

    public abstract List<MessageProcessingLog> getMessageProcessingLogsByFilter(RequestContext requestContext, int top, String filter);
}
