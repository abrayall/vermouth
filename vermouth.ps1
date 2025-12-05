# Vermouth - Semantic version detection from git tags
# Determines the current semantic version of a git repository based on tags.

param(
    [string]$timestamp = "yyyyMMddHHmmss",
    [string]$metadata = "",
    [string]$default = "0.0.1",
    [string]$pattern = "v*.*.*",
    [string]$format = "{version+}",
    [switch]$help
)

# Help function
function Show-Help {
    Write-Host "vermouth - Semantic version detection from git tags"
    Write-Host ""
    Write-Host "Usage: vermouth.ps1 [options]"
    Write-Host ""
    Write-Host "Options:"
    Write-Host "  -help                  Show this help message"
    Write-Host "  -timestamp FORMAT      Timestamp format (default: YYYYMMddHHmmss)"
    Write-Host "  -metadata VALUE        Sets the metadata part of the version"
    Write-Host "  -default VERSION       Default version if none found (default: 0.0.1)"
    Write-Host "  -pattern PATTERN       Git tag pattern to match (default: v*.*.*)"
    Write-Host "  -format FORMAT         Output format (default: {version+})"
    Write-Host ""
    Write-Host "Format placeholders:"
    Write-Host "  {major}       Major version number"
    Write-Host "  {minor}       Minor version number"
    Write-Host "  {patch}       Patch version number"
    Write-Host "  {version}     {major}.{minor}.{patch}"
    Write-Host "  {prerelease}  Pre-release identifier (e.g., beta1)"
    Write-Host "  {commits}     Commits since tag"
    Write-Host "  {timestamp}   Timestamp for uncommitted changes"
    Write-Host "  {metadata}    Build metadata"
    Write-Host "  {version+}    Full version: {version}-{prerelease}-{commits}-{timestamp}+{metadata}"
    Write-Host ""
    Write-Host "Examples:"
    Write-Host "  .\vermouth.ps1"
    Write-Host "  .\vermouth.ps1 -default 1.0.0"
    Write-Host "  .\vermouth.ps1 -timestamp YYYY-MM-dd -metadata build123"
    Write-Host "  .\vermouth.ps1 -pattern `"*.*.*`""
    Write-Host "  .\vermouth.ps1 -format `"v{version}`""
}

if ($help) {
    Show-Help
    exit 0
}

# Convert human-readable format to .NET format
$timestampFormat = $timestamp `
    -replace "YYYY", "yyyy" `
    -replace "YY", "yy" `
    -replace "MM", "MM" `
    -replace "dd", "dd" `
    -replace "HH", "HH" `
    -replace "mm", "mm" `
    -replace "ss", "ss"

$ErrorActionPreference = "SilentlyContinue"

# Get version from git describe
$gitDescribe = git describe --tags --match $pattern 2>$null
if (-not $gitDescribe) {
    $gitDescribe = "v$default"
}

# Parse git describe output
# Format: v0.1.0, v0.1.0-beta1, v0.1.0-5-g1a2b3c4, or v0.1.0-beta1-5-g1a2b3c4
$versionPattern = "^v(\d+)\.(\d+)\.(\d+)(-([a-zA-Z][a-zA-Z0-9.]*))?(-(\d+)-g([0-9a-f]+))?$"

# Initialize version components
$major = ""
$minor = ""
$patch = ""
$prerelease = ""
$commits = ""
$ts = ""

if ($gitDescribe -match $versionPattern) {
    $major = $matches[1]
    $minor = $matches[2]
    $patch = $matches[3]
    $prerelease = $matches[5]
    $commits = $matches[7]
} else {
    # Non-standard default, use as-is
    $major = $default
}

# Check for uncommitted local changes
$ErrorActionPreference = "SilentlyContinue"
$status = git status --porcelain 2>$null
if ($status) {
    $ts = Get-Date -Format $timestampFormat
}

# Apply format
# Expand {version+} shortcut
$output = $format -replace '\{version\+\}', '{version}-{prerelease}-{commits}-{timestamp}+{metadata}'

# Build {version} = major.minor.patch
if ($minor) {
    $baseVersion = "$major.$minor.$patch"
} else {
    $baseVersion = $major
}

# Replace all placeholders
$output = $output -replace '\{major\}', $major
$output = $output -replace '\{minor\}', $minor
$output = $output -replace '\{patch\}', $patch
$output = $output -replace '\{version\}', $baseVersion
$output = $output -replace '\{prerelease\}', $prerelease
$output = $output -replace '\{commits\}', $commits
$output = $output -replace '\{timestamp\}', $ts
$output = $output -replace '\{metadata\}', $metadata

# Clean up empty segments - remove separators around empty values
do {
    $prev = $output
    $output = $output -replace '--', '-'
    $output = $output -replace '\+\+', '+'
    $output = $output -replace '-\+', '+'
    $output = $output -replace '\+-', '-'
} while ($output -ne $prev)

# Remove trailing separators
$output = $output -replace '[-+]+$', ''

Write-Output $output
