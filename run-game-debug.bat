@echo off
title Futtoboru Debug Console
color 0A
echo ========================================
echo Futtoboru Game Launcher (Debug Mode)
echo ========================================
echo.
echo Starting game with error logging...
echo All output will be saved to: game-debug.log
echo.
echo ========================================
echo.

REM dev tools (Match Engine Debug on the main menu) only on request: set FUTTOBORU_DEV=true first
set DEV_FLAG=
if /I "%FUTTOBORU_DEV%"=="true" set DEV_FLAG=-Dfuttoboru.dev=true

REM Run Java with output visible on screen AND logged to file
REM Using PowerShell Tee-Object to show on screen and save to file
powershell -NoProfile -Command "java %DEV_FLAG% -jar ./futtoboru-desktop/target/futtoboru-desktop-0.5.0-SNAPSHOT-jar-with-dependencies.jar 2>&1 | Tee-Object -FilePath game-debug.log"

echo.
echo ========================================
echo Game has exited. Exit code: %ERRORLEVEL%
echo ========================================
echo.
echo Check game-debug.log for all output and errors
echo.
echo Press any key to view the log file...
pause >nul
type game-debug.log
echo.
echo Press any key to close...
pause >nul
