# Vermouth - Semantic version detection from git tags
# Determines the current semantic version of a git repository based on tags.

param(
    [string]$timestamp = "yyyyMMddHHmmss",
    [string]$metadata = "",
    [string]$default = "0.0.1",
    [string]$pattern = "v*.*.*",
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
    Write-Host "  -metadata VALUE        Append build metadata with +"
    Write-Host "  -default VERSION       Default version if none found (default: 0.0.1)"
    Write-Host "  -pattern PATTERN       Git tag pattern to match (default: v*.*.*)"
    Write-Host ""
    Write-Host "Examples:"
    Write-Host "  .\vermouth.ps1"
    Write-Host "  .\vermouth.ps1 -default 1.0.0"
    Write-Host "  .\vermouth.ps1 -timestamp YYYY-MM-dd -metadata build123"
    Write-Host "  .\vermouth.ps1 -pattern `"*.*.*`""
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
$pattern = "^v(\d+)\.(\d+)\.(\d+)(-([a-zA-Z][a-zA-Z0-9.]*))?(-(\d+)-g([0-9a-f]+))?$"

if ($gitDescribe -match $pattern) {
    $major = $matches[1]
    $minor = $matches[2]
    $patch = $matches[3]
    $prerelease = $matches[5]
    $commitCount = $matches[7]

    # Build version string
    if ($prerelease) {
        $version = "$major.$minor.$patch-$prerelease"
    } else {
        $version = "$major.$minor.$patch"
    }

    if ($commitCount) {
        $version = "$version-$commitCount"
    }
} else {
    $version = $default
}

# Check for uncommitted local changes
$ErrorActionPreference = "SilentlyContinue"
$status = git status --porcelain 2>$null
if ($status) {
    $ts = Get-Date -Format $timestampFormat
    $version = "$version-$ts"
}

# Append metadata if provided
if ($metadata) {
    $version = "$version+$metadata"
}

Write-Output $version
