@echo off
setlocal enabledelayedexpansion

REM Vermouth - Semantic version detection from git tags
REM Determines the current semantic version of a git repository based on tags.

REM Default values
set "TIMESTAMP_FORMAT=yyyyMMddHHmmss"
set "METADATA="
set "DEFAULT_VERSION=0.0.1"
set "PATTERN=v*.*.*"
set "FORMAT={version+}"

REM Parse arguments
:parse_args
if "%~1"=="" goto :done_args
if "%~1"=="-h" goto :show_help
if "%~1"=="--help" goto :show_help
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
echo %~1 | findstr /b "--default=" >nul
if not errorlevel 1 (
    set "DEFAULT_VERSION=%~1"
    set "DEFAULT_VERSION=!DEFAULT_VERSION:--default==!"
)
echo %~1 | findstr /b "--pattern=" >nul
if not errorlevel 1 (
    set "PATTERN=%~1"
    set "PATTERN=!PATTERN:--pattern==!"
)
echo %~1 | findstr /b "--format=" >nul
if not errorlevel 1 (
    set "FORMAT=%~1"
    set "FORMAT=!FORMAT:--format==!"
)
shift
goto :parse_args

:show_help
echo vermouth - Semantic version detection from git tags
echo.
echo Usage: vermouth.bat [options]
echo.
echo Options:
echo   -h, --help             Show this help message
echo   --timestamp=FORMAT     Timestamp format (default: YYYYMMddHHmmss)
echo   --metadata=VALUE       Sets the metadata part of the version
echo   --default=VERSION      Default version if none found (default: 0.0.1)
echo   --pattern=PATTERN      Git tag pattern to match (default: v*.*.*)
echo   --format=FORMAT        Output format (default: {version+})
echo.
echo Format placeholders:
echo   {major}       Major version number
echo   {minor}       Minor version number
echo   {patch}       Patch version number
echo   {version}     {major}.{minor}.{patch}
echo   {prerelease}  Pre-release identifier (e.g., beta1)
echo   {commits}     Commits since tag
echo   {timestamp}   Timestamp for uncommitted changes
echo   {metadata}    Build metadata
echo   {version+}    Full version: {version}-{prerelease}-{commits}-{timestamp}+{metadata}
echo.
echo Examples:
echo   vermouth.bat
echo   vermouth.bat --default=1.0.0
echo   vermouth.bat --timestamp=YYYY-MM-dd --metadata=build123
echo   vermouth.bat --pattern="*.*.*"
echo   vermouth.bat --format="v{version}"
goto :eof

:done_args

REM Convert human-readable format to .NET format
set "TIMESTAMP_FORMAT=!TIMESTAMP_FORMAT:YYYY=yyyy!"
set "TIMESTAMP_FORMAT=!TIMESTAMP_FORMAT:YY=yy!"

REM Get version from git describe
for /f "tokens=*" %%i in ('git describe --tags --match "%PATTERN%" 2^>nul') do set GIT_DESCRIBE=%%i
if "%GIT_DESCRIBE%"=="" set GIT_DESCRIBE=v%DEFAULT_VERSION%

REM Use PowerShell for proper regex parsing and format processing
for /f "tokens=*" %%i in ('powershell -command ^
    "$desc = '%GIT_DESCRIBE%'; ^
    $default = '%DEFAULT_VERSION%'; ^
    $fmt = '%FORMAT%'; ^
    $meta = '%METADATA%'; ^
    $tsFmt = '%TIMESTAMP_FORMAT%'; ^
    $major = ''; $minor = ''; $patch = ''; $prerelease = ''; $commits = ''; $ts = ''; ^
    if ($desc -match '^v(\d+)\.(\d+)\.(\d+)(-([a-zA-Z][a-zA-Z0-9.]*))?(-(\d+)-g([0-9a-f]+))?$') { ^
        $major = $matches[1]; $minor = $matches[2]; $patch = $matches[3]; ^
        $prerelease = $matches[5]; $commits = $matches[7] ^
    } else { $major = $default }; ^
    $status = git status --porcelain 2^>$null; ^
    if ($status) { $ts = Get-Date -Format $tsFmt }; ^
    $output = $fmt -replace '\{version\+\}', '{version}-{prerelease}-{commits}-{timestamp}+{metadata}'; ^
    if ($minor) { $baseVer = \"$major.$minor.$patch\" } else { $baseVer = $major }; ^
    $output = $output -replace '\{major\}', $major; ^
    $output = $output -replace '\{minor\}', $minor; ^
    $output = $output -replace '\{patch\}', $patch; ^
    $output = $output -replace '\{version\}', $baseVer; ^
    $output = $output -replace '\{prerelease\}', $prerelease; ^
    $output = $output -replace '\{commits\}', $commits; ^
    $output = $output -replace '\{timestamp\}', $ts; ^
    $output = $output -replace '\{metadata\}', $meta; ^
    do { $prev = $output; $output = $output -replace '--', '-' -replace '\+\+', '+' -replace '-\+', '+' -replace '\+-', '-' } while ($output -ne $prev); ^
    $output = $output -replace '[-+]+$', ''; ^
    $output"') do set VERSION=%%i

echo %VERSION%
endlocal
