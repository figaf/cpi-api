package com.figaf.integration.cpi.client;

import com.figaf.integration.common.client.BaseClient;
import com.figaf.integration.common.entity.ConnectionProperties;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.common.utils.Utils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.message.BasicHeader;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.util.Assert;
import org.springframework.util.Base64Utils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * @author Ilya Nesterov
 */


public abstract class CpiBaseClient extends BaseClient {

    /*
    Package lock:
    Request URL: https://<host>/itspaces/api/1.0/workspace/170d342e95424be6b095e0b55e9a929d
    Request Method: LOCK
     */

    /*
    Package delete:
    https://<host>/itspaces/odata/1.0/workspace.svc/ContentEntities.ContentPackages('IRTTestDummyPackage')
    Request Method: DELETE
    */

    /*
    Package create:
    Request URL: https://<host>/itspaces/odata/1.0/workspace.svc/ContentEntities.ContentPackages
    Request Method: POST
    Request: {"Category":"Integration","SupportedPlatforms":"SAP HANA Cloud Integration","TechnicalName":"hgfhgfhfhgfh","DisplayName":"hgfhgfhfhgfh","ShortText":"hghfghgfhgf","Vendor":"Figaf","Version":"1.0","Description":""}
     */

    /*
    Create IFlow
    Request URL: https://<host>/itspaces/api/1.0/workspace/170d342e95424be6b095e0b55e9a929d/iflows/
    Request Method: POST
    Request: {"name":"IRT_DemoScenario","description":"Copy of IRTTest|DemoScenario","type":"IFlow","id":"IRT_DemoScenario","additionalAttrs":{"source":[""],"target":[""],"productProfile":["iflmap"],"nodeType":["IFLMAP"]}}
    Response: {"id":"752a00da8c1342f9a207ac494089af28","name":"testFlow","tooltip":"testFlow","description":" ","type":"IFlow","entityID":"752a00da8c1342f9a207ac494089af28","additionalAttrs":{"source":[""],"target":[""],"nodeType":["IFLMAP"],"productProfile":["iflmap"]},"semanticVersion":"1.0.0","modifiedAt":1524906592350,"privilegeState":"EDIT_ALLOWED"}
    */

    /*
    Deploy IFlow
    Request URL: https://<host>/itspaces/api/1.0/workspace/170d342e95424be6b095e0b55e9a929d/artifacts/412b1fc4a2e04e459ac291b586252bf6/entities/412b1fc4a2e04e459ac291b586252bf6/iflows/IRT_DemoScenario
    Request Method: DEPLOY
    Response: {"taskId":"3270f4f3-32cd-4061-b113-e60843d76778"}
    */

    /*
    Undeploy IFlow
    Request URL: https://<host>/itspaces/Operations/com.sap.it.nm.commands.deploy.DeleteContentCommand
    Request Method: POST
    Request body: artifactIds=4b645794-c527-486b-9af5-fd564426723b&tenantId=a3bf9677f
     */

    /*
    Deployment status:
    Request URL: https://<host>/itspaces/api/1.0/deploystatus/3270f4f3-32cd-4061-b113-e60843d76778
    Request Method: GET
    Response: {"status":"DEPLOYING"}
     */

    /*
    Delete IFlow
    Request URL: https://<host>/itspaces/api/1.0/workspace/170d342e95424be6b095e0b55e9a929d/artifacts/412b1fc4a2e04e459ac291b586252bf6/entities/412b1fc4a2e04e459ac291b586252bf6/iflows/IRT_DemoScenario
    Request Method: DELETE
     */

    /*
    Unlock IFlow
    Request URL: https://<host>/itspaces/api/1.0/workspace/170d342e95424be6b095e0b55e9a929d/artifacts/412b1fc4a2e04e459ac291b586252bf6/entities/412b1fc4a2e04e459ac291b586252bf6/iflows/IRT_DemoScenario
    Request Method: UNLOCK
     */

