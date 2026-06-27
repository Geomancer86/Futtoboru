@echo off
title Futtoboru Schedule Engine Tests
color 0B
echo ========================================
echo Futtoboru Schedule Engine Unit Tests
echo ========================================
echo.
echo Running automated tests for fixture generation...
echo These tests run WITHOUT starting the full game!
echo.
echo ========================================
echo.

REM Set JAVA_HOME to Java 21 (Zulu) - same as build.bat
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

REM Create temp file for test output
set "TEST_LOG=%TEMP%\futtoboru-test-output.log"

REM Run only schedule-related tests with quiet output and generate reports
echo.
echo Running all schedule engine tests...
echo (Output suppressed - see summary below)
echo.

REM Run tests on core module only, skip desktop, suppress verbose output
REM Redirect all output to log file, only show errors
call C:\apache-maven-3.6.3\bin\mvn.cmd -pl futtoboru-core -am test -Dtest=LeagueFixtureGeneratorTest,CupBracketGeneratorTest -DskipTests=false -Dmaven.test.failure.ignore=false > "%TEST_LOG%" 2>&1

REM Extract test summary from log
echo.
echo ========================================
echo TEST SUMMARY
echo ========================================
findstr /C:"Tests run:" /C:"Failures:" /C:"Errors:" /C:"BUILD SUCCESS" /C:"BUILD FAILURE" "%TEST_LOG%"
echo.

REM Check if tests passed
findstr /C:"BUILD SUCCESS" "%TEST_LOG%" >nul
if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] All tests passed!
) else (
    echo [FAILURE] Some tests failed. Check log: %TEST_LOG%
    echo.
    echo Last 20 lines of log:
    powershell -Command "Get-Content '%TEST_LOG%' | Select-Object -Last 20"
    echo.
)

REM Generate HTML test reports
echo.
echo Generating HTML test reports...
call C:\apache-maven-3.6.3\bin\mvn.cmd -pl futtoboru-core surefire-report:report -q >nul 2>&1

REM Generate code coverage report
echo Generating code coverage report...
call C:\apache-maven-3.6.3\bin\mvn.cmd -pl futtoboru-core jacoco:report -q >nul 2>&1

echo.
echo ========================================
echo Reports Generated!
echo ========================================
echo.
echo Test Reports:
echo   - HTML Test Report: futtoboru-core\target\site\surefire-report.html
echo   - Code Coverage: futtoboru-core\target\site\jacoco\index.html
echo   - Full Test Log: %TEST_LOG%
echo.

REM Check if reports exist and open them
if exist "futtoboru-core\target\site\surefire-report.html" (
    echo Opening HTML reports in browser...
    start "" "futtoboru-core\target\site\surefire-report.html"
    ping 127.0.0.1 -n 2 >nul
    if exist "futtoboru-core\target\site\jacoco\index.html" (
        start "" "futtoboru-core\target\site\jacoco\index.html"
    )
) else (
    echo WARNING: HTML reports not found. Tests may have failed.
)

echo.
pause

