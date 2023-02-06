package com.figaf.integration.cpi.response_parser;

import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.common.utils.Utils;
import com.figaf.integration.cpi.entity.data_store_entry.DataStoreEntry;
import com.figaf.integration.cpi.entity.data_store_entry.DataStoreEntryPayload;
import com.figaf.integration.cpi.entity.message_processing.MessageRunStepProperty;
import com.figaf.integration.cpi.entity.message_processing.PropertyType;
import com.figaf.integration.cpi.utils.CpiApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Arsenii Istlentev
 */
@Slf4j
public class DataStoreEntryParser {

    public static DataStoreEntry fillDataStoreEntry(JSONObject dataStoreEntryElement) {
        DataStoreEntry dataStoreEntry = new DataStoreEntry();
        dataStoreEntry.setId(Utils.optString(dataStoreEntryElement, "Id"));
        dataStoreEntry.setDataStoreName(Utils.optString(dataStoreEntryElement, "DataStoreName"));
        dataStoreEntry.setIntegrationFlow(Utils.optString(dataStoreEntryElement, "IntegrationFlow"));
        dataStoreEntry.setType(Utils.optString(dataStoreEntryElement, "Type"));
        dataStoreEntry.setStatus(Utils.optString(dataStoreEntryElement, "Status"));
        dataStoreEntry.setMessageId(Utils.optString(dataStoreEntryElement, "MessageId"));
        dataStoreEntry.setDueAt(CpiApiUtils.parseDate(Utils.optString(dataStoreEntryElement, "DueAt")));
        dataStoreEntry.setCreatedAt(CpiApiUtils.parseDate(Utils.optString(dataStoreEntryElement, "CreatedAt")));
        dataStoreEntry.setRetainUntil(CpiApiUtils.parseDate(Utils.optString(dataStoreEntryElement, "RetainUntil")));
        return dataStoreEntry;
    }

    public static DataStoreEntryPayload parseDataStoreEntryPayload(byte[] fullArchive) {
        DataStoreEntryPayload dataStoreEntryPayload = new DataStoreEntryPayload();
        dataStoreEntryPayload.setFullArchive(fullArchive);
        try (ByteArrayInputStream bais = new ByteArrayInputStream(fullArchive);
             ZipInputStream zipIn = new ZipInputStream(bais)
        ) {
            ZipEntry sourceEntry = zipIn.getNextEntry();
            while (sourceEntry != null) {
                switch (sourceEntry.getName()) {
                    case "body":
                        byte[] body = IOUtils.toByteArray(zipIn);
                        dataStoreEntryPayload.setBody(body);
                        break;
                    case "headers.prop":
                        Properties properties = new Properties();
                        properties.load(zipIn);
                        List<MessageRunStepProperty> traceMessageProperties = new ArrayList<>();
                        for (Object propertyKey : properties.keySet()) {
                            if (propertyKey instanceof String) {
                                String propertyKeyString = (String) propertyKey;
                                traceMessageProperties.add(new MessageRunStepProperty(
                                    PropertyType.TRACE_MESSAGE_HEADER,
                                    propertyKeyString,
                                    properties.getProperty(propertyKeyString)
                                ));
                            }
                        }
                        dataStoreEntryPayload.setHeaders(traceMessageProperties);
                        break;
                }
                zipIn.closeEntry();
                sourceEntry = zipIn.getNextEntry();
            }
            return dataStoreEntryPayload;
        } catch (Exception ex) {
            log.error("Error occurred while parsing data store entry: " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while parsing data store entry: " + ex.getMessage(), ex);
        }
    }

}
