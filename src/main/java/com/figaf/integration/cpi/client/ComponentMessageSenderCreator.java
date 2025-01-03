package com.figaf.integration.cpi.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.cpi.entity.message_sender.ComponentMessageSenderType;

public class ComponentMessageSenderCreator {

    private final HttpClientsFactory httpClientsFactoryForOnPremise;

    private final HttpClientsFactory httpClientsFactory;

    private final boolean onPremiseEdgeSystem;

    public ComponentMessageSenderCreator(HttpClientsFactory httpClientsFactory, RequestContext requestContext) {
        this.onPremiseEdgeSystem = requestContext.isOnPremiseEdgeSystem();
        this.httpClientsFactoryForOnPremise = HttpClientsFactory.getForOnPremiseIntegration(
            httpClientsFactory.isUseProxyForConnections(),
            httpClientsFactory.getConnectionRequestTimeout(),
            httpClientsFactory.getConnectTimeout(),
            httpClientsFactory.getSocketTimeout(),
            requestContext.getEdgeCloudConnectorLocationId(),
            httpClientsFactory.getSapAirKey()
        );
        this.httpClientsFactory = httpClientsFactory;
    }

    public MessageSender createComponentMessageSender(ComponentMessageSenderType componentMessageSenderType) {
        HttpClientsFactory finalizedhttpClientsFactory = onPremiseEdgeSystem
            ? this.httpClientsFactoryForOnPremise
            : this.httpClientsFactory;

        return switch (componentMessageSenderType) {
            case HTTP -> new HttpComponentMessageSender(finalizedhttpClientsFactory);
            case IDOC -> new IDocComponentMessageSender(finalizedhttpClientsFactory);
            case SOAP -> new SoapComponentMessageSender(finalizedhttpClientsFactory);
        };
    }
}
