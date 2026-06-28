@echo off
title Futtoboru Dev Environment Check
cd /d "%~dp0"
call scripts\env.bat
if errorlevel 1 (
    echo.
    echo Environment check FAILED.
    pause
    exit /b 1
)

echo Repo root:  %REPO_ROOT%
echo JAVA_HOME:  %JAVA_HOME%
echo Maven:      %MVN_CMD%
echo JAR:        %JAR_FILE%
echo.

echo --- java -version ---
java -version
echo.

echo --- mvn -version ---
if /i "%MVN_CMD%"=="mvn" (
    mvn -version
) else (
    call "%MVN_CMD%" -version
)
echo.

if exist "%JAR_FILE%" (
    echo JAR status: FOUND
) else (
    echo JAR status: NOT BUILT — run build.bat
)

echo.
echo Environment check OK.
pause
