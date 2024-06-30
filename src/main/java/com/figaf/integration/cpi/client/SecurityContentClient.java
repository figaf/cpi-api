package com.figaf.integration.cpi.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.common.utils.Utils;
import com.figaf.integration.cpi.entity.security.*;
import com.figaf.integration.cpi.utils.CpiApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Arsenii Istlentev
 */
@Slf4j
public class SecurityContentClient extends CpiBaseClient {

    public SecurityContentClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    public List<UserCredentials> getUserCredentialsList(RequestContext requestContext) {
        return getUserCredentialsList(requestContext, false);
    }

    /**
     * @param skipAdditionalObjects This API returns not only UserCredential objects but also SecureParameters, OAuth2ClientCredentials and other objects. So this flag should be true if you
     */
    public List<UserCredentials> getUserCredentialsList(
        RequestContext requestContext,
        boolean skipAdditionalObjects
    ) {
        log.debug("#getUserCredentialsList(RequestContext requestContext, boolean skipAdditionalObjects): {}, {}", requestContext, skipAdditionalObjects);
        try {
            return executeGetPublicApiAndReturnResponseBody(
                requestContext,
                "/api/v1/UserCredentials?$format=json",
                (body) -> {
                    JSONObject responseModel = new JSONObject(body);
                    JSONArray results = responseModel.getJSONObject("d").getJSONArray("results");

                    List<UserCredentials> userCredentialsList = new ArrayList<>();
                    for (int ind = 0; ind < results.length(); ind++) {
                        JSONObject userCredentialsJsonObject = results.getJSONObject(ind);
                        UserCredentials userCredentials = getUserCredentialsFromJson(userCredentialsJsonObject);
                        if (skipAdditionalObjects && !"default".equals(userCredentials.getKind())) {
                            continue;
                        }
                        userCredentialsList.add(userCredentials);
                    }
                    return userCredentialsList;
                }
            );
        } catch (Exception ex) {
            log.error("Error occurred while fetching user credentials list " + ex.getMessage(), ex);
            throw new RuntimeException("Error occurred while fetching user credentials list: " + ex.getMessage(), ex);
        }
    }

    public List<OAuth2ClientCredentials> getOAuth2ClientCredentialsList(RequestContext requestContext) {
        log.debug("#getOAuth2ClientCredentialsList(RequestContext requestContext): {}", requestContext);
        try {
            return executeGetPublicApiAndReturnResponseBody(
                requestContext,
                "/api/v1/OAuth2ClientCredentials?$format=json",
                (body) -> {
                    JSONObject responseModel = new JSONObject(body);
                    JSONArray results = responseModel.getJSONObject("d").getJSONArray("results");

                    List<OAuth2ClientCredentials> oAuth2ClientCredentialsList = new ArrayList<>();
                    for (int ind = 0; ind < results.length(); ind++) {
                        JSONObject oAuth2ClientCredentialsJsonObject = results.getJSONObject(ind);
                        oAuth2ClientCredentialsList.add(getOAuth2ClientCredentialsFromJson(oAuth2ClientCredentialsJsonObject));
                    }
                    return oAuth2ClientCredentialsList;
                }
            );
        } catch (Exception ex) {
            log.error("Error occurred while fetching OAuth2 client credentials list " + ex.getMessage(), ex);
            throw new RuntimeException("Error occurred while fetching OAuth2 client credentials list: " + ex.getMessage(), ex);
        }
    }

    public List<SecureParameter> getSecureParameters(RequestContext requestContext) {
        log.debug("#getSecureParameters(RequestContext requestContext): {}", requestContext);
        try {
            return executeGetPublicApiAndReturnResponseBody(
                requestContext,
                "/api/v1/SecureParameters?$format=json",
                (body) -> {
                    JSONObject responseModel = new JSONObject(body);
                    JSONArray results = responseModel.getJSONObject("d").getJSONArray("results");

                    List<SecureParameter> secureParameters = new ArrayList<>();
                    for (int ind = 0; ind < results.length(); ind++) {
                        JSONObject secureParameterJsonObject = results.getJSONObject(ind);
                        secureParameters.add(getSecureParameterFromJson(secureParameterJsonObject));
                    }
                    return secureParameters;
                }
            );
        } catch (Exception ex) {
            log.error("Error occurred while fetching secure parameters " + ex.getMessage(), ex);
            throw new RuntimeException("Error occurred while fetching secure parameters: " + ex.getMessage(), ex);
        }
    }

    public UserCredentials getUserCredentials(RequestContext requestContext, String name) {
        log.debug("#getUserCredentials(RequestContext requestContext, String name): {}, {}", requestContext, name);
        try {
            return executeGetPublicApiAndReturnResponseBody(
                requestContext,
                String.format("/api/v1/UserCredentials('%s')$format=json", name),
                (body) -> {
                    JSONObject responseModel = new JSONObject(body);
                    JSONObject userCredentialsJsonObject = responseModel.getJSONObject("d");
                    return getUserCredentialsFromJson(userCredentialsJsonObject);
                }
            );
        } catch (Exception ex) {
            log.error("Error occurred while fetching user credentials " + ex.getMessage(), ex);
            throw new RuntimeException("Error occurred while fetching user credentials: " + ex.getMessage(), ex);
        }
    }

