@echo off

echo === 1. Compilando modificaciones ===
call mvn clean package

echo === 2. Iniciando la aplicacion (Base de datos intacta) ===
java -jar target/proye-is-1.0-SNAPSHOT.jar