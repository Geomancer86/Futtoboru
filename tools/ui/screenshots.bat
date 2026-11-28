@echo off
rem V-01: dev-only screenshot tour, see tools/ui/screenshots.sh and docs/BUILD.md
rem Usage: tools\ui\screenshots.bat [outDir=target\screenshots] [passes=1x,2x] [locale=en_US]
setlocal
cd /d "%~dp0\..\.."
set OUT=%~1
if "%OUT%"=="" set OUT=%CD%\target\screenshots
set PASSES=%~2
if "%PASSES%"=="" set PASSES=1x,2x
set LOC=%~3
if "%LOC%"=="" set LOC=en_US
rem mvnw.cmd by path: with NoDefaultCurrentDirectoryInExePath set, cmd does not look in the current folder.
rem %%%%classpath: `call` expands percents once more, so the plugin still gets %classpath
call "%CD%\mvnw.cmd" -B -q -pl futtoboru-core test-compile org.codehaus.mojo:exec-maven-plugin:3.1.0:exec -Dexec.classpathScope=test -Dexec.executable=java "-Dexec.args=-cp %%%%classpath com.rndmodgames.futtoboru.tools.ScreenshotTour %OUT% %PASSES% %LOC%"
