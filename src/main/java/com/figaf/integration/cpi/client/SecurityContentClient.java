package com.figaf.integration.cpi.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.common.utils.Utils;
import com.figaf.integration.cpi.entity.security.KeystoreEntry;
import com.figaf.integration.cpi.entity.security.UserCredentials;
import com.figaf.integration.cpi.utils.CpiApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.http.HttpHeaders;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
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
        log.debug("#getUserCredentialsList(RequestContext requestContext): {}", requestContext);
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
                            userCredentialsList.add(getUserCredentialsFromJson(userCredentialsJsonObject));
                        }
                        return userCredentialsList;
                    }
            );
        } catch (Exception ex) {
            log.error("Error occurred while fetching user credentials list " + ex.getMessage(), ex);
            throw new RuntimeException("Error occurred while fetching user credentials list: " + ex.getMessage(), ex);
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

    private UserCredentials getUserCredentialsFromJson(JSONObject userCredentialsJsonObject) {
        UserCredentials userCredentials = new UserCredentials();
        userCredentials.setName(Utils.optString(userCredentialsJsonObject, "Name"));
        userCredentials.setKind(Utils.optString(userCredentialsJsonObject, "Kind"));
        userCredentials.setDescription(Utils.optString(userCredentialsJsonObject, "Description"));
        userCredentials.setUser(Utils.optString(userCredentialsJsonObject, "User"));
        userCredentials.setPassword(Utils.optString(userCredentialsJsonObject, "Password"));
        userCredentials.setCompanyId(Utils.optString(userCredentialsJsonObject, "CompanyId"));
        userCredentials.setType(Utils.optString(userCredentialsJsonObject, "Type"));
        userCredentials.setDeployedBy(Utils.optString(userCredentialsJsonObject, "DeployedBy"));
        userCredentials.setDeployedOn(CpiApiUtils.parseDate(Utils.optString(userCredentialsJsonObject, "DeployedOn")));
        userCredentials.setStatus(Utils.optString(userCredentialsJsonObject, "Status"));
        return userCredentials;
    }

    private KeystoreEntry getKeystoreEntryFromJson(JSONObject keystoreEntryJsonObject) {
        KeystoreEntry keystoreEntry = new KeystoreEntry();
        keystoreEntry.setHexalias(Utils.optString(keystoreEntryJsonObject, "Hexalias"));
        keystoreEntry.setAlias(Utils.optString(keystoreEntryJsonObject, "Alias"));
        keystoreEntry.setType(Utils.optString(keystoreEntryJsonObject, "Type"));
        return keystoreEntry;
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}
