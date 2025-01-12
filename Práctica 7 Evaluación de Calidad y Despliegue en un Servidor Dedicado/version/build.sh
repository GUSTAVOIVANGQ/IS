#!/bin/bash

echo "Limpiando proyecto..."
./mvnw clean

echo "Instalando dependencias..."
./mvnw dependency:resolve

echo "Compilando proyecto..."
./mvnw package -DskipTests

echo "Construyendo imagen Docker..."
docker build -t book-movie-app .
