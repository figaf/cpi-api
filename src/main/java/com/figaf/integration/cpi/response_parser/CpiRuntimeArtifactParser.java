package com.figaf.integration.cpi.response_parser;

import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.common.utils.Utils;
import com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifact;
import com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifactType;
import com.figaf.integration.cpi.utils.CpiApiUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;

/**
 * @author Arsenii Istlentev
 */
public class CpiRuntimeArtifactParser {

    private CpiRuntimeArtifactParser() {}

    public static List<CpiArtifact> buildCpiArtifacts(
        String packageTechnicalName,
        String packageDisplayedName,
        String packageExternalId,
        Set<CpiArtifactType> artifactTypes,
        String responseBody
    ) {
        JSONObject response = new JSONObject(responseBody);
        JSONArray artifactsJsonArray = response.getJSONObject("d").getJSONArray("results");

        Map<String, CpiArtifactType> queryTypeTitleToTypeMap = artifactTypes.stream()
            .collect(Collectors.toMap(CpiArtifactType::getQueryTitle, identity()));
        List<CpiArtifact> artifacts = new ArrayList<>();
        for (int ind = 0; ind < artifactsJsonArray.length(); ind++) {
            JSONObject artifactElement = artifactsJsonArray.getJSONObject(ind);

            String type = Utils.optString(artifactElement, "Type");

            if (!queryTypeTitleToTypeMap.containsKey(type)) {
                continue;
            }

            CpiArtifact artifact = new CpiArtifact();
            artifact.setExternalId(artifactElement.getString("reg_id"));
            artifact.setPackageName(packageDisplayedName);
            artifact.setTechnicalName(artifactElement.getString("Name"));
            artifact.setDisplayedName(artifactElement.getString("DisplayName"));
            artifact.setVersion(Utils.optString(artifactElement, "Version"));
            artifact.setCreationDate(CpiApiUtils.parseDate(artifactElement.getString("CreatedAt")));
            artifact.setCreatedBy(Utils.optString(artifactElement, "CreatedBy"));
            artifact.setModificationDate(CpiApiUtils.parseDate(Utils.optString(artifactElement, "ModifiedAt")));
            artifact.setModifiedBy(artifactElement.getString("ModifiedBy"));
            artifact.setDescription(Utils.optString(artifactElement, "Description"));
            artifact.setTrackedObjectType(queryTypeTitleToTypeMap.get(type).getTitle());
            artifact.setPackageTechnicalName(packageTechnicalName);
            artifact.setPackageExternalId(packageExternalId);
            artifacts.add(artifact);
        }
        return artifacts;
    }

    public static String retrieveDeployStatus(String body) {
        JSONObject response = new JSONObject(body);
        return (String) response.get("status");
    }

    public static String retrieveDeployingResult(String result, CpiArtifactType objectType) {
        switch (objectType) {
            case IFLOW:
            case REST_API:
            case SCRIPT_COLLECTION: {
                return new JSONObject(result).getString("taskId");
            }
            case VALUE_MAPPING:
            case SHARED_MESSAGE_MAPPING:{
                return result != null ? result.replace("\"", "") : null;
            }
            default: {
                throw new ClientIntegrationException("Unexpected object type: " + objectType);
            }
        }
    }

}
