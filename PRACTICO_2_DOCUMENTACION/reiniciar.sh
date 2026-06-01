#!/bin/bash
clear

echo "==================================================="
echo "  REINICIANDO SERVIDOR (LIMPIANDO BASE DE DATOS)"
echo "==================================================="
echo ""

echo "[1/3] Limpiando bases de datos viejas..."
rm -f db/dev.db db/prod.db

echo "[2/3] Recreando esquemas..."
sqlite3 db/dev.db < src/main/resources/scheme.sql
sqlite3 db/prod.db < src/main/resources/scheme.sql

echo ""
echo "[3/3] Compilando proyecto con Maven..."
if mvn clean package; then
    echo ""
    echo "Lanzando aplicacion en http://localhost:8080 ..."
    echo ""
    java -jar target/proye-is-1.0-SNAPSHOT.jar
else
    echo ""
    echo "❌ [ERROR] La compilacion fallo. Revisa los errores arriba."
    echo ""
    read -p "Presiona [Enter] para cerrar..."
fi
