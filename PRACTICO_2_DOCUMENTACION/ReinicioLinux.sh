#!/bin/bash

echo "=== 1. Limpiando bases de datos viejas ==="
# El -f evita que tire error si el archivo todavía no existe
rm -f db/dev.db db/prod.db

echo "=== 2. Recreando esquemas ==="
sqlite3 db/dev.db < src/main/resources/scheme.sql
sqlite3 db/prod.db < src/main/resources/scheme.sql

echo "=== 3. Compilando el proyecto ==="
mvn clean package

echo "=== 4. Iniciando la aplicación ==="
java -jar target/proye-is-1.0-SNAPSHOT.jar