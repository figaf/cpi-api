package com.figaf.integration.cpi.response_parser;

import com.figaf.integration.common.utils.Utils;
import com.figaf.integration.cpi.entity.designtime_artifacts.IntegrationPackage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Arsenii Istlentev
 */
public class IntegrationPackageParser {

    public static List<IntegrationPackage> buildIntegrationPackages(String body) {
        JSONObject response = new JSONObject(body);
        JSONArray packagesJsonArray = response.getJSONObject("d").getJSONArray("results");

        List<IntegrationPackage> packages = new ArrayList<>();
        for (int ind = 0; ind < packagesJsonArray.length(); ind++) {
            JSONObject packageElement = packagesJsonArray.getJSONObject(ind);

            IntegrationPackage integrationPackage = new IntegrationPackage();
            integrationPackage.setExternalId(packageElement.getString("reg_id"));
            integrationPackage.setTechnicalName(packageElement.getString("TechnicalName"));
            integrationPackage.setDisplayedName(packageElement.getString("DisplayName"));
            integrationPackage.setVersion(Utils.optString(packageElement, "Version"));
            integrationPackage.setCreationDate(
                new Timestamp(Long.parseLong(packageElement.getString("CreatedAt").replaceAll("[^0-9]", "")))
            );
            integrationPackage.setCreatedBy(Utils.optString(packageElement, "CreatedBy"));
            String modifiedAt = Utils.optString(packageElement, "ModifiedAt");
            integrationPackage.setModificationDate(modifiedAt != null
                ? new Timestamp(Long.parseLong(modifiedAt.replaceAll("[^0-9]", "")))
                : null
            );
            integrationPackage.setModifiedBy(packageElement.getString("ModifiedBy"));
            integrationPackage.setVendor(Utils.optString(packageElement, "Vendor"));
            integrationPackage.setShortDescription(Utils.optString(packageElement, "ShortText"));

            packages.add(integrationPackage);
        }

        return packages;
    }
}
