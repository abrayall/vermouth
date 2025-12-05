@echo off
setlocal enabledelayedexpansion

REM Vermouth - Semantic version detection from git tags
REM Determines the current semantic version of a git repository based on tags.

REM Default values
set "TIMESTAMP_FORMAT=yyyyMMddHHmmss"
set "METADATA="

REM Parse arguments
:parse_args
if "%~1"=="" goto :done_args
echo %~1 | findstr /b "--timestamp=" >nul
if not errorlevel 1 (
    set "TIMESTAMP_FORMAT=%~1"
    set "TIMESTAMP_FORMAT=!TIMESTAMP_FORMAT:--timestamp==!"
)
echo %~1 | findstr /b "--metadata=" >nul
if not errorlevel 1 (
    set "METADATA=%~1"
    set "METADATA=!METADATA:--metadata==!"
)
shift
goto :parse_args
:done_args

REM Convert human-readable format to .NET format
set "TIMESTAMP_FORMAT=!TIMESTAMP_FORMAT:YYYY=yyyy!"
set "TIMESTAMP_FORMAT=!TIMESTAMP_FORMAT:YY=yy!"

REM Get version from git describe
for /f "tokens=*" %%i in ('git describe --tags --match "v*.*.*" 2^>nul') do set GIT_DESCRIBE=%%i
if "%GIT_DESCRIBE%"=="" set GIT_DESCRIBE=v0.0.1

REM Use PowerShell for proper regex parsing (batch regex is too limited)
for /f "tokens=*" %%i in ('powershell -command ^
    "$desc = '%GIT_DESCRIBE%'; ^
    if ($desc -match '^v(\d+)\.(\d+)\.(\d+)(-([a-zA-Z][a-zA-Z0-9.]*))?(-(\d+)-g([0-9a-f]+))?$') { ^
        $ver = \"$($matches[1]).$($matches[2]).$($matches[3])\"; ^
        if ($matches[5]) { $ver += \"-$($matches[5])\" }; ^
        if ($matches[7]) { $ver += \"-$($matches[7])\" }; ^
        $ver ^
    } else { '0.0.1' }"') do set VERSION=%%i

REM Check for uncommitted local changes
for /f "tokens=*" %%i in ('git status --porcelain 2^>nul') do set HAS_CHANGES=1
if defined HAS_CHANGES (
    for /f "tokens=*" %%i in ('powershell -command "Get-Date -Format '%TIMESTAMP_FORMAT%'"') do set TIMESTAMP=%%i
    set VERSION=!VERSION!-!TIMESTAMP!
)

REM Append metadata if provided
if defined METADATA (
    set VERSION=!VERSION!+!METADATA!
)

echo %VERSION%
endlocal
