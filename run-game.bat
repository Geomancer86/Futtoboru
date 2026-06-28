@echo off
title Futtoboru Game Launcher
cd /d "%~dp0"
call scripts\env.bat
if errorlevel 1 (
    pause
    exit /b 1
)

if not exist "%JAR_FILE%" (
    echo ERROR: Game JAR not found.
    echo   %JAR_FILE%
    echo.
    echo Run build.bat first, or build-and-run.bat
    pause
    exit /b 1
)

echo ========================================
echo Futtoboru Game Launcher
echo ========================================
echo.
echo Starting game...
echo Press Ctrl+C after the game exits
echo.
echo ========================================
echo.

java -jar "%JAR_FILE%"
set RUN_EXIT=%ERRORLEVEL%

echo.
echo Game exited with code %RUN_EXIT%.
pause
exit /b %RUN_EXIT%
