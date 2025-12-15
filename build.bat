@echo off
REM Futtoboru Build Script
REM Builds the project using Maven

title Futtoboru Build
color 0B
cls
echo ========================================
echo Futtoboru Build Script
echo ========================================
echo.

REM Check if Maven is available
where mvn >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Maven not found in PATH!
    echo.
    echo Please either:
    echo   1. Add Maven to your PATH, OR
    echo   2. Set MAVEN_HOME environment variable, OR
    echo   3. Edit this script to use: C:\apache-maven-3.6.3\bin\mvn.cmd
    echo.
    pause
    exit /b 1
)

echo Building project with Maven...
echo This may take a minute...
echo.

REM Run Maven clean install
call mvn clean install -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ========================================
    echo BUILD FAILED!
    echo ========================================
    echo.
    echo Check the error messages above.
    echo.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo ========================================
echo BUILD SUCCESSFUL!
echo ========================================
echo.
echo JAR file location:
echo   futtoboru-desktop\target\futtoboru-desktop-0.4.0-SNAPSHOT-jar-with-dependencies.jar
echo.
echo You can now run the game using run-game-debug.bat
echo.
pause

