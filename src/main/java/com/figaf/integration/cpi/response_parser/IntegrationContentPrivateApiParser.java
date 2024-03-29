package com.figaf.integration.cpi.response_parser;

import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.cpi.entity.runtime_artifacts.IntegrationContent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.sql.Timestamp;
import java.util.*;

import static com.figaf.integration.cpi.utils.CpiApiUtils.loadXMLFromString;

/**
 * @author Arsenii Istlentev
 */
@Slf4j
public class IntegrationContentPrivateApiParser {

    public static List<IntegrationContent> getAllIntegrationRuntimeArtifacts(String body) {
        return getAllIntegrationRuntimeArtifacts(body, null);
    }

    public static IntegrationContent getIntegrationRuntimeArtifactByName(String body, String name) {
        List<IntegrationContent> integrationContentList = getAllIntegrationRuntimeArtifacts(body, name);
        if (CollectionUtils.isNotEmpty(integrationContentList)) {
            return integrationContentList.get(0);
        } else {
            throw new ClientIntegrationException(String.format("Can't find integration runtime artifact with name %s", name));
        }
    }

    public static List<String> getIntegrationRuntimeErrorInformation(String body) {
        List<String> errorMessages = new ArrayList<>();
        Document document = loadXMLFromString(body);
        NodeList componentInformationsList = document.getElementsByTagName("componentInformations");
        for (int i = 0; i < componentInformationsList.getLength(); i++) {
            Node item = componentInformationsList.item(i);
            Map<String, Node> elements = new HashMap<>();
            for (int j = 0; j < item.getChildNodes().getLength(); j++) {
                Node childNode = item.getChildNodes().item(j);
                elements.put(childNode.getNodeName(), childNode);
            }
            Node stateNode = elements.get("state");
            if (stateNode != null && "ERROR".equals(stateNode.getTextContent())) {
                Node messageNode = elements.get("message");
                handleMessageNode(messageNode, errorMessages);
            }
        }
        return errorMessages;

    }

    private static void handleMessageNode(Node messageNode, List<String> errorMessages) {
        for (int j = 0; j < messageNode.getChildNodes().getLength(); j++) {
            Node childNode = messageNode.getChildNodes().item(j);
            if ("message".equals(childNode.getNodeName())) {
                handleMessageNode(childNode, errorMessages);
            }
            Node messageTextNode = childNode.getAttributes().getNamedItem("messageText");
            if (messageTextNode != null) {
                errorMessages.add(messageTextNode.getTextContent());
            }
            if ("param".equals(childNode.getNodeName())) {
                String paramValue = childNode.getTextContent();
                errorMessages.add(paramValue);
            }
        }
    }

    private static List<IntegrationContent> getAllIntegrationRuntimeArtifacts(String body, String name) {
        FastDateFormat dateFormat = FastDateFormat.getInstance("yyyy-MM-dd'T'hh:mm:ss.SSS");
        List<IntegrationContent> integrationContentList = new ArrayList<>();
        Document document = loadXMLFromString(body);
        NodeList artifactInformationsList = document.getElementsByTagName("artifactInformations");
        for (int i = 0; i < artifactInformationsList.getLength(); i++) {
            try {
                Node item = artifactInformationsList.item(i);
                IntegrationContent integrationContent = new IntegrationContent();
                Map<String, String> elements = new HashMap<>();
                for (int j = 0; j < item.getChildNodes().getLength(); j++) {
                    Node childNode = item.getChildNodes().item(j);
                    elements.put(childNode.getNodeName(), childNode.getTextContent());
                }
                integrationContent.setId(elements.get("symbolicName"));
                integrationContent.setExternalId(elements.get("id"));
                integrationContent.setVersion(elements.get("version"));
                integrationContent.setName(elements.get("name"));
                integrationContent.setType(elements.get("type"));
                integrationContent.setDeployedBy(elements.get("deployedBy"));
                Date parsedDate = dateFormat.parse(elements.get("deployedOn"));
                integrationContent.setDeployedOn(new Timestamp(parsedDate.getTime()));
                integrationContent.setStatus(elements.get("semanticState"));
                if (name != null) {
                    if (name.equals(integrationContent.getId())) {
                        return Collections.singletonList(integrationContent);
                    }
                } else {
                    integrationContentList.add(integrationContent);
                }
            } catch (Exception ex) {
                log.error("Can't handle artifact information: ", ex);
            }
        }
        return integrationContentList;
    }
}
