package com.figaf.integration.cpi.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.partner_directory.*;
import com.figaf.integration.cpi.entity.partner_directory.enums.TypeOfParam;
import com.figaf.integration.cpi.response_parser.PartnerDirectoryParser;
import com.figaf.integration.cpi.utils.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.figaf.integration.cpi.entity.partner_directory.enums.TypeOfParam.BINARY_PARAMETER;
import static com.figaf.integration.cpi.entity.partner_directory.enums.TypeOfParam.STRING_PARAMETER;
import static com.figaf.integration.cpi.utils.HttpUtils.assertSuccessfulResponse;

@Slf4j
public class PartnerDirectoryClient extends CpiBaseClient {


    public PartnerDirectoryClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    public List<Partner> retrievePartners(RequestContext requestContext) {
        log.debug("#retrievePartners(RequestContext requestContext, {}", requestContext);
        JSONArray apiParameters;
        try {
            apiParameters = callRestWs(
                requestContext,
                API_PARTNERS,
                response -> new JSONObject(response).getJSONObject("d").getJSONArray("results")
            );
        } catch (Exception e) {
            String errorMsg = String.format("Couldn't fetch partners: %s", e.getMessage());
            throw new ClientIntegrationException(errorMsg, e);
        }
        return PartnerDirectoryParser.buildPartners(apiParameters);
    }

    public List<PartnerDirectoryParameter> retrieveBinaryParametersMetadata(
        RequestContext requestContext,
        PartnerDirectoryParameterFilterRequest partnerDirectoryParameterFilterRequest
    ) {
        log.debug("#retrieveBinaryParametersMetadata(RequestContext requestContext, partnerDirectoryParameterFilterRequest): {}, {}", requestContext, partnerDirectoryParameterFilterRequest);
        try {
            return executeRequestWithPagination(
                requestContext,
                String.format(API_BINARY_PARAMETERS_META_DATA, partnerDirectoryParameterFilterRequest.createFilter()),
                PartnerDirectoryParser::buildBinaryParameters);
        } catch (Exception e) {
            String errorMsg = String.format("Couldn't fetch binary parameters metadata: %s", e.getMessage());
            throw new ClientIntegrationException(errorMsg, e);
        }
    }

    public Optional<PartnerDirectoryParameter> retrieveBinaryParameter(String id, String pid, RequestContext requestContext) {
        log.debug("#retrieveBinaryParameter(String id, String pid, RequestContext requestContext): {}, {}, {}", id, pid, requestContext);
        Optional<JSONObject> apiParameter = retrievePartnerDirectoryParameter(id, pid, API_BINARY_PARAMETER, requestContext);
        return apiParameter.map(PartnerDirectoryParser::createBinaryParameter);
    }

    public List<PartnerDirectoryParameter> retrieveStringParameters(RequestContext requestContext, PartnerDirectoryParameterFilterRequest partnerDirectoryParameterFilterRequest) {
        log.debug("#retrieveStringParameters(RequestContext requestContext): {}", requestContext);
        try {
            return executeRequestWithPagination(
                requestContext,
                String.format(API_STRING_PARAMETERS, partnerDirectoryParameterFilterRequest.createFilter()),
                PartnerDirectoryParser::buildStringParameters);
        } catch (Exception e) {
            String errorMsg = String.format("Couldn't fetch string parameters: %s", e.getMessage());
            throw new ClientIntegrationException(errorMsg, e);
        }
    }

    public List<AlternativePartner> retrieveAlternativePartners(RequestContext requestContext, AlternativePartnerFilterRequest alternativePartnerFilterRequest) {
        log.debug("#retrieveAlternativePartners(RequestContext requestContext): {}", requestContext);
        try {
            return executeRequestWithPagination(
                requestContext,
                String.format(API_ALTERNATIVE_PARTNERS, alternativePartnerFilterRequest.createAlternativePartnerKeyFilter()),
                PartnerDirectoryParser::buildAlternativePartners);
        } catch (Exception e) {
            String errorMsg = String.format("Couldn't fetch alternative partners: %s", e.getMessage());
            throw new ClientIntegrationException(errorMsg, e);
        }
    }

