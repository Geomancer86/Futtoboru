@echo off
setlocal enabledelayedexpansion
REM Futtoboru Build Script
REM Builds the project using Maven with Java 21 (Zulu)

title Futtoboru Build
color 0B
cls
echo ========================================
echo Futtoboru Build Script
echo ========================================
echo.

REM ========================================
REM Step 1: Find Java 21 (Zulu)
REM ========================================
echo Checking for Java 21 (Zulu)...

REM Check JAVA_HOME first
if defined JAVA_HOME (
    "%JAVA_HOME%\bin\java.exe" -version >nul 2>&1
    if %ERRORLEVEL% EQU 0 (
        echo Using Java from JAVA_HOME: %JAVA_HOME%
        goto :check_java_version
    )
)

REM Check common Zulu 21 locations
set "ZULU_PATH=C:\Program Files\Zulu\zulu-21"
if exist "%ZULU_PATH%\bin\java.exe" (
    set "JAVA_HOME=%ZULU_PATH%"
    echo Found Java at: %ZULU_PATH%
    goto :check_java_version
)

set "ZULU_PATH=C:\Program Files\Microsoft\jdk-21.0.0"
if exist "%ZULU_PATH%\bin\java.exe" (
    set "JAVA_HOME=%ZULU_PATH%"
    echo Found Java at: %ZULU_PATH%
    goto :check_java_version
)

set "ZULU_PATH=C:\Program Files\Eclipse Adoptium\jdk-21.0.0"
if exist "%ZULU_PATH%\bin\java.exe" (
    set "JAVA_HOME=%ZULU_PATH%"
    echo Found Java at: %ZULU_PATH%
    goto :check_java_version
)

set "ZULU_PATH=C:\Program Files\Java\jdk-21"
if exist "%ZULU_PATH%\bin\java.exe" (
    set "JAVA_HOME=%ZULU_PATH%"
    echo Found Java at: %ZULU_PATH%
    goto :check_java_version
)

set "ZULU_PATH=C:\Program Files (x86)\Zulu\zulu-21"
if exist "%ZULU_PATH%\bin\java.exe" (
    set "JAVA_HOME=%ZULU_PATH%"
    echo Found Java at: %ZULU_PATH%
    goto :check_java_version
)

REM Check if java is in PATH
where java >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    java -version >nul 2>&1
    if %ERRORLEVEL% EQU 0 (
        echo Using Java from PATH...
        goto :check_java_version
    )
)

echo ERROR: Java 21 not found!
echo.
echo Searched locations:
echo   - JAVA_HOME environment variable
echo   - Common Zulu/Java 21 installation paths
echo   - PATH environment variable
echo.
echo Please either:
echo   1. Set JAVA_HOME to your Java 21 installation, OR
echo   2. Add Java 21 to your PATH, OR
echo   3. Edit build.bat and add your Java path to ZULU_PATHS array
echo.
pause
exit /b 1

:check_java_version
REM Verify Java version (should be 21)
echo.
echo Checking Java version...
if defined JAVA_HOME (
    "%JAVA_HOME%\bin\java.exe" -version 2>&1 | findstr /C:"version" | findstr /C:"21" >nul
    if %ERRORLEVEL% NEQ 0 (
        echo WARNING: Java version may not be 21. Continuing anyway...
    ) else (
        echo Java 21 detected!
    )
) else (
    java -version 2>&1 | findstr /C:"version" | findstr /C:"21" >nul
    if %ERRORLEVEL% NEQ 0 (
        echo WARNING: Java version may not be 21. Continuing anyway...
    ) else (
        echo Java 21 detected!
    )
)

REM ========================================
REM Step 2: Find Maven
REM ========================================
echo.
echo Checking for Maven...

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
echo ========================================
echo Building project with Maven...
echo Using Java: %JAVA_HOME%
echo Using Maven: %MAVEN_CMD%
echo ========================================
echo This may take a minute...
echo.

REM Verify Java version before building
if defined JAVA_HOME (
    echo Verifying Java version...
    "%JAVA_HOME%\bin\java.exe" -version
    echo.
) else (
    echo Verifying Java version from PATH...
    java -version
    echo.
)

REM Set JAVA_HOME for Maven (ensure it's set)
if not defined JAVA_HOME (
    echo ERROR: JAVA_HOME is not set! Cannot build.
    pause
    exit /b 1
)

REM Set JAVA_HOME in current session for Maven to use
REM Put Java bin at the START of PATH so Maven uses it first
set "PATH=%JAVA_HOME%\bin;%PATH%"

REM Verify Maven sees the correct Java
echo Verifying Maven is using correct Java...
"%MAVEN_CMD%" -version
echo.

REM Check what Java version Maven is actually using
for /f "tokens=*" %%i in ('"%MAVEN_CMD%" -version 2^>^&1 ^| findstr /C:"Java version"') do (
    echo Maven Java Info: %%i
    echo %%i | findstr /C:"21" >nul
    if %ERRORLEVEL% NEQ 0 (
        echo %%i | findstr /C:"17" >nul
        if %ERRORLEVEL% NEQ 0 (
            echo.
            echo WARNING: Maven may not be using Java 17 or 21!
            echo This could cause compilation errors.
            echo.
        )
    )
)
echo.

REM Run Maven clean install with Java 21
echo Starting Maven build...
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

