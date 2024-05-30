package com.figaf.integration.cpi.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.partner_directory.*;
import com.figaf.integration.cpi.entity.partner_directory.enums.TypeOfParam;
import com.figaf.integration.cpi.response_parser.PartnerDirectoryParser;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static com.figaf.integration.cpi.entity.partner_directory.enums.TypeOfParam.BINARY_PARAMETER;
import static com.figaf.integration.cpi.entity.partner_directory.enums.TypeOfParam.STRING_PARAMETER;

@Slf4j
public class PartnerDirectoryClient extends CpiBaseClient {

    public PartnerDirectoryClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    public List<PartnerDirectoryParameter> retrieveBinaryParametersMetadata(RequestContext requestContext, BinaryParameterFilterRequest binaryParameterFilterRequest) {
        log.debug("#retrieveBinaryParametersMetadata(RequestContext requestContext): {}", requestContext);
        JSONArray apiParameters;
        try {
            apiParameters = callRestWs(
                requestContext,
                String.format(API_BINARY_PARAMETERS_META_DATA, binaryParameterFilterRequest.createFilter()),
                response -> new JSONObject(response).getJSONObject("d").getJSONArray("results")
            );
        } catch (Exception e) {
            String errorMsg = String.format("Couldn't fetch binary parameters metadata: %s", e.getMessage());
            throw new ClientIntegrationException(errorMsg, e);
        }
        return PartnerDirectoryParser.buildBinaryParameters(apiParameters);
    }

    public Optional<PartnerDirectoryParameter> retrieveBinaryParameter(String id, String pid, RequestContext requestContext) {
        log.debug("#retrieveBinaryParameter(String id, String pid, RequestContext requestContext): {}, {}, {}", id, pid, requestContext);
        Optional<JSONObject> apiParameter = retrieveApiParameter(id, pid, API_BINARY_PARAMETER, requestContext);
        return apiParameter.map(PartnerDirectoryParser::createBinaryParameter);
    }

    public List<PartnerDirectoryParameter> retrieveStringParameters(RequestContext requestContext) {
        log.debug("#retrieveStringParameters(RequestContext requestContext): {}", requestContext);
        JSONArray apiParameters;
        try {
            apiParameters = callRestWs(
                requestContext,
                API_STRING_PARAMETERS,
                response -> new JSONObject(response).getJSONObject("d").getJSONArray("results")
            );

        } catch (Exception e) {
            String errorMsg = String.format("Couldn't fetch string parameters: %s", e.getMessage());
            throw new ClientIntegrationException(errorMsg, e);
        }

        return PartnerDirectoryParser.buildStringParameters(apiParameters);
    }

    public Optional<PartnerDirectoryParameter> retrieveStringParameter(String id, String pid, RequestContext requestContext) {
        log.debug("#retrieveStringParameter(String id, String pid, RequestContext requestContext): {}, {}, {}", id, pid, requestContext);
        Optional<JSONObject> apiParameter = retrieveApiParameter(id, pid, API_STRING_PARAMETER, requestContext);
        return apiParameter.map(PartnerDirectoryParser::createStringParameter);
    }

    public PartnerDirectoryParameter createBinaryParameter(BinaryParameterCreationRequest binaryParameterCreationRequest, RequestContext requestContext) {
        log.debug(
            "#createBinaryParameter(BinaryParameterCreationRequest binaryParameterCreationRequest, RequestContext requestContext): {}, {}",
            binaryParameterCreationRequest,
            requestContext
        );
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Accept", "application/json");

        return executeMethodPublicApiAppendingCustomHeaders(
            requestContext,
            API_BINARY_PARAMETERS_CREATION,
            binaryParameterCreationRequest,
            HttpMethod.POST,
            httpHeaders,
            (responseEntity) -> createPartnerDirectoryParameter(
                responseEntity,
                binaryParameterCreationRequest.getId(),
                binaryParameterCreationRequest.getPid(),
                BINARY_PARAMETER
            )
        );
    }

    public PartnerDirectoryParameter createStringParameter(StringParameterCreationRequest stringParameterCreationRequest, RequestContext requestContext) {
        log.debug(
            "#createStringParameter(StringParameterCreationRequest stringParameterCreationRequest, RequestContext requestContext): {}, {}",
            stringParameterCreationRequest,
            requestContext
        );
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Accept", "application/json");

        return executeMethodPublicApiAppendingCustomHeaders(
            requestContext,
            API_STRING_PARAMETER_CREATION,
            stringParameterCreationRequest,
            HttpMethod.POST,
            httpHeaders,
            (responseEntity) -> createPartnerDirectoryParameter(
                responseEntity,
                stringParameterCreationRequest.getId(),
                stringParameterCreationRequest.getPid(),
                STRING_PARAMETER
            )
        );
    }

