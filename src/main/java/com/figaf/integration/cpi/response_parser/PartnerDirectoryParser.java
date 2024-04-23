package com.figaf.integration.cpi.response_parser;

import com.figaf.integration.cpi.entity.partner_directory.PartnerDirectoryParameter;
import com.figaf.integration.cpi.entity.partner_directory.enums.TypeOfParam;
import com.figaf.integration.cpi.entity.partner_directory.exception.PartnerDirectoryClientException;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
public class PartnerDirectoryParser {

    public static List<PartnerDirectoryParameter> buildBinaryParameters(JSONArray apiParameters) {
        return buildParameters(apiParameters, PartnerDirectoryParser::createBinaryParameter);
    }

    public static List<PartnerDirectoryParameter> buildStringParameters(JSONArray apiParameters) {
        return buildParameters(apiParameters, PartnerDirectoryParser::createStringParameter);
    }

    public static PartnerDirectoryParameter createBinaryParameter(JSONObject apiParameter) {
        PartnerDirectoryParameter binaryParameter = new PartnerDirectoryParameter();
        seCommonProperties(apiParameter, binaryParameter);
        getOptionalString("ContentType", apiParameter).ifPresent(binaryParameter::setContentType);
        binaryParameter.setType(TypeOfParam.BINARY_PARAMETER);
        return binaryParameter;
    }

    public static PartnerDirectoryParameter createStringParameter(JSONObject apiParameter) {
        PartnerDirectoryParameter stringParameter = new PartnerDirectoryParameter();
        seCommonProperties(apiParameter, stringParameter);
        stringParameter.setType(TypeOfParam.STRING_PARAMETER);
        return stringParameter;
    }

    private static List<PartnerDirectoryParameter> buildParameters(JSONArray apiParameters, Function<JSONObject, PartnerDirectoryParameter> parameterCreator) {
        List<PartnerDirectoryParameter> partnerDirectoryParameters = new ArrayList<>();
        for (int i = 0; i < apiParameters.length(); i++) {
            JSONObject apiParameter = apiParameters.getJSONObject(i);
            if (!Optional.ofNullable(apiParameter).isPresent()) {
                continue;
            }
            try {
                partnerDirectoryParameters.add(parameterCreator.apply(apiParameter));
            } catch (JSONException | PartnerDirectoryClientException e) {
                log.error("Error processing parameter: " + e.getMessage());
            }
        }
        return partnerDirectoryParameters;
    }

    private static void seCommonProperties(JSONObject apiParameter, PartnerDirectoryParameter partnerDirectoryParameter) throws PartnerDirectoryClientException {
        partnerDirectoryParameter.setPid(getMandatoryString("Pid", apiParameter));
        partnerDirectoryParameter.setId(getMandatoryString("Id", apiParameter));

        getOptionalString("Value", apiParameter).ifPresent(partnerDirectoryParameter::setValue);
        getOptionalString("CreatedBy", apiParameter).ifPresent(partnerDirectoryParameter::setCreatedBy);
        getOptionalString("LastModifiedBy", apiParameter).ifPresent(partnerDirectoryParameter::setModifiedBy);

        getOptionalDate("CreatedTime", apiParameter).ifPresent(partnerDirectoryParameter::setCreatedTime);
        getOptionalDate("LastModifiedTime", apiParameter).ifPresent(partnerDirectoryParameter::setModificationDate);
    }

    private static String getMandatoryString(String key, JSONObject apiParameter) throws PartnerDirectoryClientException {
        if (!apiParameter.has(key)) {
            throw new PartnerDirectoryClientException("Missing mandatory field: " + key);
        }
        return apiParameter.getString(key);
    }

    private static Optional<String> getOptionalString(String key, JSONObject apiParameter) {
        return apiParameter.has(key) ? Optional.of(apiParameter.getString(key)) : Optional.empty();
    }

    private static Optional<Date> getOptionalDate(String key, JSONObject apiParameter) {
        if (apiParameter.has(key)) {
            String jsonDate = apiParameter.getString(key);
            String dateDigits = jsonDate.replaceAll("[^0-9]", "");
            if (!dateDigits.isEmpty()) {
                return Optional.of(new Date(Long.parseLong(dateDigits)));
            }
        }
        return Optional.empty();
    }
}
