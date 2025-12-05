# Vermouth - Semantic version detection from git tags
# Determines the current semantic version of a git repository based on tags.

$ErrorActionPreference = "SilentlyContinue"

# Get version from git describe
$gitDescribe = git describe --tags --match "v*.*.*" 2>$null
if (-not $gitDescribe) {
    $gitDescribe = "v0.0.1"
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
    $version = "0.0.1"
}

# Check for uncommitted local changes
$ErrorActionPreference = "SilentlyContinue"
$status = git status --porcelain 2>$null
if ($status) {
    $timestamp = Get-Date -Format "yyyyMMddHHmmss"
    $version = "$version-$timestamp"
}

Write-Output $version
