@echo off
REM Force console window to stay visible
if not "%1"=="console" (
    start "Futtoboru Debug Console" cmd /k "%~f0" console
    exit
)

title Futtoboru Debug Console
color 0A
cls
echo ========================================
echo Futtoboru Game Launcher (Debug Mode)
echo ========================================
echo.
echo Starting game with error logging...
echo All errors will be displayed in this window
echo AND saved to: game-debug.log
echo.
echo ========================================
echo.
echo NOTE: Keep this window open to see debug output!
echo The game window will open separately.
echo.
echo ========================================
echo.

REM Run Java with all output visible AND logged to file (Windows compatible)
REM Using PowerShell to tee output to both console and file
powershell -Command "java -jar ./futtoboru-desktop/target/futtoboru-desktop-0.3.0-SNAPSHOT-jar-with-dependencies.jar 2>&1 | Tee-Object -FilePath game-debug.log"

echo.
echo ========================================
echo Game has exited. Exit code: %ERRORLEVEL%
echo ========================================
echo.
echo Log file saved to: game-debug.log
echo.
echo Press any key to view the log file...
pause >nul
type game-debug.log
echo.
echo Press any key to close this window...
pause >nul
