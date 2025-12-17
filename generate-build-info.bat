@echo off
REM Generate build.properties file with build information
REM Build number increments on EVERY build, not just commits

setlocal enabledelayedexpansion

echo Generating build.properties...

REM Get git commit hash (short)
for /f "delims=" %%i in ('git rev-parse --short HEAD 2^>nul') do set GIT_COMMIT=%%i
if "%GIT_COMMIT%"=="" set GIT_COMMIT=unknown

REM Read current build number from file (if it exists) and increment it
set BUILD_NUMBER=0
set CURRENT_BUILD=0

REM Check if build.properties exists and read the current build number
if exist "futtoboru-core\src\main\resources\build.properties" (
    REM Extract build number from existing file using findstr
    for /f "tokens=2 delims==" %%a in ('findstr /R /C:"^build.number=" "futtoboru-core\src\main\resources\build.properties"') do (
        set CURRENT_BUILD=%%a
        REM Remove any trailing spaces
        set CURRENT_BUILD=!CURRENT_BUILD: =!
    )
    
    REM Check if we successfully read a number
    if "!CURRENT_BUILD!"=="" (
        set CURRENT_BUILD=0
    )
    
    REM Increment build number (handles both 0 and existing numbers)
    if !CURRENT_BUILD! LSS 1 (
        set BUILD_NUMBER=1
        echo No valid build number found, starting at: !BUILD_NUMBER!
    ) else (
        set /a BUILD_NUMBER=!CURRENT_BUILD! + 1
        echo Found existing build number: !CURRENT_BUILD!, incrementing to: !BUILD_NUMBER!
    )
) else (
    REM No build.properties file exists, start at 1
    set BUILD_NUMBER=1
    echo No existing build.properties file, starting build number at: !BUILD_NUMBER!
)

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

