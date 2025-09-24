#!/bin/bash

echo "🚀 Starting Keycloak Auth App Setup..."

# Start Docker services
echo "📦 Starting PostgreSQL and Keycloak..."
docker-compose up -d

# Wait for services to be ready
echo "⏳ Waiting for services to start..."
sleep 30

# Check if services are running
if docker ps | grep -q "keycloak"; then
    echo "✅ Keycloak is running on http://localhost:8080"
else
    echo "❌ Keycloak failed to start"
    exit 1
fi

if docker ps | grep -q "postgres"; then
    echo "✅ PostgreSQL is running on port 5432"
else
    echo "❌ PostgreSQL failed to start"
    exit 1
fi

echo ""
echo "🔧 Next Steps:"
echo "1. Configure Keycloak:"
echo "   - Open http://localhost:8080"
echo "   - Login with admin/admin"
echo "   - Create realm 'quarkus-realm'"
echo "   - Create client 'quarkus-app'"
echo "   - Update client secret in application.properties"
echo ""
echo "2. Create test users in Keycloak:"
echo "   - john.doe (password: password123, roles: user)"
echo "   - admin.user (password: password123, roles: admin)"
echo ""
echo "3. Start the Quarkus application:"
echo "   ./mvnw quarkus:dev"
echo ""
echo "4. Import Postman collection:"
echo "   Import 'Keycloak-Auth-App.postman_collection.json'"
echo ""
echo "📚 See README.md for detailed instructions"
