@echo off

REM Resolve repo root (parent of scripts\).
for %%I in ("%~dp0..") do set "REPO_ROOT=%%~fI"
cd /d "%REPO_ROOT%" || (
    echo ERROR: Cannot change directory to repo root: %REPO_ROOT%
    exit /b 1
)

set "JAR_FILE=%REPO_ROOT%\futtoboru-desktop\target\futtoboru-desktop-0.4.0-SNAPSHOT-jar-with-dependencies.jar"
set "MVN_CMD="

REM --- Java 17+ (project requires Java 17 per pom.xml) ---
if not defined JAVA_HOME (
    if exist "C:\Program Files\Microsoft\jdk-17.0.19.10-hotspot" (
        set "JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.19.10-hotspot"
    ) else (
        for /d %%J in ("C:\Program Files\Microsoft\jdk-17*") do (
            if not defined JAVA_HOME set "JAVA_HOME=%%~fJ"
        )
    )
)
if not defined JAVA_HOME if exist "C:\Program Files\Zulu\zulu-21" (
    set "JAVA_HOME=C:\Program Files\Zulu\zulu-21"
)
if not defined JAVA_HOME if exist "C:\Program Files\Microsoft\jdk-21.0.0" (
    set "JAVA_HOME=C:\Program Files\Microsoft\jdk-21.0.0"
)
if not defined JAVA_HOME if exist "C:\Program Files\Java\jdk-21" (
    set "JAVA_HOME=C:\Program Files\Java\jdk-21"
)
if not defined JAVA_HOME (
    for /d %%J in ("C:\Program Files\Eclipse Adoptium\jdk-17*") do (
        if not defined JAVA_HOME set "JAVA_HOME=%%~fJ"
    )
)

if defined JAVA_HOME (
    set "PATH=%JAVA_HOME%\bin;%PATH%"
)

where java >nul 2>&1
if errorlevel 1 (
    echo ERROR: java not found. Install JDK 17+ and add it to PATH, or set JAVA_HOME.
    exit /b 1
)

REM --- Maven ---
where mvn >nul 2>&1
if not errorlevel 1 (
    set "MVN_CMD=mvn"
    exit /b 0
)

if defined MAVEN_HOME if exist "%MAVEN_HOME%\bin\mvn.cmd" (
    set "MVN_CMD=%MAVEN_HOME%\bin\mvn.cmd"
    exit /b 0
)

if exist "C:\Program Files\Apache\Maven\apache-maven-3.9.16\bin\mvn.cmd" (
    set "MVN_CMD=C:\Program Files\Apache\Maven\apache-maven-3.9.16\bin\mvn.cmd"
    exit /b 0
)

if exist "C:\apache-maven-3.6.3\bin\mvn.cmd" (
    set "MVN_CMD=C:\apache-maven-3.6.3\bin\mvn.cmd"
    exit /b 0
)

echo ERROR: Maven not found.
echo Install Maven 3.6+ and ensure mvn is on PATH, or run scripts\install-maven-windows.ps1 as Administrator.
echo See scripts\README.md
exit /b 1
