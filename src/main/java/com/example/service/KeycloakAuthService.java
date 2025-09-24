package com.example.service;

import com.example.dto.LoginRequest;
import com.example.dto.LoginResponse;
import com.example.dto.UserCreateRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

@ApplicationScoped
public class KeycloakAuthService {

    @ConfigProperty(name = "quarkus.oidc.auth-server-url")
    String authServerUrl;

    @ConfigProperty(name = "quarkus.oidc.client-id")
    String clientId;

    @ConfigProperty(name = "quarkus.oidc.credentials.secret")
    String clientSecret;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public LoginResponse login(LoginRequest loginRequest) {
        Client client = ClientBuilder.newClient();
        try {
            String tokenUrl = authServerUrl + "/protocol/openid-connect/token";

            Form form = new Form()
                    .param("grant_type", "password")
                    .param("client_id", clientId)
                    .param("client_secret", clientSecret)
                    .param("username", loginRequest.email)  // Using email as username
                    .param("password", loginRequest.password);

            Response response = client.target(tokenUrl)
                    .request(MediaType.APPLICATION_FORM_URLENCODED)
                    .post(Entity.form(form));

            if (response.getStatus() == 200) {
                String responseBody = response.readEntity(String.class);
                JsonNode jsonNode = objectMapper.readTree(responseBody);

                return new LoginResponse(
                        jsonNode.get("access_token").asText(),
                        jsonNode.has("refresh_token") ? jsonNode.get("refresh_token").asText() : null,
                        jsonNode.get("token_type").asText(),
                        jsonNode.get("expires_in").asInt()
                );
            } else {
                throw new RuntimeException("Authentication failed: " + response.readEntity(String.class));
            }
        } catch (Exception e) {
            throw new RuntimeException("Login failed: " + e.getMessage(), e);
        } finally {
            client.close();
        }
    }

    public void createKeycloakUser(UserCreateRequest userRequest) {
        Client client = ClientBuilder.newClient();
        try {
            // First, get admin access token
            String adminToken = getAdminAccessToken(client);

            // Create user payload
            ObjectNode userPayload = objectMapper.createObjectNode();
            userPayload.put("username", userRequest.email); // Using email as username
            userPayload.put("email", userRequest.email);
            userPayload.put("firstName", userRequest.firstName);
            userPayload.put("lastName", userRequest.lastName);
            userPayload.put("enabled", true);
            userPayload.put("emailVerified", true);

            // Add credentials
            ArrayNode credentials = objectMapper.createArrayNode();
            ObjectNode credential = objectMapper.createObjectNode();
            credential.put("type", "password");
            credential.put("value", userRequest.password);
            credential.put("temporary", false);
            credentials.add(credential);
            userPayload.set("credentials", credentials);

            // Extract realm name from auth-server-url
            String realmName = extractRealmName(authServerUrl);
            String adminApiUrl = authServerUrl.replace("/realms/" + realmName, "") + "/admin/realms/" + realmName + "/users";

            Response response = client.target(adminApiUrl)
                    .request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + adminToken)
                    .post(Entity.json(userPayload.toString()));

            if (response.getStatus() != 201) {
                String errorBody = response.readEntity(String.class);
                throw new RuntimeException("Failed to create user in Keycloak: " + errorBody);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to create Keycloak user: " + e.getMessage(), e);
        } finally {
            client.close();
        }
    }

    private String extractRealmName(String authServerUrl) {
        // Extract realm name from URL like "http://localhost:8080/realms/quarkus-realm"
        String[] parts = authServerUrl.split("/realms/");
        return parts.length > 1 ? parts[1] : "master";
    }

    private String getAdminAccessToken(Client client) {
        try {
            // Use master realm for admin authentication
            String baseUrl = authServerUrl.substring(0, authServerUrl.indexOf("/realms/"));
            String tokenUrl = baseUrl + "/realms/master/protocol/openid-connect/token";

            Form form = new Form()
                    .param("grant_type", "password")
                    .param("client_id", "admin-cli")
                    .param("username", "admin")
                    .param("password", "admin");

            Response response = client.target(tokenUrl)
                    .request(MediaType.APPLICATION_FORM_URLENCODED)
                    .post(Entity.form(form));

            if (response.getStatus() == 200) {
                String responseBody = response.readEntity(String.class);
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                return jsonNode.get("access_token").asText();
            } else {
                throw new RuntimeException("Failed to get admin token: " + response.readEntity(String.class));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to authenticate as admin: " + e.getMessage(), e);
        }
    }

    public void createKeycloakUser(String username, String email, String firstName, String lastName, String password) {
        UserCreateRequest userRequest = new UserCreateRequest();
        userRequest.username = username;
        userRequest.email = email;
        userRequest.firstName = firstName;
        userRequest.lastName = lastName;
        userRequest.password = password;
        createKeycloakUser(userRequest);
    }
}
