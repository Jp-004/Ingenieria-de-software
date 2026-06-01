#!/bin/bash

echo "=== 1. Compilando modificaciones ==="
mvn clean package

echo "=== 2. Iniciando la aplicación (Base de datos intacta) ==="
java -jar target/proye-is-1.0-SNAPSHOT.jar