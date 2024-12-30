package com.figaf.integration.cpi.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.message_sender.MessageSenderType;

public class MessageSenderFactory {

    private final HttpClientsFactory httpClientsFactory;

    public MessageSenderFactory(HttpClientsFactory defaultHttpClientsFactory, RequestContext requestContext) {
        this.httpClientsFactory = resolveHttpClientsFactory(defaultHttpClientsFactory, requestContext);
    }

    public MessageSender createMessageSender(MessageSenderType messageSenderType) {
        return switch (messageSenderType) {
            case HTTP -> new HttpMessageSender(this.httpClientsFactory);
            case IDOC -> new IDocMessageSender(this.httpClientsFactory);
            case SOAP -> new SoapMessageSender(this.httpClientsFactory);
        };
    }

    private HttpClientsFactory resolveHttpClientsFactory(HttpClientsFactory defaultHttpClientsFactory, RequestContext requestContext) {
        if (!requestContext.isOnPremiseEdgeSystem()) {
            return defaultHttpClientsFactory;
        }
        return HttpClientsFactory.getForOnPremiseIntegration(
            defaultHttpClientsFactory.isUseProxyForConnections(),
            defaultHttpClientsFactory.getConnectionRequestTimeout(),
            defaultHttpClientsFactory.getConnectTimeout(),
            defaultHttpClientsFactory.getSocketTimeout(),
            requestContext.getEdgeCloudConnectorLocationId(),
            defaultHttpClientsFactory.getSapAirKey()
        );
    }
}
