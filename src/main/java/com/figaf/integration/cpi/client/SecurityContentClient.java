package com.figaf.integration.cpi.client;

import com.figaf.integration.common.entity.ConnectionProperties;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.common.utils.Utils;
import com.figaf.integration.cpi.entity.security.KeystoreEntry;
import com.figaf.integration.cpi.entity.security.UserCredentials;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.HttpClientUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Arsenii Istlentev
 */
@Slf4j
public class SecurityContentClient extends CpiBaseClient {

    public SecurityContentClient(String ssoUrl, HttpClientsFactory httpClientsFactory) {
        super(ssoUrl, httpClientsFactory);
    }

    public List<UserCredentials> getUserCredentialsList(ConnectionProperties connectionProperties) {
        log.debug("#getUserCredentialsList(ConnectionProperties connectionProperties): {}", connectionProperties);
        try {
            URI uri = UriComponentsBuilder.newInstance()
                .scheme(connectionProperties.getProtocol())
                .host(connectionProperties.getHost())
                .path("/api/v1/UserCredentials")
                .queryParam("$format", "json")
                .build()
                .encode()
                .toUri();

            HttpClient client = httpClientsFactory.createHttpClient();

            Header basicAuthHeader = createBasicAuthHeader(connectionProperties);

            HttpGet request = new HttpGet(uri);
            request.setHeader("Content-type", "application/json");
            request.setHeader(basicAuthHeader);
            HttpResponse response = null;
            try {

                response = client.execute(request);

                if (response.getStatusLine().getStatusCode() == 200) {
                    JSONObject responseModel = new JSONObject(IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8));
                    JSONArray results = responseModel.getJSONObject("d").getJSONArray("results");

                    List<UserCredentials> userCredentialsList = new ArrayList<>();
                    for (int ind = 0; ind < results.length(); ind++) {
                        JSONObject userCredentialsJsonObject = results.getJSONObject(ind);
                        userCredentialsList.add(getUserCredentialsFromJson(userCredentialsJsonObject));
                    }
                    return userCredentialsList;
                } else {
                    throw new RuntimeException("Couldn't execute user credentials list GET request:\n" + IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8));
                }

            } finally {
                HttpClientUtils.closeQuietly(response);
            }

        } catch (Exception ex) {
            log.error("Error occurred while fetching user credentials list " + ex.getMessage(), ex);
            throw new RuntimeException("Error occurred while fetching user credentials list: " + ex.getMessage(), ex);
        }
    }

    public UserCredentials getUserCredentials(ConnectionProperties connectionProperties, String name) {
        log.debug("#getUserCredentials(ConnectionProperties connectionProperties, String name): {}, {}", connectionProperties, name);
        try {
            URI uri = UriComponentsBuilder.newInstance()
                .scheme(connectionProperties.getProtocol())
                .host(connectionProperties.getHost())
                .path(String.format("/api/v1/UserCredentials('%s')", name))
                .queryParam("$format", "json")
                .build()
                .encode()
                .toUri();

            HttpClient client = httpClientsFactory.createHttpClient();

            Header basicAuthHeader = createBasicAuthHeader(connectionProperties);

            HttpGet request = new HttpGet(uri);
            request.setHeader("Content-type", "application/json");
            request.setHeader(basicAuthHeader);
            HttpResponse response = null;
            try {

                response = client.execute(request);

                if (response.getStatusLine().getStatusCode() == 200) {
                    JSONObject responseModel = new JSONObject(IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8));
                    JSONObject userCredentialsJsonObject = responseModel.getJSONObject("d");
                    return getUserCredentialsFromJson(userCredentialsJsonObject);
                } else {
                    throw new RuntimeException("Couldn't execute user credentials GET request:\n" + IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8));
                }

            } finally {
                HttpClientUtils.closeQuietly(response);
            }

        } catch (Exception ex) {
            log.error("Error occurred while fetching user credentials " + ex.getMessage(), ex);
            throw new RuntimeException("Error occurred while fetching user credentials: " + ex.getMessage(), ex);
        }
    }

    public List<KeystoreEntry> getKeystoreEntryList(ConnectionProperties connectionProperties) {
        log.debug("#getKeystoreEntryList(ConnectionProperties connectionProperties): {}", connectionProperties);
        try {
            URI uri = UriComponentsBuilder.newInstance()
                .scheme(connectionProperties.getProtocol())
                .host(connectionProperties.getHost())
                .path("/api/v1/KeystoreEntries")
                .build()
                .encode()
                .toUri();

            HttpClient client = httpClientsFactory.createHttpClient();

            Header basicAuthHeader = createBasicAuthHeader(connectionProperties);

            HttpGet request = new HttpGet(uri);
            request.setHeader("Content-type", "application/json");
            request.setHeader("Accept", "application/json");
            request.setHeader(basicAuthHeader);
            HttpResponse response = null;
            try {

                response = client.execute(request);

                if (response.getStatusLine().getStatusCode() == 200) {
                    JSONObject responseModel = new JSONObject(IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8));
                    JSONArray results = responseModel.getJSONObject("d").getJSONArray("results");

                    List<KeystoreEntry> keystoreEntries = new ArrayList<>();
                    for (int ind = 0; ind < results.length(); ind++) {
                        JSONObject keystoreEntryJsonObject = results.getJSONObject(ind);
                        keystoreEntries.add(getKeystoreEntryFromJson(keystoreEntryJsonObject));
                    }
                    return keystoreEntries;
                } else {
                    throw new RuntimeException("Couldn't execute keystore entries GET request:\n" + IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8));
                }

            } finally {
                HttpClientUtils.closeQuietly(response);
            }

        } catch (Exception ex) {
            log.error("Error occurred while fetching keystore entries " + ex.getMessage(), ex);
            throw new RuntimeException("Error occurred while fetching keystore entries: " + ex.getMessage(), ex);
        }
    }

    public KeystoreEntry getKeystoreEntry(ConnectionProperties connectionProperties, String name) {
        log.debug("#getKeystoreEntry(ConnectionProperties connectionProperties, String name): {}, {}", connectionProperties, name);
        try {
            String hexName = Hex.encodeHexString(name.getBytes(StandardCharsets.UTF_8));
            URI uri = UriComponentsBuilder.newInstance()
                .scheme(connectionProperties.getProtocol())
                .host(connectionProperties.getHost())
                .path(String.format("/api/v1/KeystoreEntries('%s')", hexName))
                .queryParam("keystoreName", "system")
                .build()
                .encode()
                .toUri();

            HttpClient client = httpClientsFactory.createHttpClient();

            Header basicAuthHeader = createBasicAuthHeader(connectionProperties);

            HttpGet request = new HttpGet(uri);
            request.setHeader("Content-type", "application/json");
            request.setHeader("Accept", "application/json");
            request.setHeader(basicAuthHeader);
            HttpResponse response = null;
            try {

                response = client.execute(request);

                if (response.getStatusLine().getStatusCode() == 200) {
                    JSONObject responseModel = new JSONObject(IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8));
                    JSONObject keystoreEntryJsonObject = responseModel.getJSONObject("d");
                    KeystoreEntry keystoreEntry = getKeystoreEntryFromJson(keystoreEntryJsonObject);
                    return keystoreEntry;
                } else {
                    throw new RuntimeException("Couldn't execute keystore entry GET request:\n" + IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8));
                }

            } finally {
                HttpClientUtils.closeQuietly(response);
            }

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
        String deployedOn = Utils.optString(userCredentialsJsonObject, "DeployedOn");
        userCredentials.setDeployedOn(deployedOn != null
            ? new Timestamp(Long.parseLong(deployedOn.replaceAll("[^0-9]", "")))
            : null
        );
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
