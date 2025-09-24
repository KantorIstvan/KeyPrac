# Keycloak Authentication App with Quarkus

This is a simple authentication application built with Quarkus that integrates with Keycloak for JWT-based authentication and uses PostgreSQL as the database.

## Prerequisites

- Java 21+
- Maven 3.8+
- Docker & Docker Compose
- Postman (for testing)

## Quick Start

### 1. Start Infrastructure Services

Start PostgreSQL and Keycloak using Docker Compose:

```bash
docker-compose up -d
```

This will start:
- PostgreSQL on port 5432
- Keycloak on port 8080

### 2. Configure Keycloak

1. Open Keycloak Admin Console: http://localhost:8080
2. Login with: admin/admin
3. Create a new realm called `quarkus-realm`
4. Create a client:
   - Client ID: `quarkus-app`
   - Client Protocol: `openid-connect`
   - Access Type: `confidential`
   - Valid Redirect URIs: `http://localhost:8081/*`
   - Web Origins: `http://localhost:8081`
5. Note down the client secret from the Credentials tab
6. Update `src/main/resources/application.properties` with the actual client secret

### 3. Create Test Users in Keycloak

Create users with the following details:
- Username: `john.doe`, Password: `password123`, Roles: `user`
- Username: `jane.smith`, Password: `password123`, Roles: `user`, `manager`  
- Username: `admin.user`, Password: `password123`, Roles: `user`, `admin`

### 4. Start the Quarkus Application

```bash
./mvnw quarkus:dev
```

The application will start on http://localhost:8081

## API Endpoints for Postman Testing

### Public Endpoints (No Authentication Required)

1. **Health Check**
   - GET `http://localhost:8081/api/health`

2. **Public Endpoint**
   - GET `http://localhost:8081/api/public`

3. **Create User**
   - POST `http://localhost:8081/api/users`
   - Body (JSON):
     ```json
     {
       "username": "testuser",
       "email": "test@example.com", 
       "firstName": "Test",
       "lastName": "User"
     }
     ```

### Protected Endpoints (Requires Authentication)

#### Getting Access Token

First, get an access token from Keycloak:

**POST** `http://localhost:8080/realms/quarkus-realm/protocol/openid-connect/token`

Headers:
- `Content-Type: application/x-www-form-urlencoded`

Body (x-www-form-urlencoded):
- `grant_type`: `password`
- `client_id`: `quarkus-app`
- `client_secret`: `[your-client-secret]`
- `username`: `john.doe`
- `password`: `password123`

This will return a JSON response with `access_token`. Use this token in the Authorization header for protected endpoints.

#### Protected API Calls

For all protected endpoints, add this header:
- `Authorization: Bearer [your-access-token]`

1. **Protected Endpoint**
   - GET `http://localhost:8081/api/protected`

2. **Get Current User Info**
   - GET `http://localhost:8081/api/me`

3. **Get User by ID**
   - GET `http://localhost:8081/api/users/1`

#### Admin Only Endpoints

Use the `admin.user` credentials to get an admin token:

1. **Admin Endpoint**
   - GET `http://localhost:8081/api/admin`

2. **Get All Users**
   - GET `http://localhost:8081/api/users`

3. **Delete User**
   - DELETE `http://localhost:8081/api/users/1`

4. **Update User Roles**
   - PUT `http://localhost:8081/api/users/1/roles`
   - Body (JSON):
     ```json
     ["USER", "ADMIN"]
     ```

## Sample Postman Collection

You can import this collection into Postman for easier testing:

```json
{
  "info": {
    "name": "Keycloak Auth App",
    "description": "Collection for testing Keycloak authentication endpoints"
  },
  "variable": [
    {
      "key": "baseUrl",
      "value": "http://localhost:8081"
    },
    {
      "key": "keycloakUrl", 
      "value": "http://localhost:8080"
    },
    {
      "key": "accessToken",
      "value": ""
    }
  ]
}
```

## Database Schema

The application creates these tables:
- `users`: Main user information
- `user_roles`: User role assignments

## Troubleshooting

1. **Connection refused to PostgreSQL**: Ensure Docker containers are running
2. **Keycloak authentication fails**: Verify realm and client configuration
3. **JWT token expired**: Get a new token from Keycloak
4. **Port conflicts**: Change ports in docker-compose.yml or application.properties

## Security Roles

- `USER`: Basic authenticated user
- `MANAGER`: Can view all users
- `ADMIN`: Full administrative access

## Development

To run in development mode with live reload:

```bash
./mvnw quarkus:dev
```

The application supports hot reload - changes to Java files will be automatically recompiled.
