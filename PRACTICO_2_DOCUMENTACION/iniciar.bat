@echo off
title Servidor Spark - Iniciar
cls

echo ===================================================
echo   INICIANDO SERVIDOR (Base de datos INTACTA)
echo ===================================================
echo.

echo [1/2] Compilando proyecto con Maven...
call mvn clean package
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] La compilacion fallo.
    goto error
)

echo.
echo [2/2] Lanzando aplicacion en http://localhost:8080 ...
echo.
java -jar target/proye-is-1.0-SNAPSHOT.jar
goto fin

:error
echo.
echo El servidor no pudo iniciar debido a un error de compilacion.
:fin
pause