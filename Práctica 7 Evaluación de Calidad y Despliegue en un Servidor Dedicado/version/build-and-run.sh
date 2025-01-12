#!/bin/bash

echo "Cleaning project..."
./mvnw clean

echo "Building project..."
./mvnw package -DskipTests

echo "Building Docker image..."
docker build -t book-movie-app .

echo "Running container..."
docker run -d --name book-movie-app -p 8083:8083 -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/book_movie_recommendation_db -e SPRING_DATASOURCE_USERNAME=root -e SPRING_DATASOURCE_PASSWORD= -v book-movie-uploads:/app/uploads --memory="512m" --memory-swap="1g" book-movie-app

echo "Container logs:"
docker logs book-movie-app
