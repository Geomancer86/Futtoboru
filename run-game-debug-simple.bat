@echo off
title Futtoboru Debug Console
color 0A
cd /d "%~dp0"
call scripts\env.bat
if errorlevel 1 (
    pause
    exit /b 1
)

if not exist "%JAR_FILE%" (
    echo ERROR: Game JAR not found.
    echo   %JAR_FILE%
    echo Run build.bat first.
    pause
    exit /b 1
)

echo Logging to: %REPO_ROOT%\game-debug.log
java -jar "%JAR_FILE%" > "%REPO_ROOT%\game-debug.log" 2>&1
set RUN_EXIT=%ERRORLEVEL%

echo Game exited with code %RUN_EXIT%.
type "%REPO_ROOT%\game-debug.log"
pause
exit /b %RUN_EXIT%
