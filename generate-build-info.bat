@echo off
REM Generate build.properties file with build information

setlocal enabledelayedexpansion

echo Generating build.properties...

REM Get git commit hash (short)
for /f "delims=" %%i in ('git rev-parse --short HEAD 2^>nul') do set GIT_COMMIT=%%i
if "%GIT_COMMIT%"=="" set GIT_COMMIT=unknown

REM Get git commit count (build number)
for /f "delims=" %%i in ('git rev-list --count HEAD 2^>nul') do set BUILD_NUMBER=%%i
if "%BUILD_NUMBER%"=="" set BUILD_NUMBER=0

REM Get current timestamp
for /f "tokens=2 delims==" %%a in ('wmic os get localdatetime /value') do set "dt=%%a"
set BUILD_TIMESTAMP=%dt:~0,4%-%dt:~4,2%-%dt:~6,2% %dt:~8,2%:%dt:~10,2%:%dt:~12,2%

REM Version from pom.xml (hardcoded for now, could parse it)
set VERSION=0.4.0-SNAPSHOT

REM Create build.properties file
(
echo version=%VERSION%
echo build.number=%BUILD_NUMBER%
echo git.commit.id.abbrev=%GIT_COMMIT%
echo build.timestamp=%BUILD_TIMESTAMP%
) > futtoboru-core\src\main\resources\build.properties

echo Build info generated:
echo   Version: %VERSION%
echo   Build Number: %BUILD_NUMBER%
echo   Commit: %GIT_COMMIT%
echo   Timestamp: %BUILD_TIMESTAMP%
echo.
echo File created: futtoboru-core\src\main\resources\build.properties

endlocal

