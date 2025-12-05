#!/bin/sh

# Vermouth - Semantic version detection from git tags
# Determines the current semantic version of a git repository based on tags.

set -e

# Default values
TIMESTAMP_FORMAT="%Y%m%d%H%M%S"
METADATA=""
DEFAULT_VERSION="0.0.1"
PATTERN="v*.*.*"

# Help function
show_help() {
    echo "vermouth - Semantic version detection from git tags"
    echo ""
    echo "Usage: vermouth.sh [options]"
    echo ""
    echo "Options:"
    echo "  -h, --help             Show this help message"
    echo "  --timestamp=FORMAT     Timestamp format (default: YYYYMMddHHmmss)"
    echo "  --metadata=VALUE       Append build metadata with +"
    echo "  --default=VERSION      Default version if none found (default: 0.0.1)"
    echo "  --pattern=PATTERN      Git tag pattern to match (default: v*.*.*)"
    echo ""
    echo "Examples:"
    echo "  vermouth.sh"
    echo "  vermouth.sh --default=1.0.0"
    echo "  vermouth.sh --timestamp=YYYY-MM-dd --metadata=build123"
    echo "  vermouth.sh --pattern=\"*.*.*\""
}

# Parse arguments
for arg in "$@"; do
    case "$arg" in
        -h|--help)
            show_help
            exit 0
            ;;
        --timestamp=*)
            TIMESTAMP_FORMAT="${arg#--timestamp=}"
            # Convert human-readable format to date format
            TIMESTAMP_FORMAT=$(echo "$TIMESTAMP_FORMAT" | sed \
                -e 's/YYYY/%Y/g' \
                -e 's/YY/%y/g' \
                -e 's/MM/%m/g' \
                -e 's/dd/%d/g' \
                -e 's/HH/%H/g' \
                -e 's/mm/%M/g' \
                -e 's/ss/%S/g')
            ;;
        --metadata=*)
            METADATA="${arg#--metadata=}"
            ;;
        --default=*)
            DEFAULT_VERSION="${arg#--default=}"
            ;;
        --pattern=*)
            PATTERN="${arg#--pattern=}"
            ;;
    esac
done

# Get version from git describe
GIT_DESCRIBE=$(git describe --tags --match "$PATTERN" 2>/dev/null || echo "v${DEFAULT_VERSION}")

# Remove 'v' prefix
GIT_DESCRIBE="${GIT_DESCRIBE#v}"

# Default version
VERSION="$DEFAULT_VERSION"

# Parse git describe output
# Format: 0.1.0, 0.1.0-beta1, 0.1.0-5-g1a2b3c4, or 0.1.0-beta1-5-g1a2b3c4
case "$GIT_DESCRIBE" in
    # Match X.Y.Z or X.Y.Z-something
    [0-9]*.[0-9]*.[0-9]*)
        # Extract base version (X.Y.Z)
        BASE=$(echo "$GIT_DESCRIBE" | sed 's/^\([0-9]*\.[0-9]*\.[0-9]*\).*/\1/')
        REST="${GIT_DESCRIBE#$BASE}"
        REST="${REST#-}"

        VERSION="$BASE"

        if [ -n "$REST" ]; then
            # Check if REST starts with a letter (prerelease) or number (commit count)
            case "$REST" in
                [a-zA-Z]*)
                    # Has prerelease: beta1, alpha, rc.1, etc.
                    # Could be: beta1 or beta1-5-gabc123
                    case "$REST" in
                        *-[0-9]*-g[0-9a-f]*)
                            # prerelease-commits-hash: extract prerelease and commit count
                            PRERELEASE=$(echo "$REST" | sed 's/\(.*\)-[0-9]*-g[0-9a-f]*$/\1/')
                            COMMIT_COUNT=$(echo "$REST" | sed 's/.*-\([0-9]*\)-g[0-9a-f]*$/\1/')
                            VERSION="${VERSION}-${PRERELEASE}-${COMMIT_COUNT}"
                            ;;
                        *)
                            # Just prerelease, no commits after
                            VERSION="${VERSION}-${REST}"
                            ;;
                    esac
                    ;;
                [0-9]*-g[0-9a-f]*)
                    # No prerelease, just commits-hash: 5-gabc123
                    COMMIT_COUNT=$(echo "$REST" | sed 's/\([0-9]*\)-g[0-9a-f]*$/\1/')
                    VERSION="${VERSION}-${COMMIT_COUNT}"
                    ;;
            esac
        fi
        ;;
esac

# Check for uncommitted local changes
if [ -n "$(git status --porcelain 2>/dev/null)" ]; then
    TIMESTAMP=$(date +"$TIMESTAMP_FORMAT")
    VERSION="${VERSION}-${TIMESTAMP}"
fi

# Append metadata if provided
if [ -n "$METADATA" ]; then
    VERSION="${VERSION}+${METADATA}"
fi

echo "$VERSION"
