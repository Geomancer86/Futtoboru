@echo off
title Futtoboru Build
cd /d "%~dp0"
call scripts\env.bat
if errorlevel 1 (
    pause
    exit /b 1
)

echo Using Java: %JAVA_HOME%
echo Using Maven: %MVN_CMD%
echo.

echo Generating build information...
call generate-build-info.bat
if errorlevel 1 (
    echo ERROR: generate-build-info.bat failed.
    pause
    exit /b 1
)
echo.

echo Building project...
echo.
if /i "%MVN_CMD%"=="mvn" (
    mvn clean install -DskipTests
) else (
    call "%MVN_CMD%" clean install -DskipTests
)
set BUILD_EXIT=%ERRORLEVEL%
echo.

if %BUILD_EXIT% NEQ 0 (
    echo BUILD FAILED with exit code %BUILD_EXIT%.
    pause
    exit /b %BUILD_EXIT%
)

if not exist "%JAR_FILE%" (
    echo ERROR: Build reported success but JAR not found:
    echo   %JAR_FILE%
    pause
    exit /b 1
)

echo Build complete.
echo JAR: %JAR_FILE%
exit /b 0
