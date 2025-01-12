#!/bin/bash
echo "Building application..."
./mvnw clean package -DskipTests

echo "Building Docker images..."
docker-compose build

echo "Starting services..."
docker-compose up -d

echo "Checking service health..."
sleep 30

if docker ps | grep -q book-movie-app; then
    echo "Application deployed successfully!"
else
    echo "Deployment failed. Check logs with: docker-compose logs"
    exit 1
fi
