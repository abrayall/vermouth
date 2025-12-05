# Vermouth

A CLI tool for detecting semantic versions from git tags.

## Installation

### Quick Install

**macOS/Linux:**
```bash
curl -sfL https://raw.githubusercontent.com/abrayall/vermouth/refs/heads/main/install.sh | sh -
```

**Windows (PowerShell):**
```powershell
irm https://raw.githubusercontent.com/abrayall/vermouth/refs/heads/main/install.ps1 | iex
```

### Build from Source

```bash
git clone https://github.com/abrayall/vermouth.git
cd vermouth
./install.sh
```

## Usage

Simply run `vermouth` in any git repository to get the current semantic version:

```bash
vermouth
```

### Quick Run (No Installation)

Run vermouth directly without installing:

```bash
curl -sfL https://raw.githubusercontent.com/abrayall/vermouth/refs/heads/main/vermouth.sh | sh -
```

This downloads and executes the script in a single command - no root access or file storage required.

### Options

```
-h, --help             Show help message
-v, --version          Show vermouth version
--timestamp=FORMAT     Timestamp format (default: YYYYMMddHHmmss)
--metadata=VALUE       Append build metadata with +
--default=VERSION      Default version if none found (default: 0.0.1)
--pattern=PATTERN      Git tag pattern to match (default: v*.*.*)
```

### Examples

```bash
# Get current version
vermouth
# Output: 1.2.3

# With uncommitted changes (adds timestamp)
vermouth
# Output: 1.2.3-20251205143022

# Custom timestamp format
vermouth --timestamp=YY-MM-ddTHH:mm:ss
# Output: 1.2.3-25-12-05T14:30:22

# Add build metadata
vermouth --metadata=build.123
# Output: 1.2.3+build.123

# Combine timestamp and metadata
vermouth --metadata=ci
# Output: 1.2.3-20251205143022+ci

# Custom default version (when no tags exist)
vermouth --default=1.0.0
# Output: 1.0.0-20251205143022

# Use "dev" as default
vermouth --default=dev
# Output: dev-20251205143022

# Match tags without 'v' prefix
vermouth --pattern="*.*.*"
# Output: 1.2.3

# Match release tags only (e.g., release/1.0.0)
vermouth --pattern="release/*"
# Output: 1.0.0
```

## Version Detection

Vermouth reads git tags to determine the current semantic version. Tags must follow the format `v<MAJOR>.<MINOR>.<PATCH>` with optional pre-release suffix.

### Supported Tag Formats

| Tag | Description |
|-----|-------------|
| `v1.0.0` | Release version |
| `v1.0.0-beta1` | Pre-release version |
| `v1.0.0-alpha` | Alpha release |
| `v1.0.0-rc.1` | Release candidate |

### Output Format

| Scenario | Output |
|----------|--------|
| On a tagged commit | `1.0.0` |
| On a pre-release tag | `1.0.0-beta1` |
| 5 commits after release tag | `1.0.0-5` |
| 5 commits after pre-release tag | `1.0.0-beta1-5` |
| With uncommitted changes | `1.0.0-beta1-5-20251205143022` |
| With metadata | `1.0.0+build.123` |
| With timestamp and metadata | `1.0.0-beta1-5-20251205143022+abc` |

## Timestamp Format

The `--timestamp` option accepts a human-readable format string:

| Token | Description | Example |
|-------|-------------|---------|
| `YYYY` | 4-digit year | 2025 |
| `YY` | 2-digit year | 25 |
| `MM` | Month (01-12) | 12 |
| `dd` | Day (01-31) | 05 |
| `HH` | Hour (00-23) | 14 |
| `mm` | Minute (00-59) | 30 |
| `ss` | Second (00-59) | 22 |

### Timestamp Examples

```bash
# Default format
vermouth --timestamp=YYYYMMddHHmmss
# Output: 1.0.0-20251205143022

# ISO-like format
vermouth --timestamp=YYYY-MM-ddTHH:mm:ss
# Output: 1.0.0-2025-12-05T14:30:22

# Date only
vermouth --timestamp=YYYY-MM-dd
# Output: 1.0.0-2025-12-05

# Short format
vermouth --timestamp=YYMMdd
# Output: 1.0.0-251205
```