    public void updateBinaryParameter(
        String id,
        String pid,
        BinaryParameterUpdateRequest binaryParameterUpdateRequest,
        RequestContext requestContext
    ) {
        log.debug(
            "#updateBinaryParameter(String id, String pid, BinaryParameterUpdateRequest binaryParameterUpdateRequest, RequestContext requestContext: {}, {}, {}, {}",
            id,
            pid,
            binaryParameterUpdateRequest,
            requestContext
        );

        executeMethodPublicApi(
            requestContext,
            String.format(API_BINARY_PARAMETERS_MANAGE, pid, id),
            binaryParameterUpdateRequest,
            HttpMethod.PUT,
            (responseEntity) -> {
                handleResponse(
                    responseEntity,
                    id,
                    pid,
                    "Update"
                );
                return null;
            }
        );
    }

    public void updateStringParameter(
        String id,
        String pid,
        StringParameterUpdateRequest stringParameterUpdateRequest,
        RequestContext requestContext
    ) {
        log.debug(
            "#updateStringParameter(String id, String pid, StringParameterUpdateRequest stringParameterUpdateRequest, RequestContext requestContext: {}, {}, {}, {}",
            id,
            pid,
            stringParameterUpdateRequest,
            requestContext
        );

        executeMethodPublicApi(
            requestContext,
            String.format(API_STRING_PARAMETERS_MANAGE, pid, id),
            stringParameterUpdateRequest,
            HttpMethod.PUT,
            (responseEntity) -> {
                handleResponse(
                    responseEntity,
                    id,
                    pid,
                    "Update"
                );
                return null;
            }
        );
    }

    public void deleteBinaryParameter(String id, String pid, RequestContext requestContext) {
        log.debug(
            "#deleteBinaryParameter(String id, String pid, RequestContext requestContext: {}, {}, {}",
            id,
            pid,
            requestContext
        );

        executeMethodPublicApi(
            requestContext,
            String.format(API_BINARY_PARAMETERS_MANAGE, pid, id),
            null,
            HttpMethod.DELETE,
            (responseEntity) -> {
                handleResponse(
                    responseEntity,
                    id,
                    pid,
                    "Delete"
                );
                return null;
            }
        );
    }

    public void deleteStringParameter(String id, String pid, RequestContext requestContext) {
        log.debug(
            "#deleteStringParameter(String id, String pid, RequestContext requestContext: {}, {}, {}",
            id,
            pid,
            requestContext
        );

        executeMethodPublicApi(
            requestContext,
            String.format(API_STRING_PARAMETERS_MANAGE, pid, id),
            null,
            HttpMethod.DELETE,
            (responseEntity) -> {
                handleResponse(
                    responseEntity,
                    id,
                    pid,
                    "Delete"
                );
                return null;
            }
        );
    }

    private void handleResponse(ResponseEntity<String> responseEntity, String id, String pid, String operation) {
        switch (responseEntity.getStatusCode().value()) {
            case 200:
            case 202:
            case 204:
                log.debug("{} operation on parameter {} with pid: {} was successful", operation, id, pid);
                break;
            default:
                throw new ClientIntegrationException(String.format(
                    "%s operation failed for parameter %s with PID %s: Code: %d, Message: %s",
                    operation, id, pid, responseEntity.getStatusCode().value(), responseEntity.getBody()));
        }
    }

    private PartnerDirectoryParameter createPartnerDirectoryParameter(
        ResponseEntity<String> responseEntity,
        String id,
        String pid,
        TypeOfParam typeOfParam
    ) {
        if (responseEntity == null) {
            throw new ClientIntegrationException(String.format(
                "Creation operation failed for parameter %s with PID %s: ResponseEntity is null", id, pid));
        }

        int statusCode = responseEntity.getStatusCode().value();
        String responseBody = getResponseBody(responseEntity);

        switch (statusCode) {
            case 200:
            case 201:
                log.debug("Creation operation on parameter {} with pid: {} was successful. Response: {}", id, pid, responseBody);
                if (BINARY_PARAMETER.equals(typeOfParam)) {
                    return PartnerDirectoryParser.createBinaryParameter(new JSONObject(responseBody).getJSONObject("d"));
                } else {
                    return PartnerDirectoryParser.createStringParameter(new JSONObject(responseBody).getJSONObject("d"));
                }
            default:
                log.error("Creation failed for parameter {} with PID {}: Code: {}, Response: {}", id, pid, statusCode, responseBody);
                throw new ClientIntegrationException(String.format(
                    "Creation operation failed for parameter %s with PID %s: Code: %d, Response: %s", id, pid, statusCode, responseBody));
        }
    }

    private String getResponseBody(ResponseEntity<String> responseEntity) {
        try {
            return responseEntity.getBody();
        } catch (Exception e) {
            log.error("Failed to extract response body", e);
            return "Unable to retrieve response body";
        }
    }

    private Optional<JSONObject> retrieveApiParameter(String id, String pid, String url, RequestContext requestContext) {
        try {
            JSONObject apiParameter = callRestWs(
                requestContext,
                String.format(url, pid, id),
                response -> new JSONObject(response).getJSONObject("d")
            );
            return Optional.ofNullable(apiParameter);
        } catch (Exception e) {
            String errorMsg = String.format("Couldn't fetch parameter with id %s and pid %s: %s", id, pid, e.getMessage());
            log.error(errorMsg, e);
            return Optional.empty();
        }
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}
