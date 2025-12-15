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

REM Set Maven path (default location)
set MAVEN_PATH=C:\apache-maven-3.6.3\bin\mvn.cmd

REM Check if Maven is in PATH first
where mvn >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    set MAVEN_CMD=mvn
    echo Using Maven from PATH...
    goto :build
)

REM Check if default Maven location exists
if exist "%MAVEN_PATH%" (
    set MAVEN_CMD=%MAVEN_PATH%
    echo Using Maven from: %MAVEN_PATH%
    goto :build
)

REM Check MAVEN_HOME environment variable
if defined MAVEN_HOME (
    if exist "%MAVEN_HOME%\bin\mvn.cmd" (
        set MAVEN_CMD=%MAVEN_HOME%\bin\mvn.cmd
        echo Using Maven from MAVEN_HOME: %MAVEN_CMD%
        goto :build
    )
)

REM Maven not found - show error
echo ERROR: Maven not found!
echo.
echo Searched locations:
echo   - PATH environment variable
echo   - %MAVEN_PATH%
echo   - MAVEN_HOME environment variable
echo.
echo Please either:
echo   1. Add Maven to your PATH, OR
echo   2. Set MAVEN_HOME environment variable, OR
echo   3. Edit build.bat and set MAVEN_PATH to your Maven location
echo.
pause
exit /b 1

:build
echo.
echo Building project with Maven...
echo This may take a minute...
echo.

REM Run Maven clean install
call "%MAVEN_CMD%" clean install -DskipTests

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

