@echo off
REM Futtoboru Build and Run Script
REM Builds the project and then runs it in debug mode

title Futtoboru Build and Run
color 0B
cls
echo ========================================
echo Futtoboru Build and Run
echo ========================================
echo.

REM First, build the project
echo Step 1: Building project...
echo.
call build.bat
set BUILD_EXIT_CODE=%ERRORLEVEL%

echo.
echo Build script exited with code: %BUILD_EXIT_CODE%

if %BUILD_EXIT_CODE% NEQ 0 (
    echo.
    echo ========================================
    echo BUILD FAILED - Cannot run game
    echo ========================================
    echo.
    echo Please check the build errors above.
    echo.
    pause
    exit /b %BUILD_EXIT_CODE%
)

REM Verify JAR file exists
if not exist ".\futtoboru-desktop\target\futtoboru-desktop-0.4.0-SNAPSHOT-jar-with-dependencies.jar" (
    echo.
    echo ========================================
    echo ERROR: JAR file not found after build!
    echo ========================================
    echo.
    echo Expected location:
    echo   futtoboru-desktop\target\futtoboru-desktop-0.4.0-SNAPSHOT-jar-with-dependencies.jar
    echo.
    echo The build may have failed silently.
    echo Please check build output above.
    echo.
    pause
    exit /b 1
)

echo.
echo JAR file verified: futtoboru-desktop\target\futtoboru-desktop-0.4.0-SNAPSHOT-jar-with-dependencies.jar
echo.

echo.
echo ========================================
echo Step 2: Starting game in debug mode...
echo ========================================
echo.

REM Run the game
call run-game-debug.bat

