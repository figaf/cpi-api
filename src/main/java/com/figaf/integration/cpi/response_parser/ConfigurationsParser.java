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

    public static CpiConfigurations buildCpiIsConfigurations(String body) {

        CpiConfigurations configurations = new CpiConfigurations();
        JSONArray jsonArray = new JSONArray(body);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jSONObject = jsonArray.getJSONObject(i);
            if (BUILD_NUMBER.equals(jSONObject.optString(KEY))) {
                configurations.setIsBuildNumber(jSONObject.getString(VALUE));
            }
            if (jSONObject.optString(KEY, "").equals("capabilityVersions")) {
                JSONObject valueObj = jSONObject.getJSONObject(VALUE);
                String capabilityName = valueObj.optString("capabilityName", "");
                String capabilityVersion = valueObj.optString("capabilityVersion", "");
                switch (capabilityName) {
                    case "{com.sap.it.spc.ibp.workspace.configuration.common.i18n>TMN_APPLICATION_TITLE_ISUITE}":
                        configurations.setCiBuildNumber(capabilityVersion);
                        break;
                    case "{com.sap.it.spc.ibp.workspace.configuration.common.i18n>CLOUD_INTEGRATION_RUNTIME}":
                        configurations.setCiRuntimeBuildNumber(capabilityVersion);
                        break;
                    case "API Management":
                        configurations.setApiManagementBuildNumber(capabilityVersion);
                        break;
                    case "{com.sap.it.spc.smarti.ica.common.i18n>IA_APPLICATION_TITLE}":
                        configurations.setIaBuildNumber(capabilityVersion);
                        break;
                    default:
                        log.warn("Unexpected capabilityName:" + capabilityName);
                        break;
                }
            }
        }
        log.debug("#buildCpiIsConfigurations: configurations={}", configurations);
        return configurations;
    }

    public static CpiConfigurations buildCpiNonIsConfigurations(String body) {

        CpiConfigurations configurations = new CpiConfigurations();
        JSONArray jsonArray = new JSONArray(body);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jSONObject = jsonArray.getJSONObject(i);
            if (BUILD_NUMBER.equals(jSONObject.optString(KEY))) {
                configurations.setNonISBuildNumber(jSONObject.getString(VALUE));
            }
        }
        log.debug("#buildCpiNonIsConfigurations: configurations={}", configurations);
        return configurations;
    }
}