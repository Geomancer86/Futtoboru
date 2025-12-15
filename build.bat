@echo off
title Futtoboru Build
echo Building project...
echo.
call C:\apache-maven-3.6.3\bin\mvn.cmd clean install -DskipTests
echo.
echo Build complete. Press any key to close...
pause >nul