## Build Metadata

The `--metadata` option appends build metadata after a `+` sign. Per the [semver spec](https://semver.org/), build metadata is ignored when determining version precedence.

```bash
# CI build number
vermouth --metadata=build.$(BUILD_NUMBER)

# Git commit SHA
vermouth --metadata=$(git rev-parse --short HEAD)

# Combined
vermouth --metadata=ci.build.123.sha.abc1234
```

## Shell Scripts

Vermouth also includes shell scripts for environments where the Go binary isn't available:

- `vermouth.sh` - Bash script (macOS/Linux)
- `vermouth.ps1` - PowerShell script (Windows)
- `vermouth.bat` - Batch script (Windows)

These scripts provide the same version detection and support the `--timestamp` and `--metadata` options.

## Use Cases

### Build Scripts

```bash
#!/bin/bash
VERSION=$(vermouth)
go build -ldflags "-X main.Version=${VERSION}" -o myapp .
```

### Build Scripts (With Fallback)

Try the installed binary first, fallback to curl if not installed:

```bash
#!/bin/bash
VERSION=$(vermouth || curl -sfL https://raw.githubusercontent.com/abrayall/vermouth/refs/heads/main/vermouth.sh | sh -)
go build -ldflags "-X main.Version=${VERSION}" -o myapp .
```

### CI/CD Pipelines

```yaml
# GitHub Actions
- name: Get version
  id: version
  run: echo "version=$(vermouth --metadata=${{ github.run_number }})" >> $GITHUB_OUTPUT

- name: Build
  run: docker build -t myapp:${{ steps.version.outputs.version }} .
```

### CI/CD Pipelines (With Fallback)

```yaml
# GitHub Actions - try binary, fallback to curl
- name: Get version
  id: version
  run: |
    VERSION=$(vermouth || curl -sfL https://raw.githubusercontent.com/abrayall/vermouth/refs/heads/main/vermouth.sh | sh -)
    echo "version=$VERSION" >> $GITHUB_OUTPUT

- name: Build
  run: docker build -t myapp:${{ steps.version.outputs.version }} .
```

### Package Publishing

```bash
VERSION=$(vermouth)
npm version $VERSION --no-git-tag-version
npm publish
```

### Docker Build with Version

```bash
VERSION=$(vermouth || curl -sfL https://raw.githubusercontent.com/abrayall/vermouth/refs/heads/main/vermouth.sh | sh -)
docker build --build-arg VERSION=$VERSION -t myapp:$VERSION .
```

### Makefile Integration

```makefile
VERSION := $(shell vermouth || curl -sfL https://raw.githubusercontent.com/abrayall/vermouth/refs/heads/main/vermouth.sh | sh -)

build:
	go build -ldflags "-X main.Version=$(VERSION)" -o myapp .

release:
	@echo "Building version $(VERSION)"
```

### Windows Batch (With Fallback)

```batch
for /f "tokens=*" %%i in ('vermouth 2^>nul') do set "VERSION=%%i"
if not defined VERSION (
    curl -sfL https://raw.githubusercontent.com/abrayall/vermouth/refs/heads/main/vermouth.bat -o %TEMP%\vermouth.bat
    for /f "tokens=*" %%i in ('%TEMP%\vermouth.bat') do set "VERSION=%%i"
)
echo %VERSION%
```

### PowerShell (With Fallback)

```powershell
$VERSION = $(vermouth 2>$null)
if (-not $VERSION) {
    $VERSION = Invoke-Expression (Invoke-WebRequest -Uri "https://raw.githubusercontent.com/abrayall/vermouth/refs/heads/main/vermouth.ps1" -UseBasicParsing).Content
}
```

Or as a one-liner (PowerShell 7+):

```powershell
$VERSION = $(vermouth 2>$null) ?? (iex (iwr -Uri "https://raw.githubusercontent.com/abrayall/vermouth/refs/heads/main/vermouth.ps1" -UseBasicParsing).Content)
```

## Future Work

- **Version management** - Add commands to increment versions (`vermouth bump major|minor|patch`) and create git tags

## License

MIT
