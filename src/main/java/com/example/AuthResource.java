package com.example;

import com.example.dto.LoginRequest;
import com.example.dto.LoginResponse;
import com.example.dto.UserCreateRequest;
import com.example.dto.UserResponse;
import com.example.service.KeycloakAuthService;
import com.example.service.UserService;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.List;
import java.util.Set;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    UserService userService;

    @Inject
    KeycloakAuthService keycloakAuthService;

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    JsonWebToken jwt;

    // Authentication endpoints
    @POST
    @Path("/auth/login")
    public Response login(@Valid LoginRequest loginRequest) {
        try {
            LoginResponse loginResponse = keycloakAuthService.login(loginRequest);
            return Response.ok(loginResponse).build();
        } catch (Exception e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\": \"Invalid credentials\"}")
                    .build();
        }
    }

    @POST
    @Path("/auth/register")
    public Response register(@Valid UserCreateRequest request) {
        try {
            // Create user in local database
            UserResponse user = userService.createUser(request);

            // Create user in Keycloak automatically
            keycloakAuthService.createKeycloakUser(request);

            return Response.status(Response.Status.CREATED)
                    .entity("{\"message\": \"User registered successfully in both local database and Keycloak.\", \"user\": " +
                           "{\"id\": " + user.id + ", \"username\": \"" + user.username + "\", \"email\": \"" + user.email + "\"}}")
                    .build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Registration failed: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    // Public endpoint - no authentication required
    @GET
    @Path("/public")
    public Response publicEndpoint() {
        return Response.ok()
                .entity("{\"message\": \"This is a public endpoint accessible without authentication\"}")
                .build();
    }

    // Protected endpoint - requires authentication
    @GET
    @Path("/protected")
    @Authenticated
    public Response protectedEndpoint() {
        String username = securityIdentity.getPrincipal().getName();
        return Response.ok()
                .entity("{\"message\": \"Hello " + username + "! This is a protected endpoint\", \"roles\": " + securityIdentity.getRoles() + "}")
                .build();
    }

    // Admin only endpoint
    @GET
    @Path("/admin")
    @RolesAllowed("admin")
    public Response adminEndpoint() {
        String username = securityIdentity.getPrincipal().getName();
        return Response.ok()
                .entity("{\"message\": \"Hello " + username + "! This is an admin-only endpoint\"}")
                .build();
    }

    // Get current user info from JWT token
    @GET
    @Path("/me")
    @Authenticated
    public Response getCurrentUser() {
        String username = securityIdentity.getPrincipal().getName();
        String email = jwt.getClaim("email");
        String firstName = jwt.getClaim("given_name");
        String lastName = jwt.getClaim("family_name");

        return Response.ok()
                .entity("{\"username\": \"" + username + "\", \"email\": \"" + email +
                       "\", \"firstName\": \"" + firstName + "\", \"lastName\": \"" + lastName +
                       "\", \"roles\": " + securityIdentity.getRoles() + "}")
                .build();
    }

    // User management endpoints
    @POST
    @Path("/users")
    public Response createUser(@Valid UserCreateRequest request) {
        UserResponse user = userService.createUser(request);
        return Response.status(Response.Status.CREATED).entity(user).build();
    }

    @GET
    @Path("/users")
    @RolesAllowed({"admin", "manager"})
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    @GET
    @Path("/users/{id}")
    @Authenticated
    public UserResponse getUserById(@PathParam("id") Long id) {
        return userService.getUserById(id);
    }

    @DELETE
    @Path("/users/{id}")
    @RolesAllowed("admin")
    public Response deleteUser(@PathParam("id") Long id) {
        userService.deleteUser(id);
        return Response.noContent().build();
    }

    @PUT
    @Path("/users/{id}/roles")
    @RolesAllowed("admin")
    public UserResponse updateUserRoles(@PathParam("id") Long id, Set<User.Role> roles) {
        return userService.updateUserRoles(id, roles);
    }

    // Health check endpoint
    @GET
    @Path("/health")
    public Response health() {
        return Response.ok()
                .entity("{\"status\": \"UP\", \"timestamp\": \"" + java.time.LocalDateTime.now() + "\"}")
                .build();
    }
}
