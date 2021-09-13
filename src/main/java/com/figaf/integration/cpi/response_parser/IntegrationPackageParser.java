package com.figaf.integration.cpi.response_parser;

import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.common.utils.Utils;
import com.figaf.integration.cpi.entity.designtime_artifacts.IntegrationPackage;
import com.figaf.integration.cpi.utils.CpiApiUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Arsenii Istlentev
 */
public class IntegrationPackageParser {

    public static List<IntegrationPackage> buildIntegrationPackages(String body) {
        try {
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
                integrationPackage.setCreationDate(CpiApiUtils.parseDate(packageElement.getString("CreatedAt")));
                integrationPackage.setCreatedBy(Utils.optString(packageElement, "CreatedBy"));
                integrationPackage.setModificationDate(CpiApiUtils.parseDate(Utils.optString(packageElement, "ModifiedAt")));
                integrationPackage.setModifiedBy(packageElement.getString("ModifiedBy"));
                integrationPackage.setVendor(Utils.optString(packageElement, "Vendor"));
                integrationPackage.setShortDescription(Utils.optString(packageElement, "ShortText"));

                packages.add(integrationPackage);
            }

            return packages;
        } catch (JSONException ex) {
            throw new ClientIntegrationException(String.format("Can't parse json. Probably it's authentication issue. Body: %s", body), ex);
        }
    }
}