    public Optional<PartnerDirectoryParameter> retrieveStringParameter(String id, String pid, RequestContext requestContext) {
        log.debug("#retrieveStringParameter(String id, String pid, RequestContext requestContext): {}, {}, {}", id, pid, requestContext);
        Optional<JSONObject> apiParameter = retrievePartnerDirectoryParameter(id, pid, API_STRING_PARAMETER, requestContext);
        return apiParameter.map(PartnerDirectoryParser::createStringParameter);
    }

    public Optional<AlternativePartner> retrieveAlternativePartner(String agency, String scheme, String id, RequestContext requestContext) {
        log.debug("#retrieveAlternativePartner(String agency, String scheme, String id, RequestContext requestContext): {}, {}, {}, {}", agency, scheme, id, requestContext);
        Optional<JSONObject> apiParameter = retrieveAlternativePartnerInner(agency, scheme, id, requestContext);
        return apiParameter.map(PartnerDirectoryParser::createAlternativePartner);
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

    public AlternativePartner createAlternativePartner(AlternativePartnerCreationRequest alternativePartnerCreationRequest, RequestContext requestContext) {
        log.debug(
            "#createAlternativePartner(AlternativePartnerCreationRequest alternativePartnerCreationRequest, RequestContext requestContext): {}, {}",
            alternativePartnerCreationRequest,
            requestContext
        );
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Accept", "application/json");

        return executeMethodPublicApiAppendingCustomHeaders(
            requestContext,
            API_ALTERNATIVE_PARTNER_CREATION,
            alternativePartnerCreationRequest,
            HttpMethod.POST,
            httpHeaders,
            (responseEntity) -> createAlternativePartner(
                responseEntity,
                alternativePartnerCreationRequest.getId(),
                alternativePartnerCreationRequest.getPid()
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
                assertSuccessfulResponse(
                    responseEntity,
                    "Update",
                    String.format("parameter %s with pid: %s", id, pid)
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
                assertSuccessfulResponse(
                    responseEntity,
                    "Update",
                    String.format("parameter %s with pid: %s", id, pid)
                );
                return null;
            }
        );
    }

    public void updateAlternativePartner(
        String agency,
        String scheme,
        String id,
        AlternativePartnerUpdateRequest alternativePartnerUpdateRequest,
        RequestContext requestContext
    ) {
        log.debug(
            "#updateAlternativePartner(String agency, String scheme, String id, StringParameterUpdateRequest stringParameterUpdateRequest, RequestContext requestContext: {}, {}, {}, {}, {}",
            agency,
            scheme,
            id,
            alternativePartnerUpdateRequest,
            requestContext
        );

        executeMethodPublicApi(
            requestContext,
            String.format(
                API_ALTERNATIVE_PARTNER_MANAGE,
                new String(Hex.encodeHex(agency.getBytes(StandardCharsets.UTF_8), false)),
                new String(Hex.encodeHex(scheme.getBytes(StandardCharsets.UTF_8), false)),
                new String(Hex.encodeHex(id.getBytes(StandardCharsets.UTF_8), false))
            ),
            alternativePartnerUpdateRequest,
            HttpMethod.PUT,
            (responseEntity) -> {
                assertSuccessfulResponse(
                    responseEntity,
                    "Update",
                    String.format("alternative partner %s with agency: %s and scheme: %s",
                        id,
                        agency,
                        scheme
                    )
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
                assertSuccessfulResponse(
                    responseEntity,
                    "Delete",
                    String.format("parameter %s with pid: %s", id, pid)
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
                assertSuccessfulResponse(
                    responseEntity,
                    "Delete",
                    String.format("parameter %s with pid: %s", id, pid)
                );
                return null;
            }
        );
    }

    public void deleteAlternativePartner(String agency, String scheme, String id, RequestContext requestContext) {
        log.debug(
            "#deleteAlternativePartner(String agency, String scheme, String id, RequestContext requestContext: {}, {}, {}, {}",
            agency,
            scheme,
            id,
            requestContext
        );

        executeMethodPublicApi(
            requestContext,
            String.format(
                API_ALTERNATIVE_PARTNER_MANAGE,
                new String(Hex.encodeHex(agency.getBytes(StandardCharsets.UTF_8), false)),
                new String(Hex.encodeHex(scheme.getBytes(StandardCharsets.UTF_8), false)),
                new String(Hex.encodeHex(id.getBytes(StandardCharsets.UTF_8), false))
            ),
            null,
            HttpMethod.DELETE,
            (responseEntity) -> {
                assertSuccessfulResponse(
                    responseEntity,
                    "Delete",
                    String.format("alternative partner %s with agency: %s and scheme: %s",
                        id,
                        agency,
                        scheme
                    ));
                return null;
            }
        );
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

        return switch (statusCode) {
            case 200, 201 -> {
                log.debug("Creation operation on parameter {} with pid: {} was successful. Response: {}", id, pid, responseBody);
                if (BINARY_PARAMETER.equals(typeOfParam)) {
                    yield PartnerDirectoryParser.createBinaryParameter(new JSONObject(responseBody).getJSONObject("d"));
                } else {
                    yield PartnerDirectoryParser.createStringParameter(new JSONObject(responseBody).getJSONObject("d"));
                }
            }
            default -> {
                log.error("Creation failed for parameter {} with PID {}: Code: {}, Response: {}", id, pid, statusCode, responseBody);
                throw new ClientIntegrationException(String.format(
                    "Creation operation failed for parameter %s with PID %s: Code: %d, Response: %s", id, pid, statusCode, responseBody));
            }
        };
    }

    private AlternativePartner createAlternativePartner(
        ResponseEntity<String> responseEntity,
        String id,
        String pid
    ) {
        if (responseEntity == null) {
            throw new ClientIntegrationException(String.format(
                "Creation operation failed for alternative partner %s with PID %s: ResponseEntity is null",
                id,
                pid
            ));
        }

        int statusCode = responseEntity.getStatusCode().value();
        String responseBody = getResponseBody(responseEntity);

        return switch (statusCode) {
            case 200, 201 -> {
                log.debug("Creation operation on alternative partner {} with pid: {} was successful. Response: {}", id, pid, responseBody);
                yield PartnerDirectoryParser.createAlternativePartner(new JSONObject(responseBody).getJSONObject("d"));
            }
            default -> {
                log.error("Creation failed for alternative partner {} with pid {}: Code: {}, Response: {}", id, pid, statusCode, responseBody);
                throw new ClientIntegrationException(String.format(
                    "Creation operation failed for alternative partner %s with PID %s: Code: %d, Response: %s", id, pid, statusCode, responseBody));
            }
        };
    }

    private String getResponseBody(ResponseEntity<String> responseEntity) {
        return StringUtils.isNotBlank(responseEntity.getBody()) ? responseEntity.getBody() : StringUtils.EMPTY;
    }

    private Optional<JSONObject> retrievePartnerDirectoryParameter(String id, String pid, String url, RequestContext requestContext) {
        return HttpUtils.executeHttpCallWithRetry(
            () -> this.callRestWs(
                requestContext,
                String.format(url, pid, id),
                JSONObject::new
            ).getJSONObject("d")
        );
    }

    private Optional<JSONObject> retrieveAlternativePartnerInner(String agency, String scheme, String id, RequestContext requestContext) {
        return HttpUtils.executeHttpCallWithRetry(
            () -> this.callRestWs(
                requestContext,
                String.format(
                    API_ALTERNATIVE_PARTNER,
                    new String(Hex.encodeHex(agency.getBytes(StandardCharsets.UTF_8), false)),
                    new String(Hex.encodeHex(scheme.getBytes(StandardCharsets.UTF_8), false)),
                    new String(Hex.encodeHex(id.getBytes(StandardCharsets.UTF_8), false))
                ),
                JSONObject::new
            ).getJSONObject("d")
        );
    }

    private <T> List<T> executeRequestWithPagination(
        RequestContext requestContext,
        String initialPath,
        Function<JSONArray, List<T>> jsonParser
    ) {
        List<T> aggregated = new ArrayList<>();
        String path = initialPath;

        do {
            JSONObject page = callRestWs(
                requestContext,
                path,
                response -> new JSONObject(response).getJSONObject("d")
            );

            aggregated.addAll(jsonParser.apply(page.getJSONArray("results")));

            path = optString(page, "__next");
            if (path != null) {
                path = "/api/v1/" + path;
            }
        } while (path != null);

        return aggregated;
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}
