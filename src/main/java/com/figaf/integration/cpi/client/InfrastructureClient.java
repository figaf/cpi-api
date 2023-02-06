package com.figaf.integration.cpi.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.xml.sax.InputSource;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.net.URL;

/**
 * @author Arsenii Istlentev
 */
@Slf4j
public class InfrastructureClient extends CpiBaseClient {

    public InfrastructureClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    public String fetchIflMapHost(RequestContext requestContext) {
        log.debug("#fetchIflMapHost(RequestContext requestContext): {}", requestContext);
        return executeGetPublicApiAndReturnResponseBody(
            requestContext,
            "/api/v1/ServiceEndpoints?$expand=EntryPoints,ApiDefinitions",
            (body) -> {
                InputSource inputXML = new InputSource(new StringReader(body));
                XPath xPath = XPathFactory.newInstance().newXPath();
                String fetchedUrl = xPath.evaluate("(//*[local-name()='Url']/text())[1]", inputXML);
                URL url = new URL(fetchedUrl);
                return url.getHost();
            }
        );
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}
