@echo off
setlocal enabledelayedexpansion

title Futtoboru Build
color 0B
cls

echo ========================================
echo Futtoboru Build Script
echo ========================================
echo.

REM Try to find Java - use whatever is in PATH or JAVA_HOME
if defined JAVA_HOME (
    set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
) else (
    set "JAVA_CMD=java"
)

REM Try to find Maven - use whatever is in PATH or default location
set "MAVEN_CMD=mvn"
if not exist "C:\apache-maven-3.6.3\bin\mvn.cmd" (
    where mvn >nul 2>&1
    if %ERRORLEVEL% NEQ 0 (
        set "MAVEN_CMD=C:\apache-maven-3.6.3\bin\mvn.cmd"
    )
)

echo Checking Java...
%JAVA_CMD% -version 2>&1 | findstr /C:"version"
if %ERRORLEVEL% NEQ 0 (
    echo WARNING: Could not verify Java version
)
echo.

echo Checking Maven...
%MAVEN_CMD% -version 2>&1 | findstr /C:"Apache Maven"
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Maven not found!
    echo.
    echo Please ensure Maven is in PATH or at C:\apache-maven-3.6.3
    echo.
    goto :error
)
echo.

echo ========================================
echo Starting Maven build...
echo ========================================
echo.

REM Run Maven build
call %MAVEN_CMD% clean install -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ========================================
    echo BUILD FAILED!
    echo ========================================
    echo Exit code: %ERRORLEVEL%
    goto :error
)

echo.
echo ========================================
echo BUILD SUCCESSFUL!
echo ========================================
echo.

REM Verify JAR
if exist ".\futtoboru-desktop\target\futtoboru-desktop-0.4.0-SNAPSHOT-jar-with-dependencies.jar" (
    echo JAR file created successfully!
) else (
    echo WARNING: JAR file not found!
)

echo.
goto :end

:error
echo.
echo Build failed. Check errors above.
echo.

:end
echo Press any key to close...
pause >nul
exit /b %ERRORLEVEL%
