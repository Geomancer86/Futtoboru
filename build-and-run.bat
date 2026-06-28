@echo off
setlocal enabledelayedexpansion
title Futtoboru Build and Run
color 0B
cd /d "%~dp0"

echo ========================================
echo Futtoboru Build and Run
echo ========================================
echo.

call build.bat
set BUILD_EXIT_CODE=%ERRORLEVEL%

if %BUILD_EXIT_CODE% NEQ 0 (
    echo.
    echo BUILD FAILED - Cannot run game
    exit /b %BUILD_EXIT_CODE%
)

call scripts\env.bat
if not exist "%JAR_FILE%" (
    echo ERROR: JAR file not found after build.
    pause
    exit /b 1
)

echo.
echo Starting game in debug mode...
start "" "%~dp0run-game-debug.bat"
exit /b 0
