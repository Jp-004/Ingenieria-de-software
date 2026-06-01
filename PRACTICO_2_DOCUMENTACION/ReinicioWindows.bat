@echo off

echo === 1. Limpiando bases de datos viejas ===
IF EXIST db\dev.db DEL /F /Q db\dev.db
IF EXIST db\prod.db DEL /F /Q db\prod.db

echo === 2. Recreando esquemas ===
sqlite3 db\dev.db < src\main\resources\scheme.sql
sqlite3 db\prod.db < src\main\resources\scheme.sql

echo === 3. Compilando el proyecto ===
call mvn clean package

echo === 4. Iniciando la aplicacion ===
java -jar target\proye-is-1.0-SNAPSHOT.jar