package com.figaf.integration.cpi.response_parser;

import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.common.utils.Utils;
import com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifact;
import com.figaf.integration.cpi.entity.designtime_artifacts.CpiArtifactType;
import com.figaf.integration.cpi.utils.CpiApiUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.function.Function.identity;

/**
 * @author Arsenii Istlentev
 */
public class CpiRuntimeArtifactParser {

    private CpiRuntimeArtifactParser() {
    }

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
                JSONObject jsonObject = new JSONObject(result);
                String taskId = jsonObject.optString("taskId");
                if (StringUtils.isNotEmpty(taskId)) {
                    return taskId;
                }
                JSONObject validationResultModel = jsonObject.optJSONObject("validationResultModel");
                if (validationResultModel == null) {
                    throw new ClientIntegrationException(format("Unexpected error: can't find neither 'taskId' nor 'validationResultModel' properties in the response: %s", jsonObject));
                }

                List<String> errors = new ArrayList<>();
                JSONArray problemsArray = validationResultModel.optJSONArray("problems");
                for (int i = 0; i < problemsArray.length(); i++) {
                    JSONObject problemObject = problemsArray.getJSONObject(i);
                    String severity = problemObject.optString("severity");
                    if ("Error".equalsIgnoreCase(severity)) {
                        errors.add(format("%s [%s]: %s", problemObject.optString("location"), problemObject.optString("elementId"), problemObject.optString("message")));
                    }
                }
                if (!errors.isEmpty()) {
                    throw new ClientIntegrationException(format("IFlow validation failed because of the following errors:\n%s", StringUtils.join(errors, ";\n")));
                } else {
                    throw new ClientIntegrationException(format("IFlow validation failed but no errors were found: %s", jsonObject));
                }
            }
            case VALUE_MAPPING:
            case MESSAGE_MAPPING: {
                return result != null ? result.replace("\"", "") : null;
            }
            default: {
                throw new ClientIntegrationException("Unexpected object type: " + objectType);
            }
        }
    }

}
