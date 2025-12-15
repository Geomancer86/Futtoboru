@echo off
title Futtoboru Build
echo Building project...
echo.
call mvn clean install -DskipTests
echo.
echo Build complete. Press any key to close...
pause >nul
