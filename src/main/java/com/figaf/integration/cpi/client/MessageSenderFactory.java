package com.figaf.integration.cpi.client;

import com.figaf.integration.common.client.HttpMessageSender;
import com.figaf.integration.common.client.MessageSender;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;

public class MessageSenderFactory {

    private final HttpClientsFactory httpClientsFactory;

    public MessageSenderFactory(HttpClientsFactory defaultHttpClientsFactory, RequestContext requestContext) {
        this.httpClientsFactory = resolveHttpClientsFactory(defaultHttpClientsFactory, requestContext);
    }

    public MessageSender createMessageSender() {
        return new HttpMessageSender(this.httpClientsFactory);
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
