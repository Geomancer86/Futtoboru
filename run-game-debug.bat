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
    echo.
    echo Run build.bat first, or build-and-run.bat
    pause
    exit /b 1
)

echo ========================================
echo Futtoboru Game Launcher (Debug Mode)
echo ========================================
echo.
echo Log file: %REPO_ROOT%\game-debug.log
echo.
echo ========================================
echo.

powershell -NoProfile -Command "java -jar '%JAR_FILE%' 2>&1 | Tee-Object -FilePath '%REPO_ROOT%\game-debug.log'"
set RUN_EXIT=%ERRORLEVEL%

echo.
echo Game exited with code %RUN_EXIT%.
echo Check game-debug.log for full output.
echo.
pause
type "%REPO_ROOT%\game-debug.log"
echo.
pause
exit /b %RUN_EXIT%