    protected static final String API_MSG_PROC_LOGS = "/api/v1/MessageProcessingLogs?$format=json&$orderby=LogEnd&$filter=%s";
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
    protected static final String API_MSG_PROC_LOG_RUN_STEP_PROPERTIES = "/api/v1/MessageProcessingLogRunSteps(RunId='%s',ChildCount=%d)/RunStepProperties?$format=json";
    protected static final String API_MSG_PROC_LOG_RUN_STEP_TRACE_MESSAGES = "/api/v1/MessageProcessingLogRunSteps(RunId='%s',ChildCount=%d)/TraceMessages?$format=json";
    protected static final String API_TRACE_MESSAGE_PAYLOAD = "/api/v1/TraceMessages(%sL)/$value";
    protected static final String API_TRACE_MESSAGE_PROPERTIES = "/api/v1/TraceMessages(%sL)/Properties?$format=json";
    protected static final String API_TRACE_MESSAGE_EXCHANGE_PROPERTIES = "/api/v1/TraceMessages(%sL)/ExchangeProperties?$format=json";
    protected static final String API_MSG_STORE_ENTRIES_VALUE = "/api/v1/MessageStoreEntries('%s')/$value";
    protected static final String API_DATA_STORE_ENTRIES = "/api/v1/DataStoreEntries?messageid=%s&$format=json";
    protected static final String API_DATA_STORE_ENTRY_PAYLOAD = "/api/v1/DataStoreEntries(Id='%s',DataStoreName='%s',IntegrationFlow='%s',Type='%s')/$value";


    public CpiBaseClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    protected String getCsrfToken(ConnectionProperties connectionProperties, HttpClient httpClient) {
        return getCsrfToken(connectionProperties, "/api/v1/", httpClient);
    }


    protected String getCsrfToken(ConnectionProperties connectionProperties, String path, HttpClient httpClient) {
        getLogger().debug("#getCsrfToken(ConnectionProperties connectionProperties, String path, HttpClient httpClient): {}, {}, {}", connectionProperties, path, httpClient);

        Assert.notNull(connectionProperties, "connectionProperties must be not null!");
        Assert.notNull(httpClient, "httpClient must be not null!");

        HttpResponse headResponse = null;
        try {

            UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance()
                .scheme(connectionProperties.getProtocol())
                .host(connectionProperties.getHost())
                .path(path);

            if (StringUtils.isNotEmpty(connectionProperties.getPort())) {
                uriBuilder.port(connectionProperties.getPort());
            }

            URI uri = uriBuilder.build().toUri();


            HttpGet getRequest = new HttpGet(uri);
            getRequest.setHeader("X-CSRF-Token", "Fetch");
            getRequest.setHeader(createBasicAuthHeader(connectionProperties));

            headResponse = httpClient.execute(getRequest);

            if (headResponse == null) {
                throw new ClientIntegrationException("Couldn't fetch token: response is null.");
            }

            if (headResponse.getStatusLine().getStatusCode() != 200) {
                throw new ClientIntegrationException(String.format(
                    "Couldn't fetch token. Code: %d, Message: %s",
                    headResponse.getStatusLine().getStatusCode(),
                    IOUtils.toString(headResponse.getEntity().getContent(), StandardCharsets.UTF_8))
                );
            }

            return headResponse.getFirstHeader("X-CSRF-Token").getValue();

        } catch (Exception ex) {
            getLogger().error("Error occurred while fetching csrf token: " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while fetching csrf token: " + ex.getMessage(), ex);
        } finally {
            HttpClientUtils.closeQuietly(headResponse);
        }
    }

    protected <R> R callRestWs(
        RequestContext requestContext,
        String resourcePath,
        CheckedFunction<String, R, JSONException> responseExtractor
    ) {
        return callRestWs(requestContext, resourcePath, responseExtractor, String.class);
    }

    protected <R, T> R callRestWs(
        RequestContext requestContext,
        String resourcePath,
        CheckedFunction<T, R, JSONException> responseExtractor,
        Class<T> bodyType
    ) {
        try {
            ConnectionProperties connectionProperties = requestContext.getConnectionProperties();
            RestTemplate restTemplate = getOrCreateRestTemplateWrapperSingletonWithInterceptors(requestContext);
            final String url = connectionProperties.getUrlRemovingDefaultPortIfNecessary() + resourcePath;
            T response = restTemplate.getForObject(url, bodyType);
            return responseExtractor.apply(response);
        } catch (JSONException ex) {
            getLogger().error("Error occurred while parsing response: " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while parsing response: " + ex.getMessage(), ex);
        }
    }

    public static Header createBasicAuthHeader(ConnectionProperties connectionProperties) throws UnsupportedEncodingException {
        return new BasicHeader(
            "Authorization",
            String.format(
                "Basic %s",
                Base64Utils.encodeToString(
                    (connectionProperties.getUsername() + ":" + connectionProperties.getPassword()).getBytes(StandardCharsets.UTF_8)
                )
            )
        );
    }

    protected Header createCsrfTokenHeader(String csrfToken) {
        return new BasicHeader("X-CSRF-Token", csrfToken);
    }

    protected static String optString(JSONObject json, String key) {
        return Utils.optString(json, key);
    }

    protected abstract Logger getLogger();

    @FunctionalInterface
    public interface CheckedFunction<T, R, E extends Throwable> {
        R apply(T t) throws E;
    }

}
