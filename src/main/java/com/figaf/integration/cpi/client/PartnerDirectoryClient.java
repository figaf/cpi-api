package com.figaf.integration.cpi.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.partner_directory.PartnerDirectoryParameter;
import com.figaf.integration.cpi.entity.partner_directory.exception.PartnerDirectoryClientException;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;

import static com.figaf.integration.cpi.response_parser.PartnerDirectoryParser.*;

@Slf4j
public class PartnerDirectoryClient extends CpiBaseClient {

    public PartnerDirectoryClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    public List<PartnerDirectoryParameter> retrieveBinaryParameters(RequestContext requestContext) {
        log.debug("#retrieveBinaryParameters(RequestContext requestContext): {}", requestContext);
        JSONArray apiParameters = callRestWs(
            requestContext,
            API_BINARY_PARAMETERS,
            response -> new JSONObject(response).getJSONObject("d").getJSONArray("results")
        );

        if (!Optional.ofNullable(apiParameters).isPresent()) {
            String errorMsg = "couldn't fetch binary parameters";
            log.error(errorMsg);
            throw new PartnerDirectoryClientException(errorMsg);
        }
        return buildBinaryParameters(apiParameters);
    }

    public List<PartnerDirectoryParameter> retrieveStringParameters(RequestContext requestContext) {
        log.debug("#retrieveStringParameters(RequestContext requestContext): {}", requestContext);
        JSONArray apiParameters = callRestWs(
            requestContext,
            API_STRING_PARAMETERS,
            response -> new JSONObject(response).getJSONObject("d").getJSONArray("results")
        );

        if (!Optional.ofNullable(apiParameters).isPresent()) {
            String errorMsg = "couldn't fetch string parameters";
            log.error(errorMsg);
            throw new PartnerDirectoryClientException(errorMsg);
        }
        return buildStringParameters(apiParameters);
    }


    @Override
    protected Logger getLogger() {
        return log;
    }
}
