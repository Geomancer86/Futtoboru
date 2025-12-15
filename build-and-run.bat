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
call build.bat

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ========================================
    echo BUILD FAILED - Cannot run game
    echo ========================================
    echo.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo ========================================
echo Step 2: Starting game in debug mode...
echo ========================================
echo.

REM Run the game
call run-game-debug.bat

