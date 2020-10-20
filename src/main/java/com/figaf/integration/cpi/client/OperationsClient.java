package com.figaf.integration.cpi.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.figaf.integration.common.entity.CommonClientWrapperEntity;
import com.figaf.integration.common.entity.ConnectionProperties;
import com.figaf.integration.common.entity.RestTemplateWrapper;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.monitoring.*;
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
 * @author Arsenii Istlentev
 */
@Slf4j
public class OperationsClient extends CpiBaseClient {

    private final ObjectMapper xmlObjectMapper;

    public OperationsClient(String ssoUrl, HttpClientsFactory httpClientsFactory) {
        super(ssoUrl, httpClientsFactory);
        this.xmlObjectMapper = new XmlMapper();
        this.xmlObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.xmlObjectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        this.xmlObjectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public StatisticOverviewCommandResponse callStatisticOverviewCommand(CommonClientWrapperEntity commonClientWrapperEntity) {

        RestTemplateWrapper restTemplateWrapper = getRestTemplateWrapper(commonClientWrapperEntity);

        String token = retrieveToken(commonClientWrapperEntity, restTemplateWrapper.getRestTemplate(), "/itspaces/Operations");

        String url = buildUrl(commonClientWrapperEntity, "/itspaces/Operations/com.sap.it.op.tmn.commands.dashboard.webui.StatisticOverviewCommand");

        return callStatisticOverviewCommand(restTemplateWrapper.getRestTemplate(), url, token);
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
            String requestBody = xmlObjectMapper.writeValueAsString(participantListCommandRequest);

            HttpPost createPackageRequest = new HttpPost(uri);
            createPackageRequest.setHeader(createBasicAuthHeader(connectionProperties));
            createPackageRequest.setHeader(createCsrfTokenHeader(csrfToken));
            createPackageRequest.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_XML));

            participantListHttpResponse = client.execute(createPackageRequest);

            if (participantListHttpResponse.getStatusLine().getStatusCode() == 200) {
                String responseBody = IOUtils.toString(participantListHttpResponse.getEntity().getContent(), StandardCharsets.UTF_8);
                ParticipantListCommandResponse participantListCommandResponse = xmlObjectMapper.readValue(responseBody, ParticipantListCommandResponse.class);
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

            String requestBody = xmlObjectMapper.writeValueAsString(nodeProcessStatisticCommandRequest);

            HttpPost nodeProcessStatisticCommandHttpRequest = new HttpPost(uri);
            nodeProcessStatisticCommandHttpRequest.setHeader(createBasicAuthHeader(connectionProperties));
            nodeProcessStatisticCommandHttpRequest.setHeader(createCsrfTokenHeader(csrfToken));
            nodeProcessStatisticCommandHttpRequest.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_XML));

            nodeProcessStatisticCommandHttpResponse = client.execute(nodeProcessStatisticCommandHttpRequest);

            if (nodeProcessStatisticCommandHttpResponse.getStatusLine().getStatusCode() == 200) {
                String responseBody = IOUtils.toString(nodeProcessStatisticCommandHttpResponse.getEntity().getContent(), StandardCharsets.UTF_8);
                NodeProcessStatisticCommandResponse nodeProcessStatisticCommandResponse = xmlObjectMapper.readValue(responseBody, NodeProcessStatisticCommandResponse.class);
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

    public StatisticOverviewCommandResponse callStatisticOverviewCommand(RestTemplate restTemplate, String url, String csrfToken) {
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("X-CSRF-Token", csrfToken);
            httpHeaders.setContentType(MediaType.APPLICATION_XML);

            HttpEntity<Void> requestEntity = new HttpEntity<>(httpHeaders);

            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);


            if (HttpStatus.OK.equals(responseEntity.getStatusCode())) {
                StatisticOverviewCommandResponse statisticOverviewCommandResponse = xmlObjectMapper.readValue(responseEntity.getBody(), StatisticOverviewCommandResponse.class);
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
