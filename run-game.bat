@echo off
echo ========================================
echo Futtoboru Game Launcher
echo ========================================
echo.
echo Starting game...
echo.
echo NOTE: This window will stay open to show any errors
echo Press Ctrl+C to close this window after the game exits
echo.
echo ========================================
echo.

java -jar ./futtoboru-desktop/target/futtoboru-desktop-0.4.0-SNAPSHOT-jar-with-dependencies.jar

echo.
echo ========================================
echo Game has exited. Exit code: %ERRORLEVEL%
echo ========================================
echo.
echo Press any key to close this window...
pause >nul

