@echo off
REM p0y0money Deploy Script (Windows Batch)
REM Usage: deploy.bat

setlocal enabledelayedexpansion

set JAR_FILE=build\libs\p0y0money-1.0-SNAPSHOT.jar
set SSH_HOST=05.jpn.gg
set SSH_USER=rxhvoehx
set SSH_PORT=2032
set SSH_KEY=%USERPROFILE%\.ssh\id_rsa
set REMOTE_PATH=/home/container/plugins

echo ======================================
echo Deploy: p0y0money
echo ======================================
echo.

if not exist "%JAR_FILE%" (
    echo Error: JAR file not found: %JAR_FILE%
    exit /b 1
)

if not exist "%SSH_KEY%" (
    echo Error: SSH key not found: %SSH_KEY%
    exit /b 1
)

echo Running deploy-simple.ps1...
echo.

powershell -ExecutionPolicy Bypass -File deploy-simple.ps1 ^
  -jarFile "%JAR_FILE%" ^
  -sshHost "%SSH_HOST%" ^
  -sshUser "%SSH_USER%" ^
  -sshPort %SSH_PORT% ^
  -sshKey "%SSH_KEY%" ^
  -remotePath "%REMOTE_PATH%"

if %errorlevel% neq 0 (
    echo Deploy failed!
    exit /b 1
)

echo.
echo Deploy complete!

