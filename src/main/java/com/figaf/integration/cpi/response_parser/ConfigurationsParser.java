package com.figaf.integration.cpi.response_parser;

import com.figaf.integration.cpi.entity.configuration.CpiConfigurations;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;


/**
 * @author Kostas Charalambous
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConfigurationsParser {

    private static final String VALUE = "value";
    private static final String KEY = "key";
    private static final String BUILD_NUMBER = "buildNumber";

    public static CpiConfigurations parseConfigurationsFromJsonString(String body) {
        CpiConfigurations configurations = new CpiConfigurations();
        JSONArray jsonArray = new JSONArray(body);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jSONObject = jsonArray.getJSONObject(i);
            if (BUILD_NUMBER.equals(jSONObject.optString(KEY))) {
                configurations.setTenantBuildNumber(jSONObject.getString(VALUE));
            }
            if (jSONObject.optString(KEY, "").equals("capabilityVersions")) {
                JSONObject valueObj = jSONObject.getJSONObject(VALUE);
                String capabilityName = valueObj.optString("capabilityName", "");
                String capabilityVersion = valueObj.optString("capabilityVersion", "");
                switch (capabilityName) {
                    case "{com.sap.it.spc.ibp.workspace.configuration.common.i18n>TMN_APPLICATION_TITLE_ISUITE}":
                        configurations.setCloudIntegrationBuildNumber(capabilityVersion);
                        break;
                    case "{com.sap.it.spc.ibp.workspace.configuration.common.i18n>CLOUD_INTEGRATION_RUNTIME}":
                        configurations.setCloudIntegrationRunTimeBuildNumber(capabilityVersion);
                        break;
                    case "API Management":
                        configurations.setApiManagementBuildNumber(capabilityVersion);
                        break;
                    case "{com.sap.it.spc.smarti.ica.common.i18n>IA_APPLICATION_TITLE}":
                        configurations.setIntegrationAdvisorBuildNumber(capabilityVersion);
                        break;
                    default:
                        log.warn("Unexpected capabilityName:" + capabilityName);
                        break;
                }
            }
        }
        log.debug("parsed configurations: {}", configurations);
        return configurations;
    }
}