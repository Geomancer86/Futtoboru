@echo off
echo ========================================
echo Futtoboru Game Launcher (Debug Mode)
echo ========================================
echo.
echo Starting game with error logging...
echo All errors will be displayed in this window
echo.
echo ========================================
echo.

java -jar ./futtoboru-desktop/target/futtoboru-desktop-0.3.0-SNAPSHOT-jar-with-dependencies.jar

echo.
echo ========================================
echo Game has exited. Exit code: %ERRORLEVEL%
echo ========================================
echo.
echo Press any key to close this window...
pause >nul

