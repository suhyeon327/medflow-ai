@echo off
title MedFlow Development

cd /d C:\medflow-ai

echo ========================================
echo       MedFlow Development
echo ========================================
echo.

echo [1/3] Loading environment variables...
call C:\medflow-ai\medflow-env.bat

echo.
echo [2/3] Starting MySQL...
docker compose up -d mysql

if errorlevel 1 (
    echo.
    echo [ERROR] Failed to start MySQL.
    pause
    exit /b 1
)

echo.
echo Waiting for MySQL to become healthy...

:WAIT_MYSQL
set "MYSQL_HEALTH="

for /f "delims=" %%H in ('docker inspect --format="{{.State.Health.Status}}" medflow-mysql 2^>nul') do set "MYSQL_HEALTH=%%H"

if /I "%MYSQL_HEALTH%"=="healthy" goto MYSQL_READY

if /I "%MYSQL_HEALTH%"=="unhealthy" (
    echo.
    echo [ERROR] MySQL is unhealthy.
    echo.
    docker logs --tail 30 medflow-mysql
    pause
    exit /b 1
)

timeout /t 2 /nobreak >nul
goto WAIT_MYSQL


:MYSQL_READY
echo MySQL is healthy and ready.

echo.
echo [3/3] Starting Backend and Frontend...
echo.

start "MedFlow Backend" cmd /k "cd /d C:\medflow-ai\backend && call gradlew.bat bootRun"

start "MedFlow Frontend" cmd /k "cd /d C:\medflow-ai\frontend && npm run dev"

echo.
echo ========================================
echo       MedFlow Started
echo ========================================
echo.
echo Backend  : http://localhost:8080
echo Frontend : http://localhost:5173
echo MySQL    : localhost:3306
echo.