#!/bin/bash

# Vermouth - Semantic version detection from git tags
# Determines the current semantic version of a git repository based on tags.

set -e

# Get version from git describe
GIT_DESCRIBE=$(git describe --tags --match "v*.*.*" 2>/dev/null || echo "v0.0.1")

# Parse git describe output
# Format: v0.1.0, v0.1.0-beta1, v0.1.0-5-g1a2b3c4, or v0.1.0-beta1-5-g1a2b3c4
if [[ "$GIT_DESCRIBE" =~ ^v([0-9]+)\.([0-9]+)\.([0-9]+)(-([a-zA-Z][a-zA-Z0-9.]*))?(-([0-9]+)-g([0-9a-f]+))?$ ]]; then
    MAJOR="${BASH_REMATCH[1]}"
    MINOR="${BASH_REMATCH[2]}"
    PATCH="${BASH_REMATCH[3]}"
    PRERELEASE="${BASH_REMATCH[5]}"
    COMMIT_COUNT="${BASH_REMATCH[7]}"

    # Build version string
    if [[ -n "$PRERELEASE" ]]; then
        VERSION="${MAJOR}.${MINOR}.${PATCH}-${PRERELEASE}"
    else
        VERSION="${MAJOR}.${MINOR}.${PATCH}"
    fi

    if [[ -n "$COMMIT_COUNT" ]]; then
        VERSION="${VERSION}-${COMMIT_COUNT}"
    fi
else
    # Fallback
    VERSION="0.0.1"
fi

# Check for uncommitted local changes
if [[ -n $(git status --porcelain 2>/dev/null) ]]; then
    TIMESTAMP=$(date +"%Y%m%d%H%M%S")
    VERSION="${VERSION}-${TIMESTAMP}"
fi

echo "$VERSION"
