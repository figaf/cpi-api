package com.figaf.integration.cpi.entity.data_store_entry;

import com.figaf.integration.cpi.entity.message_processing.MessageRunStepProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @author Arsenii Istlentev
 */
@Getter
@Setter
@ToString
public class DataStoreEntryPayload {

    private byte[] fullArchive;
    private byte[] body;
    private List<MessageRunStepProperty> headers;
}
