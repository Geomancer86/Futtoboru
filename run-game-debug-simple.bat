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

REM Run Java and log ALL output to file (stdout and stderr)
java -jar ./futtoboru-desktop/target/futtoboru-desktop-0.3.0-SNAPSHOT-jar-with-dependencies.jar > game-debug.log 2>&1

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

