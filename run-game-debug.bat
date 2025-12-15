@echo off
title Futtoboru Debug Console
color 0A
cls
echo ========================================
echo Futtoboru Game Launcher (Debug Mode)
echo ========================================
echo.

REM Check if JAR exists, if not, prompt to build
if not exist ".\futtoboru-desktop\target\futtoboru-desktop-0.4.0-SNAPSHOT-jar-with-dependencies.jar" (
    echo ERROR: JAR file not found!
    echo.
    echo The game needs to be built first.
    echo.
    echo Would you like to build now? (Y/N)
    set /p BUILD_NOW=
    if /i "%BUILD_NOW%"=="Y" (
        echo.
        echo Building project...
        call build.bat
        if %ERRORLEVEL% NEQ 0 (
            echo.
            echo Build failed! Cannot run game.
            pause
            exit /b 1
        )
        echo.
        echo Build complete! Starting game...
        echo.
    ) else (
        echo.
        echo Please run build.bat first, or use build-and-run.bat
        pause
        exit /b 1
    )
)

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

REM Run Java and log output to file (same as run-game-debug-simple.bat)
REM Output will be in game-debug.log file
java -jar ./futtoboru-desktop/target/futtoboru-desktop-0.4.0-SNAPSHOT-jar-with-dependencies.jar > game-debug.log 2>&1

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
