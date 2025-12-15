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

REM Run Java with output visible on screen AND logged to file
REM Using tee-like functionality: output goes to both console and file
java -jar ./futtoboru-desktop/target/futtoboru-desktop-0.4.0-SNAPSHOT-jar-with-dependencies.jar 2>&1 | powershell -Command "$input | Tee-Object -FilePath game-debug.log"

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
