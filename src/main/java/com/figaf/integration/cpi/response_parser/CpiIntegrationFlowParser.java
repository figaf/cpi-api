package com.figaf.integration.cpi.response_parser;

import com.figaf.integration.common.utils.Utils;
import com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifact;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Arsenii Istlentev
 */
public class CpiIntegrationFlowParser {

    public static List<CpiArtifact> buildCpiArtifacts(
        String packageTechnicalName,
        String packageDisplayedName,
        String packagePackageExternalId,
        Set<String> artifactTypes,
        String responseBody
    ) {
        JSONObject response = new JSONObject(responseBody);
        JSONArray iFlowsJsonArray = response.getJSONObject("d").getJSONArray("results");

        List<CpiArtifact> artifacts = new ArrayList<>();

        Set<String> synchronizedTypes = new HashSet<>();

        for (String artifactType : artifactTypes) {
            switch (artifactType) {
                case "CPI_IFLOW":
                    synchronizedTypes.add("IFlow");
                    break;
                case "VALUE_MAPPING":
                    synchronizedTypes.add("ValueMapping");
                    break;
                default:
                    throw new IllegalArgumentException("Unexpected object type " + artifactType);
            }
        }

        for (int ind = 0; ind < iFlowsJsonArray.length(); ind++) {
            JSONObject iFlowElement = iFlowsJsonArray.getJSONObject(ind);

            String type = Utils.optString(iFlowElement, "Type");

            if (!synchronizedTypes.contains(type)) {
                continue;
            }

            CpiArtifact artifact = new CpiArtifact();
            artifact.setExternalId(iFlowElement.getString("reg_id"));
            artifact.setPackageName(packageDisplayedName);
            artifact.setTechnicalName(iFlowElement.getString("Name"));
            artifact.setDisplayedName(iFlowElement.getString("DisplayName"));
            artifact.setVersion(Utils.optString(iFlowElement, "Version"));
            artifact.setCreationDate(
                new Timestamp(Long.parseLong(iFlowElement.getString("CreatedAt").replaceAll("[^0-9]", "")))
            );
            artifact.setCreatedBy(Utils.optString(iFlowElement, "CreatedBy"));
            String modifiedAt = Utils.optString(iFlowElement, "ModifiedAt");
            artifact.setModificationDate(modifiedAt != null
                ? new Timestamp(Long.parseLong(modifiedAt.replaceAll("[^0-9]", "")))
                : null
            );
            artifact.setModifiedBy(iFlowElement.getString("ModifiedBy"));
            artifact.setDescription(Utils.optString(iFlowElement, "Description"));

            switch (type) {
                case "IFlow":
                    artifact.setTrackedObjectType("CPI_IFLOW");
                    break;
                case "ValueMapping":
                    artifact.setTrackedObjectType("VALUE_MAPPING");
                    break;
                default:
                    throw new IllegalArgumentException("Unexpected object type " + type);
            }

            artifact.setPackageTechnicalName(packageTechnicalName);
            artifact.setPackageExternalId(packagePackageExternalId);


            artifacts.add(artifact);
        }
        return artifacts;
    }

    public static String retrieveDeployStatus(String body) {
        JSONObject response = new JSONObject(body);
        return (String) response.get("status");
    }

}
