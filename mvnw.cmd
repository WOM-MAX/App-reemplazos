@REM Maven Wrapper script for Windows
@echo off
set "MAVEN_PROJECTBASEDIR=%~dp0"
set "MAVEN_DIST_DIR=%USERPROFILE%\.m2\wrapper\dists"
set "MAVEN_HOME=%MAVEN_DIST_DIR%\apache-maven-3.9.6"
set "MAVEN_CMD=%MAVEN_HOME%\bin\mvn.cmd"

if exist "%MAVEN_CMD%" goto runMaven

echo Downloading Apache Maven 3.9.6...
if not exist "%MAVEN_DIST_DIR%" mkdir "%MAVEN_DIST_DIR%"
set "MAVEN_ZIP=%TEMP%\apache-maven-3.9.6-bin.zip"

powershell -NoProfile -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.6/apache-maven-3.9.6-bin.zip' -OutFile '%MAVEN_ZIP%' -UseBasicParsing"

if not exist "%MAVEN_ZIP%" (
    echo ERROR: Failed to download Maven.
    exit /b 1
)

echo Extracting Maven...
powershell -NoProfile -Command "Expand-Archive -LiteralPath '%MAVEN_ZIP%' -DestinationPath '%MAVEN_DIST_DIR%' -Force"

if not exist "%MAVEN_CMD%" (
    echo ERROR: Maven extraction failed. Trying to find extracted Maven...
    for /d %%d in ("%MAVEN_DIST_DIR%\apache-maven-*") do (
        if exist "%%d\bin\mvn.cmd" (
            set "MAVEN_HOME=%%d"
            set "MAVEN_CMD=%%d\bin\mvn.cmd"
            goto runMaven
        )
    )
    echo ERROR: Could not find Maven after extraction.
    exit /b 1
)

:runMaven
echo Using Maven: %MAVEN_CMD%
"%MAVEN_CMD%" -f "%MAVEN_PROJECTBASEDIR%pom.xml" %*
