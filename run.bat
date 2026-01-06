@echo off
echo Setting up environment...

:: Try to find JDK 20
for /d %%i in ("%ProgramFiles%\Java\jdk-20*") do set "JAVA_HOME=%%i"

:: If not found, try generic JDK
if "%JAVA_HOME%"=="" (
    for /d %%i in ("%ProgramFiles%\Java\jdk*") do set "JAVA_HOME=%%i"
)

if "%JAVA_HOME%"=="" (
    echo [WARNING] Could not auto-detect JAVA_HOME. Maven might fail if it's not set globally.
) else (
    echo Found Java: %JAVA_HOME%
)

:: Add local Maven to PATH
set "MAVEN_HOME=%~dp0apache-maven-3.9.6"
set "PATH=%MAVEN_HOME%\bin;%PATH%"

echo Running Gym App...
call mvn clean javafx:run
pause
