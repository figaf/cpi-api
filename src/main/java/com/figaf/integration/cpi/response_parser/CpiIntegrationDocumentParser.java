package com.figaf.integration.cpi.response_parser;

import com.figaf.integration.common.utils.Utils;
import com.figaf.integration.cpi.entity.designtime_artifacts.CpiIntegrationDocument;
import com.figaf.integration.cpi.utils.CpiApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Klochkov Sergey
 */

@Slf4j
public class CpiIntegrationDocumentParser {

    public static List<CpiIntegrationDocument> buildCpiIntegrationDocuments(String documentType, String responseBody) {
        JSONObject response = new JSONObject(responseBody);
        JSONArray documentsJsonArray = response.getJSONObject("d").getJSONArray("results");

        List<CpiIntegrationDocument> documents = new ArrayList<>();

        for (int ind = 0; ind < documentsJsonArray.length(); ind++) {
            JSONObject documentElement = documentsJsonArray.getJSONObject(ind);

            CpiIntegrationDocument document = new CpiIntegrationDocument();
            document.setTrackedObjectType(documentType);
            document.setExternalId(documentElement.getString("reg_id"));
            document.setTechnicalName(documentElement.getString("TechnicalName"));
            document.setDisplayedName(documentElement.getString("DisplayName"));
            document.setVersion(Utils.optString(documentElement, "Version"));
            document.setCreationDate(CpiApiUtils.parseDate(documentElement.getString("CreatedAt")));
            document.setCreatedBy(Utils.optString(documentElement, "CreatedBy"));
            document.setModificationDate(CpiApiUtils.parseDate(Utils.optString(documentElement, "ModifiedAt")));
            document.setModifiedBy(documentElement.getString("ModifiedBy"));
            document.setDescription(Utils.optString(documentElement, "Description"));

            switch (documentType) {
                case "FILE_DOCUMENT":
                    document.setFileName(documentElement.getString("FileName"));
                    if (documentElement.get("ContentType") != null) {
                        document.setContentType(documentElement.get("ContentType").toString()); //TODO IRT-929 check
                    } else {
                        document.setContentType(null);
                    }
                    break;
                case "URL_DOCUMENT":
                    document.setUrl(documentElement.getString("Url"));
                    break;
                default:
                    throw new IllegalArgumentException("Unexpected object type " + documentType);
            }

            documents.add(document);
        }
        return documents;
    }

}
