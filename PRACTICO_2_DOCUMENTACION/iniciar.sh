#!/bin/bash
clear

echo "==================================================="
echo "  INICIANDO SERVIDOR (Base de datos INTACTA)"
echo "==================================================="
echo ""

echo "[1/2] Compilando proyecto con Maven..."
if mvn clean package; then
    echo ""
    echo "[2/2] Lanzando aplicacion en http://localhost:8080 ..."
    echo ""
    java -jar target/proye-is-1.0-SNAPSHOT.jar
else
    echo ""
    echo "❌ [ERROR] La compilacion fallo. Revisa los errores arriba."
    echo ""
    read -p "Presiona [Enter] para cerrar..."
fi
