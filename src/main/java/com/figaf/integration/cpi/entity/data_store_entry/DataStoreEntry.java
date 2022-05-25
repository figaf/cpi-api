package com.figaf.integration.cpi.entity.data_store_entry;

import com.figaf.integration.cpi.entity.AdditionalPayloadEntry;
import com.figaf.integration.cpi.entity.AdditionalPayloadType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * @author Arsenii Istlentev
 */
@Getter
@Setter
@ToString
public class DataStoreEntry implements AdditionalPayloadEntry {

    private String id;
    private String dataStoreName;
    private String integrationFlow;
    private String type;
    private String status;
    private String messageId;
    private Date dueAt;
    private Date createdAt;
    private Date retainUntil;

    @Override
    public String getUniqueId() {
        return String.format("%s|%s|%s|%s", id, dataStoreName, integrationFlow, type);
    }

    @Override
    public String getName() {
        return dataStoreName;
    }

    @Override
    public String getModelStepId() {
        return dataStoreName;
    }

    @Override
    public String getActivity() {
        return "data-store-entry";
    }

    @Override
    public Date getDate() {
        return createdAt;
    }

    @Override
    public String getContentType() {
        return "Data store entry";
    }

    @Override
    public AdditionalPayloadType getPayloadType() {
        return AdditionalPayloadType.DATA_STORE_ENTRY;
    }
}