    public List<KeystoreEntry> getKeystoreEntryList(RequestContext requestContext) {
        log.debug("#getKeystoreEntryList(RequestContext requestContext): {}", requestContext);

        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("Accept", "application/json");
            return executeGetPublicApiAndReturnResponseBody(
                requestContext,
                "/api/v1/KeystoreEntries",
                httpHeaders,
                (body) -> {
                    JSONObject responseModel = new JSONObject(body);
                    JSONArray results = responseModel.getJSONObject("d").getJSONArray("results");

                    List<KeystoreEntry> keystoreEntries = new ArrayList<>();
                    for (int ind = 0; ind < results.length(); ind++) {
                        JSONObject keystoreEntryJsonObject = results.getJSONObject(ind);
                        keystoreEntries.add(getKeystoreEntryFromJson(keystoreEntryJsonObject));
                    }
                    return keystoreEntries;
                }
            );
        } catch (Exception ex) {
            log.error("Error occurred while fetching keystore entries " + ex.getMessage(), ex);
            throw new RuntimeException("Error occurred while fetching keystore entries: " + ex.getMessage(), ex);
        }

    }

    public KeystoreEntry getKeystoreEntry(RequestContext requestContext, String name) {
        log.debug("#getKeystoreEntry(RequestContext requestContext, String name): {}, {}", requestContext, name);

        try {
            String hexName = Hex.encodeHexString(name.getBytes(StandardCharsets.UTF_8));
            return executeGetPublicApiAndReturnResponseBody(
                requestContext,
                String.format("/api/v1/KeystoreEntries('%s')?keystoreName=system", hexName),
                (body) -> {
                    JSONObject responseModel = new JSONObject(body);
                    JSONObject keystoreEntryJsonObject = responseModel.getJSONObject("d");
                    KeystoreEntry keystoreEntry = getKeystoreEntryFromJson(keystoreEntryJsonObject);
                    return keystoreEntry;
                }
            );
        } catch (Exception ex) {
            log.error("Error occurred while fetching keystore entry " + ex.getMessage(), ex);
            throw new RuntimeException("Error occurred while fetching keystore entry: " + ex.getMessage(), ex);
        }

    }

    public void createUserCredentials(RequestContext requestContext, UserCredentialsCreationRequest userCredentialsCreationRequest) {
        log.debug("#createUserCredentials: requestContext = {}, userCredentialsCreationRequest = {}", requestContext, userCredentialsCreationRequest);
        try {
            executeMethodPublicApi(
                requestContext,
                "/api/v1/UserCredentials",
                userCredentialsCreationRequest,
                HttpMethod.POST,
                (responseEntity) -> {
                    handleResponse(responseEntity);
                    return null;
                }
            );
        } catch (Exception ex) {
            log.error("Error occurred while creating user credentials: " + ex.getMessage(), ex);
            throw new RuntimeException("Error occurred while creating user credentials: " + ex.getMessage(), ex);
        }
    }

    public void createOAuth2ClientCredentials(RequestContext requestContext, OAuth2ClientCredentialsCreationRequest oAuth2ClientCredentialsCreationRequest) {
        log.debug("#createOAuth2ClientCredentials: requestContext = {}, oAuth2ClientCredentialsCreationRequest = {}", requestContext, oAuth2ClientCredentialsCreationRequest);
        try {
            executeMethodPublicApi(
                requestContext,
                "/api/v1/OAuth2ClientCredentials",
                oAuth2ClientCredentialsCreationRequest,
                HttpMethod.POST,
                (responseEntity) -> {
                    handleResponse(responseEntity);
                    return null;
                }
            );
        } catch (Exception ex) {
            log.error("Error occurred while creating OAuth2 client credentials: " + ex.getMessage(), ex);
            throw new RuntimeException("Error occurred while creating OAuth2 client credentials: " + ex.getMessage(), ex);
        }
    }

    public void createSecureParameter(RequestContext requestContext, SecureParameterCreationRequest secureParameterCreationRequest) {
        log.debug("#createSecureParameter: requestContext = {}, secureParameterCreationRequest = {}", requestContext, secureParameterCreationRequest);
        try {
            executeMethodPublicApi(
                requestContext,
                "/api/v1/SecureParameters",
                secureParameterCreationRequest,
                HttpMethod.POST,
                (responseEntity) -> {
                    handleResponse(responseEntity);
                    return null;
                }
            );
        } catch (Exception ex) {
            log.error("Error occurred while creating secure parameter: " + ex.getMessage(), ex);
            throw new RuntimeException("Error occurred while creating secure parameter: " + ex.getMessage(), ex);
        }
    }


    private UserCredentials getUserCredentialsFromJson(JSONObject userCredentialsJsonObject) {
        UserCredentials userCredentials = new UserCredentials();
        userCredentials.setName(Utils.optString(userCredentialsJsonObject, "Name"));
        userCredentials.setKind(Utils.optString(userCredentialsJsonObject, "Kind"));
        userCredentials.setDescription(Utils.optString(userCredentialsJsonObject, "Description"));
        userCredentials.setUser(Utils.optString(userCredentialsJsonObject, "User"));
        userCredentials.setPassword(Utils.optString(userCredentialsJsonObject, "Password"));
        userCredentials.setCompanyId(Utils.optString(userCredentialsJsonObject, "CompanyId"));

        JSONObject securityArtifactDescriptor = userCredentialsJsonObject.optJSONObject("SecurityArtifactDescriptor");
        if (securityArtifactDescriptor != null) {
            userCredentials.setType(Utils.optString(securityArtifactDescriptor, "Type"));
            userCredentials.setDeployedBy(Utils.optString(securityArtifactDescriptor, "DeployedBy"));
            userCredentials.setDeployedOn(CpiApiUtils.parseDate(Utils.optString(securityArtifactDescriptor, "DeployedOn")));
            userCredentials.setStatus(Utils.optString(securityArtifactDescriptor, "Status"));
        }
        return userCredentials;
    }

    private OAuth2ClientCredentials getOAuth2ClientCredentialsFromJson(JSONObject oAuth2ClientCredentialsJsonObject) {
        OAuth2ClientCredentials oAuth2ClientCredentials = new OAuth2ClientCredentials();
        oAuth2ClientCredentials.setName(Utils.optString(oAuth2ClientCredentialsJsonObject, "Name"));
        oAuth2ClientCredentials.setDescription(Utils.optString(oAuth2ClientCredentialsJsonObject, "Description"));
        oAuth2ClientCredentials.setTokenServiceUrl(Utils.optString(oAuth2ClientCredentialsJsonObject, "TokenServiceUrl"));
        oAuth2ClientCredentials.setClientId(Utils.optString(oAuth2ClientCredentialsJsonObject, "ClientId"));
        oAuth2ClientCredentials.setClientSecret(Utils.optString(oAuth2ClientCredentialsJsonObject, "ClientSecret"));
        oAuth2ClientCredentials.setClientAuthentication(Utils.optString(oAuth2ClientCredentialsJsonObject, "ClientAuthentication"));
        oAuth2ClientCredentials.setScope(Utils.optString(oAuth2ClientCredentialsJsonObject, "Scope"));
        oAuth2ClientCredentials.setScopeContentType(Utils.optString(oAuth2ClientCredentialsJsonObject, "ScopeContentType"));
        oAuth2ClientCredentials.setResource(Utils.optString(oAuth2ClientCredentialsJsonObject, "Resource"));
        oAuth2ClientCredentials.setAudience(Utils.optString(oAuth2ClientCredentialsJsonObject, "Audience"));


        JSONObject securityArtifactDescriptor = oAuth2ClientCredentialsJsonObject.optJSONObject("SecurityArtifactDescriptor");
        if (securityArtifactDescriptor != null) {
            oAuth2ClientCredentials.setType(Utils.optString(securityArtifactDescriptor, "Type"));
            oAuth2ClientCredentials.setDeployedBy(Utils.optString(securityArtifactDescriptor, "DeployedBy"));
            oAuth2ClientCredentials.setDeployedOn(CpiApiUtils.parseDate(Utils.optString(securityArtifactDescriptor, "DeployedOn")));
            oAuth2ClientCredentials.setStatus(Utils.optString(securityArtifactDescriptor, "Status"));
        }
        return oAuth2ClientCredentials;
    }

    private KeystoreEntry getKeystoreEntryFromJson(JSONObject keystoreEntryJsonObject) {
        KeystoreEntry keystoreEntry = new KeystoreEntry();
        keystoreEntry.setHexalias(Utils.optString(keystoreEntryJsonObject, "Hexalias"));
        keystoreEntry.setAlias(Utils.optString(keystoreEntryJsonObject, "Alias"));
        keystoreEntry.setType(Utils.optString(keystoreEntryJsonObject, "Type"));
        return keystoreEntry;
    }

    private SecureParameter getSecureParameterFromJson(JSONObject secureParameterJsonObject) {
        SecureParameter secureParameter = new SecureParameter();
        secureParameter.setName(Utils.optString(secureParameterJsonObject, "Name"));
        secureParameter.setDescription(Utils.optString(secureParameterJsonObject, "Description"));
        secureParameter.setSecureParam(Utils.optString(secureParameterJsonObject, "SecureParam"));
        secureParameter.setDeployedBy(Utils.optString(secureParameterJsonObject, "DeployedBy"));
        secureParameter.setDeployedOn(CpiApiUtils.parseDate(Utils.optString(secureParameterJsonObject, "DeployedOn")));
        secureParameter.setStatus(Utils.optString(secureParameterJsonObject, "Status"));
        return secureParameter;
    }

    private void handleResponse(ResponseEntity<String> responseEntity) {
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            throw new ClientIntegrationException(String.format("Code: %d, Message: %s", responseEntity.getStatusCode().value(), responseEntity.getBody()));
        }
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}
