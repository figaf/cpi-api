package com.figaf.integration.cpi.entity;

import java.util.Date;

/**
 * @author Arsenii Istlentev
 */
public interface AdditionalPayloadEntry {

    String getId();
    String getName();
    String getUniqueId();
    String getModelStepId();
    String getActivity();
    String getMessageId();
    Date getDate();
    String getContentType();
    AdditionalPayloadType getPayloadType();
}
