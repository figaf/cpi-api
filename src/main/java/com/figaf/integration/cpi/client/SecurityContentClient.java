package com.figaf.integration.cpi.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.common.utils.Utils;
import com.figaf.integration.cpi.entity.security.*;
import com.figaf.integration.cpi.entity.security.request.OAuth2ClientCredentialsRequest;
import com.figaf.integration.cpi.entity.security.request.SecureParameterRequest;
import com.figaf.integration.cpi.entity.security.request.UserCredentialsRequest;
import com.figaf.integration.cpi.utils.CpiApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.lang.String.format;

/**
 * @author Arsenii Istlentev
 */
@Slf4j
//TODO make separate clients for UserCredentials, OAuth2ClientCredentials, SecureParameters, Keystores
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
                        if (skipAdditionalObjects && userCredentials.getKind() == null) { //the kind value is resolved as null for not user credential objects
                            continue;
                        }
                        userCredentialsList.add(userCredentials);
                    }
                    return userCredentialsList;
                }
            );
        } catch (Exception ex) {
            log.error("Error occurred while fetching user credentials list " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while fetching user credentials list: " + ex.getMessage(), ex);
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
            throw new ClientIntegrationException("Error occurred while fetching OAuth2 client credentials list: " + ex.getMessage(), ex);
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
            throw new ClientIntegrationException("Error occurred while fetching secure parameters: " + ex.getMessage(), ex);
        }
    }

    public UserCredentials getUserCredentials(RequestContext requestContext, String name) {
        log.debug("#getUserCredentials(RequestContext requestContext, String name): {}, {}", requestContext, name);
        try {
            return executeGetPublicApiAndReturnResponseBody(
                requestContext,
                format("/api/v1/UserCredentials('%s')?$format=json", name),
                (body) -> {
                    JSONObject responseModel = new JSONObject(body);
                    JSONObject userCredentialsJsonObject = responseModel.getJSONObject("d");
                    return getUserCredentialsFromJson(userCredentialsJsonObject);
                }
            );
        } catch (HttpClientErrorException.NotFound ex) {
            log.error("User credentials are not found by {}: {}", name, ExceptionUtils.getMessage(ex));
            return null;
        } catch (Exception ex) {
            log.error("Error occurred while fetching user credentials " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while fetching user credentials: " + ex.getMessage(), ex);
        }
    }

    public OAuth2ClientCredentials getOAuth2ClientCredentials(RequestContext requestContext, String name) {
        log.debug("#getOAuth2ClientCredentials(RequestContext requestContext, String name): {}, {}", requestContext, name);
        try {
            return executeGetPublicApiAndReturnResponseBody(
                    requestContext,
                    format("/api/v1/OAuth2ClientCredentials('%s')?$format=json", name),
                    (body) -> {
                        JSONObject responseModel = new JSONObject(body);
                        JSONObject oauth2ClientCredentialsJsonObject = responseModel.getJSONObject("d");
                        return getOAuth2ClientCredentialsFromJson(oauth2ClientCredentialsJsonObject);
                    }
            );
        } catch (HttpClientErrorException.NotFound ex) {
            log.error("OAuth2 client credentials are not found by {}: {}", name, ExceptionUtils.getMessage(ex));
            return null;
        } catch (Exception ex) {
            log.error("Error occurred while fetching OAuth2 client credentials " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while fetching OAuth2 client credentials: " + ex.getMessage(), ex);
        }
    }

    public SecureParameter getSecureParameter(RequestContext requestContext, String name) {
        log.debug("#getSecureParameter(RequestContext requestContext, String name): {}, {}", requestContext, name);
        try {
            return executeGetPublicApiAndReturnResponseBody(
                    requestContext,
                    format("/api/v1/SecureParameters('%s')?$format=json", name),
                    (body) -> {
                        JSONObject responseModel = new JSONObject(body);
                        JSONObject secureParameterJsonObject = responseModel.getJSONObject("d");
                        return getSecureParameterFromJson(secureParameterJsonObject);
                    }
            );
        } catch (HttpClientErrorException.NotFound ex) {
            log.error("Secure parameter not found by {}: {}", name, ExceptionUtils.getMessage(ex));
            return null;
        } catch (Exception ex) {
            log.error("Error occurred while fetching secure parameter " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while fetching secure parameter: " + ex.getMessage(), ex);
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
            throw new ClientIntegrationException("Error occurred while fetching keystore entries: " + ex.getMessage(), ex);
        }

    }

    public KeystoreEntry getKeystoreEntry(RequestContext requestContext, String name) {
        log.debug("#getKeystoreEntry(RequestContext requestContext, String name): {}, {}", requestContext, name);

        try {
            String hexName = Hex.encodeHexString(name.getBytes(StandardCharsets.UTF_8));
            return executeGetPublicApiAndReturnResponseBody(
                requestContext,
                format("/api/v1/KeystoreEntries('%s')?keystoreName=system", hexName),
                (body) -> {
                    JSONObject responseModel = new JSONObject(body);
                    JSONObject keystoreEntryJsonObject = responseModel.getJSONObject("d");
                    KeystoreEntry keystoreEntry = getKeystoreEntryFromJson(keystoreEntryJsonObject);
                    return keystoreEntry;
                }
            );
        } catch (Exception ex) {
            log.error("Error occurred while fetching keystore entry " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while fetching keystore entry: " + ex.getMessage(), ex);
        }

    }

    public void createUserCredentials(RequestContext requestContext, UserCredentialsRequest userCredentialsRequest) {
        log.debug("#createUserCredentials: requestContext = {}, userCredentialsRequest = {}", requestContext, userCredentialsRequest);
        try {
            executeMethodPublicApi(
                requestContext,
                "/api/v1/UserCredentials",
                userCredentialsRequest,
                HttpMethod.POST,
                (responseEntity) -> {
                    handleResponse(responseEntity);
                    return null;
                }
            );
        } catch (Exception ex) {
            log.error("Error occurred while creating user credentials: " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while creating user credentials: " + ex.getMessage(), ex);
        }
    }

    public void createOAuth2ClientCredentials(RequestContext requestContext, OAuth2ClientCredentialsRequest oAuth2ClientCredentialsRequest) {
        log.debug("#createOAuth2ClientCredentials: requestContext = {}, oAuth2ClientCredentialsRequest = {}", requestContext, oAuth2ClientCredentialsRequest);
        try {
            executeMethodPublicApi(
                requestContext,
                "/api/v1/OAuth2ClientCredentials",
                oAuth2ClientCredentialsRequest,
                HttpMethod.POST,
                (responseEntity) -> {
                    handleResponse(responseEntity);
                    return null;
                }
            );
        } catch (Exception ex) {
            log.error("Error occurred while creating OAuth2 client credentials: " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while creating OAuth2 client credentials: " + ex.getMessage(), ex);
        }
    }

    public void createSecureParameter(RequestContext requestContext, SecureParameterRequest secureParameterRequest) {
        log.debug("#createSecureParameter: requestContext = {}, secureParameterRequest = {}", requestContext, secureParameterRequest);
        try {
            executeMethodPublicApi(
                requestContext,
                "/api/v1/SecureParameters",
                secureParameterRequest,
                HttpMethod.POST,
                (responseEntity) -> {
                    handleResponse(responseEntity);
                    return null;
                }
            );
        } catch (Exception ex) {
            log.error("Error occurred while creating secure parameter: " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while creating secure parameter: " + ex.getMessage(), ex);
        }
    }

    public void updateUserCredentials(RequestContext requestContext, UserCredentialsRequest userCredentialsRequest) {
        log.debug("#createUserCredentials: requestContext = {}, userCredentialsRequest = {}", requestContext, userCredentialsRequest);
        try {
            executeMethodPublicApi(
                requestContext,
                format("/api/v1/UserCredentials('%s')", userCredentialsRequest.getName()),
                userCredentialsRequest,
                HttpMethod.PUT,
                (responseEntity) -> {
                    handleResponse(responseEntity);
                    return null;
                }
            );
        } catch (Exception ex) {
            log.error("Error occurred while updating user credentials: " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while updating user credentials: " + ex.getMessage(), ex);
        }
    }

    public void updateOAuth2ClientCredentials(RequestContext requestContext, OAuth2ClientCredentialsRequest oAuth2ClientCredentialsRequest) {
        log.debug("#updateOAuth2ClientCredentials: requestContext = {}, oAuth2ClientCredentialsRequest = {}", requestContext, oAuth2ClientCredentialsRequest);
        try {
            executeMethodPublicApi(
                requestContext,
                format("/api/v1/OAuth2ClientCredentials('%s')", oAuth2ClientCredentialsRequest.getName()),
                oAuth2ClientCredentialsRequest,
                HttpMethod.PUT,
                (responseEntity) -> {
                    handleResponse(responseEntity);
                    return null;
                }
            );
        } catch (Exception ex) {
            log.error("Error occurred while updating OAuth2 client credentials: " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while updating OAuth2 client credentials: " + ex.getMessage(), ex);
        }
    }

    public void updateSecureParameter(RequestContext requestContext, SecureParameterRequest secureParameterRequest) {
        log.debug("#updateSecureParameter: requestContext = {}, secureParameterRequest = {}", requestContext, secureParameterRequest);
        try {
            executeMethodPublicApi(
                requestContext,
                format("/api/v1/SecureParameters('%s')", secureParameterRequest.getName()),
                secureParameterRequest,
                HttpMethod.PUT,
                (responseEntity) -> {
                    handleResponse(responseEntity);
                    return null;
                }
            );
        } catch (Exception ex) {
            log.error("Error occurred while updating secure parameter: " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while updating secure parameter: " + ex.getMessage(), ex);
        }
    }

    public void deleteUserCredentials(RequestContext requestContext, String name) {
        log.debug("#deleteUserCredentials: requestContext = {}, name = {}", requestContext, name);
        try {
            executeDeletePublicApi(
                requestContext,
                format("/api/v1/UserCredentials('%s')", name),
                Objects::nonNull
            );
        } catch (Exception ex) {
            log.error("Error occurred while deleting user credentials: " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while deleting user credentials: " + ex.getMessage(), ex);
        }
    }

    public void deleteOAuth2ClientCredentials(RequestContext requestContext, String name) {
        log.debug("#deleteOAuth2ClientCredentials: requestContext = {}, name = {}", requestContext, name);
        try {
            executeDeletePublicApi(
                requestContext,
                format("/api/v1/OAuth2ClientCredentials('%s')", name),
                Objects::nonNull
            );
        } catch (Exception ex) {
            log.error("Error occurred while deleting OAuth2 client credentials: " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while deleting OAuth2 client credentials: " + ex.getMessage(), ex);
        }
    }

    public void deleteSecureParameter(RequestContext requestContext, String name) {
        log.debug("#deleteSecureParameter: requestContext = {}, name = {}", requestContext, name);
        try {
            executeDeletePublicApi(
                requestContext,
                format("/api/v1/SecureParameters('%s')", name),
                Objects::nonNull
            );
        } catch (Exception ex) {
            log.error("Error occurred while deleting secure parameter: " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while deleting secure parameter: " + ex.getMessage(), ex);
        }
    }

    private UserCredentials getUserCredentialsFromJson(JSONObject userCredentialsJsonObject) {
        UserCredentials userCredentials = new UserCredentials();
        userCredentials.setName(Utils.optString(userCredentialsJsonObject, "Name"));

        String kindValue = Utils.optString(userCredentialsJsonObject, "Kind");
        userCredentials.setKind(UserCredentialsKind.getByApiValue(kindValue));
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
            throw new ClientIntegrationException(format("Code: %d, Message: %s", responseEntity.getStatusCode().value(), responseEntity.getBody()));
        }
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}
