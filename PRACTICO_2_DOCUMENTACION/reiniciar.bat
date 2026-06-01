@echo off
title Servidor Spark - Reiniciar (WIPE)
cls

echo ===================================================
echo   REINICIANDO SERVIDOR (LIMPIANDO BASE DE DATOS)
echo ===================================================
echo.

echo [1/3] Limpiando bases de datos viejas...
IF EXIST db\dev.db DEL /F /Q db\dev.db
IF EXIST db\prod.db DEL /F /Q db\prod.db

echo [2/3] Recreando esquemas...
sqlite3 db\dev.db < src\main\resources\scheme.sql
sqlite3 db\prod.db < src\main\resources\scheme.sql

echo.
echo [3/3] Compilando proyecto con Maven...
call mvn clean package
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] La compilacion fallo.
    goto error
)

echo.
echo Lanzando aplicacion en http://localhost:8080 ...
echo.
java -jar target/proye-is-1.0-SNAPSHOT.jar
goto fin

:error
echo.
echo El servidor no pudo iniciar debido a un error de compilacion.
:fin
pause