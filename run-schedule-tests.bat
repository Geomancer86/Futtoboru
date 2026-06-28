@echo off
title Futtoboru Schedule Engine Tests
color 0B
cd /d "%~dp0"
call scripts\env.bat
if errorlevel 1 (
    pause
    exit /b 1
)

set "TEST_LOG=%TEMP%\futtoboru-test-output.log"

echo ========================================
echo Futtoboru Schedule Engine Unit Tests
echo ========================================
echo.
echo Using Maven: %MVN_CMD%
echo.

if /i "%MVN_CMD%"=="mvn" (
    mvn -pl futtoboru-core -am test -Dtest=LeagueFixtureGeneratorTest,CupBracketGeneratorTest -DskipTests=false -Dmaven.test.failure.ignore=false > "%TEST_LOG%" 2>&1
) else (
    call "%MVN_CMD%" -pl futtoboru-core -am test -Dtest=LeagueFixtureGeneratorTest,CupBracketGeneratorTest -DskipTests=false -Dmaven.test.failure.ignore=false > "%TEST_LOG%" 2>&1
)

echo ========================================
echo TEST SUMMARY
echo ========================================
findstr /C:"Tests run:" /C:"Failures:" /C:"Errors:" /C:"BUILD SUCCESS" /C:"BUILD FAILURE" "%TEST_LOG%"
echo.

findstr /C:"BUILD SUCCESS" "%TEST_LOG%" >nul
if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] All tests passed!
) else (
    echo [FAILURE] Some tests failed. Check log: %TEST_LOG%
    powershell -Command "Get-Content '%TEST_LOG%' | Select-Object -Last 20"
)

echo.
if /i "%MVN_CMD%"=="mvn" (
    mvn -pl futtoboru-core surefire-report:report -q >nul 2>&1
    mvn -pl futtoboru-core jacoco:report -q >nul 2>&1
) else (
    call "%MVN_CMD%" -pl futtoboru-core surefire-report:report -q >nul 2>&1
    call "%MVN_CMD%" -pl futtoboru-core jacoco:report -q >nul 2>&1
)

echo Reports:
echo   futtoboru-core\target\site\surefire-report.html
echo   futtoboru-core\target\site\jacoco\index.html
echo.

if exist "futtoboru-core\target\site\surefire-report.html" (
    start "" "futtoboru-core\target\site\surefire-report.html"
)

pause
