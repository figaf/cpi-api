package com.figaf.integration.cpi.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.data_store_entry.DataStoreEntry;
import com.figaf.integration.cpi.entity.data_store_entry.DataStoreEntryPayload;
import com.figaf.integration.cpi.response_parser.DataStoreEntryParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Arsenii Istlentev
 */
@Slf4j
public class DataStoreEntriesClient extends CpiBaseClient {

    public DataStoreEntriesClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    public List<DataStoreEntry> getDataStoreEntriesByMessageId(RequestContext requestContext, String messageId) {
        log.debug("#getDataStoreEntriesByMessageId(RequestContext requestContext, String messageId): {}, {}", requestContext, messageId);
        String resourcePath = String.format(API_DATA_STORE_ENTRIES, messageId);
        try {
            JSONArray dataStoreEntriesJsonArray = callRestWs(
                    requestContext,
                    resourcePath,
                    response -> new JSONObject(response).getJSONObject("d").getJSONArray("results")
            );

            List<DataStoreEntry> dataStoreEntries = new ArrayList<>();
            for (int ind = 0; ind < dataStoreEntriesJsonArray.length(); ind++) {
                JSONObject dataStoreEntryElement = dataStoreEntriesJsonArray.getJSONObject(ind);
                DataStoreEntry dataStoreEntry = DataStoreEntryParser.fillDataStoreEntry(dataStoreEntryElement);
                dataStoreEntries.add(dataStoreEntry);
            }

            return dataStoreEntries;
        } catch (HttpClientErrorException.NotFound ex) {
            log.debug("Can't find dataStoreEntries for {}", messageId);
            return Collections.emptyList();
        } catch (HttpStatusCodeException ex) {
            log.error("Can't get dataStoreEntries for {}: {}", messageId, ex);
            return Collections.emptyList();
        } catch (JSONException ex) {
            log.error("Error occurred while parsing response: " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while parsing response: " + ex.getMessage(), ex);
        }
    }

    public DataStoreEntryPayload getDataStoreEntryPayload(RequestContext requestContext, DataStoreEntry dataStoreEntry) {
        return getDataStoreEntryPayload(requestContext, dataStoreEntry.getId(), dataStoreEntry.getDataStoreName(), dataStoreEntry.getIntegrationFlow(), dataStoreEntry.getType());
    }

    public DataStoreEntryPayload getDataStoreEntryPayload(RequestContext requestContext, String dataStoreEntryId, String dataStoreEntryName, String integrationFlow, String type) {
        log.debug("#getDataStoreEntryPayload(RequestContext requestContext, String dataStoreEntryId, String dataStoreEntryName, String integrationFlow, String type): {}, {}, {}, {}, {}",
                requestContext, dataStoreEntryId, dataStoreEntryName, integrationFlow, type
        );
        try {
            byte[] fullArchive = executeGet(
                    requestContext,
                    String.format(
                            API_DATA_STORE_ENTRY_PAYLOAD,
                            dataStoreEntryId.replace(" ", "%20"),
                            dataStoreEntryName,
                            integrationFlow,
                            StringUtils.defaultString(type, "")
                    ),
                    response -> response,
                    byte[].class
            );
            return DataStoreEntryParser.parseDataStoreEntryPayload(fullArchive);
        } catch (Exception ex) {
            throw new ClientIntegrationException(ex);
        }
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}
