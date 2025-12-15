@echo off
title Futtoboru Build

REM Set JAVA_HOME to Java 21 (Zulu) - check common locations
if exist "C:\Program Files\Zulu\zulu-21" (
    set "JAVA_HOME=C:\Program Files\Zulu\zulu-21"
) else if exist "C:\Program Files\Microsoft\jdk-21.0.0" (
    set "JAVA_HOME=C:\Program Files\Microsoft\jdk-21.0.0"
) else if exist "C:\Program Files\Java\jdk-21" (
    set "JAVA_HOME=C:\Program Files\Java\jdk-21"
) else if defined JAVA_HOME (
    echo Using existing JAVA_HOME: %JAVA_HOME%
) else (
    echo WARNING: Java 21 not found in common locations!
    echo Using system Java (may cause errors if not Java 17+)
)

if defined JAVA_HOME (
    echo Using Java: %JAVA_HOME%
    set "PATH=%JAVA_HOME%\bin;%PATH%"
)

echo Generating build information...
call generate-build-info.bat
echo.

echo Building project...
echo.
call C:\apache-maven-3.6.3\bin\mvn.cmd clean install -DskipTests
echo.
echo Build complete. Press any key to close...
pause >nul
