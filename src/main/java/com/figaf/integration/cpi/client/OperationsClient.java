package com.figaf.integration.cpi.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.entity.ConnectionProperties;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.monitoring.*;
import com.figaf.integration.cpi.client.mapper.ObjectMapperFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * @author Kostas Charalambous
 */
@Slf4j
public class OperationsClient extends CpiBaseClient {

    private static final String RUNTIME_LOCATION_URI = "/Operations/com.sap.it.op.srv.web.cf.RuntimeLocationListCommand";

    public OperationsClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    public RuntimeLocationsResponse getRuntimeLocations(RequestContext requestContext) {
        log.debug("#getRuntimeLocations(RequestContext requestContext): {}", requestContext);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Accept", "application/json");

        return executeGet(
            requestContext,
            httpHeaders,
            RUNTIME_LOCATION_URI,
            runtimeLocationsResponseRaw -> ObjectMapperFactory.getJsonObjectMapper().readValue(runtimeLocationsResponseRaw, RuntimeLocationsResponse.class),
            String.class
        );
    }

    public StatisticOverviewCommandResponse callStatisticOverviewCommand(RequestContext requestContext) {
        return executeMethod(
            requestContext,
            "/itspaces/Operations",
            "/itspaces/Operations/com.sap.it.op.tmn.commands.dashboard.webui.StatisticOverviewCommand",
            (url, token, restTemplateWrapper) -> callStatisticOverviewCommand(url, token, restTemplateWrapper.getRestTemplate())
        );
    }

    public String getCsrfToken(ConnectionProperties connectionProperties, HttpClient httpClient) {
        return getCsrfToken(connectionProperties, "/Operations/", httpClient);
    }

    public ParticipantListCommandResponse callParticipantListCommand(ConnectionProperties connectionProperties, HttpClient client, String csrfToken) {
        URI uri = UriComponentsBuilder.newInstance()
            .scheme(connectionProperties.getProtocol())
            .host(connectionProperties.getHost())
            .path("/Operations/com.sap.it.op.srv.commands.dashboard.ParticipantListCommand")
            .build()
            .encode()
            .toUri();

        HttpResponse participantListHttpResponse = null;
        try {

            ParticipantListCommandRequest participantListCommandRequest = new ParticipantListCommandRequest();
            String requestBody = ObjectMapperFactory.getXmlObjectMapper().writeValueAsString(participantListCommandRequest);

            HttpPost createPackageRequest = new HttpPost(uri);
            createPackageRequest.setHeader(createBasicAuthHeader(connectionProperties));
            createPackageRequest.setHeader(createCsrfTokenHeader(csrfToken));
            createPackageRequest.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_XML));

            participantListHttpResponse = client.execute(createPackageRequest);

            if (participantListHttpResponse.getStatusLine().getStatusCode() == 200) {
                String responseBody = IOUtils.toString(participantListHttpResponse.getEntity().getContent(), StandardCharsets.UTF_8);
                ParticipantListCommandResponse participantListCommandResponse = ObjectMapperFactory.getXmlObjectMapper().readValue(responseBody, ParticipantListCommandResponse.class);
                return participantListCommandResponse;
            } else {
                throw new ClientIntegrationException(String.format(
                    "Couldn't get participantListCommandResponse. Code: %d, Message: %s",
                    participantListHttpResponse.getStatusLine().getStatusCode(),
                    IOUtils.toString(participantListHttpResponse.getEntity().getContent(), StandardCharsets.UTF_8))
                );
            }

        } catch (Exception ex) {
            log.error("Error occurred while getting participantListCommandResponse: " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while getting participantListCommandResponse: " + ex.getMessage(), ex);
        } finally {
            HttpClientUtils.closeQuietly(participantListHttpResponse);
        }
    }

    public NodeProcessStatisticCommandResponse callNodeProcessStatisticCommand(ConnectionProperties connectionProperties, HttpClient client, String csrfToken, NodeProcessStatisticCommandRequest nodeProcessStatisticCommandRequest) {
        URI uri = UriComponentsBuilder.newInstance()
            .scheme(connectionProperties.getProtocol())
            .host(connectionProperties.getHost())
            .path("/Operations/com.sap.it.op.srv.commands.dashboard.NodeProcessStatisticCommand")
            .build()
            .encode()
            .toUri();

        HttpResponse nodeProcessStatisticCommandHttpResponse = null;
        try {

            String requestBody = ObjectMapperFactory.getXmlObjectMapper().writeValueAsString(nodeProcessStatisticCommandRequest);

            HttpPost nodeProcessStatisticCommandHttpRequest = new HttpPost(uri);
            nodeProcessStatisticCommandHttpRequest.setHeader(createBasicAuthHeader(connectionProperties));
            nodeProcessStatisticCommandHttpRequest.setHeader(createCsrfTokenHeader(csrfToken));
            nodeProcessStatisticCommandHttpRequest.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_XML));

            nodeProcessStatisticCommandHttpResponse = client.execute(nodeProcessStatisticCommandHttpRequest);

            if (nodeProcessStatisticCommandHttpResponse.getStatusLine().getStatusCode() == 200) {
                String responseBody = IOUtils.toString(nodeProcessStatisticCommandHttpResponse.getEntity().getContent(), StandardCharsets.UTF_8);
                NodeProcessStatisticCommandResponse nodeProcessStatisticCommandResponse = ObjectMapperFactory.getXmlObjectMapper().readValue(responseBody, NodeProcessStatisticCommandResponse.class);
                return nodeProcessStatisticCommandResponse;
            } else {
                throw new ClientIntegrationException(String.format(
                    "Couldn't get nodeProcessStatisticCommandResponse. Code: %d, Message: %s",
                    nodeProcessStatisticCommandHttpResponse.getStatusLine().getStatusCode(),
                    IOUtils.toString(nodeProcessStatisticCommandHttpResponse.getEntity().getContent(), StandardCharsets.UTF_8))
                );
            }

        } catch (Exception ex) {
            log.error("Error occurred while getting nodeProcessStatisticCommandHttpResponse: " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while getting nodeProcessStatisticCommandHttpResponse: " + ex.getMessage(), ex);
        } finally {
            HttpClientUtils.closeQuietly(nodeProcessStatisticCommandHttpResponse);
        }
    }

    private StatisticOverviewCommandResponse callStatisticOverviewCommand(String url, String csrfToken, RestTemplate restTemplate) {
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("X-CSRF-Token", csrfToken);
            httpHeaders.setContentType(MediaType.APPLICATION_XML);

            HttpEntity<Void> requestEntity = new HttpEntity<>(httpHeaders);

            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);


            if (HttpStatus.OK.equals(responseEntity.getStatusCode())) {
                StatisticOverviewCommandResponse statisticOverviewCommandResponse = ObjectMapperFactory.getXmlObjectMapper().readValue(responseEntity.getBody(), StatisticOverviewCommandResponse.class);
                return statisticOverviewCommandResponse;
            } else {
                throw new ClientIntegrationException(String.format(
                    "Couldn't get statisticOverviewCommandResponse. Code: %d, Message: %s",
                    responseEntity.getStatusCode().value(),
                    requestEntity.getBody())
                );
            }

        } catch (Exception ex) {
            log.error("Error occurred while getting statisticOverviewCommandResponse: " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while getting statisticOverviewCommandResponse: " + ex.getMessage(), ex);
        }
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

}
