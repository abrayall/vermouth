package main

import (
	"fmt"
	"os"
	"os/exec"
	"regexp"
	"strings"
	"time"
)

var Version = "0.1.0"

// ANSI color codes for #4CAF50 (bright green)
const (
	colorPrimary = "\033[38;2;76;175;80m"
	colorWhite   = "\033[1;37m"
	colorMuted   = "\033[38;2;136;136;136m"
	colorReset   = "\033[0m"
)

// VersionInfo holds all parsed version components
type VersionInfo struct {
	Major      string
	Minor      string
	Patch      string
	Prerelease string
	Commits    string
	Timestamp  string
	Metadata   string
}

func main() {
	timestampFormat := "YYYYMMddHHmmss" // default
	defaultVersion := "0.0.1"           // default
	pattern := "v*.*.*"                 // default
	format := "{version+}"              // default
	var metadata string

	for _, arg := range os.Args[1:] {
		switch {
		case arg == "--version" || arg == "-v":
			fmt.Println(Version)
			return
		case arg == "--help" || arg == "-h":
			printHelp()
			return
		case strings.HasPrefix(arg, "--timestamp="):
			timestampFormat = strings.TrimPrefix(arg, "--timestamp=")
		case strings.HasPrefix(arg, "--metadata="):
			metadata = strings.TrimPrefix(arg, "--metadata=")
		case strings.HasPrefix(arg, "--default="):
			defaultVersion = strings.TrimPrefix(arg, "--default=")
		case strings.HasPrefix(arg, "--pattern="):
			pattern = strings.TrimPrefix(arg, "--pattern=")
		case strings.HasPrefix(arg, "--format="):
			format = strings.TrimPrefix(arg, "--format=")
		}
	}

	version, err := getVersion(".", timestampFormat, metadata, defaultVersion, pattern, format)
	if err != nil {
		fmt.Fprintf(os.Stderr, "Error: %v\n", err)
		os.Exit(1)
	}

	fmt.Println(version)
}

// convertTimeFormat converts a human-readable time format to Go's time format
func convertTimeFormat(format string) string {
	replacer := strings.NewReplacer(
		"YYYY", "2006",
		"YY", "06",
		"MM", "01",
		"dd", "02",
		"HH", "15",
		"mm", "04",
		"ss", "05",
	)
	return replacer.Replace(format)
}

// formatVersion applies the format string to the version info
func formatVersion(info VersionInfo, format string) string {
	// First expand {version+} to the full format
	format = strings.ReplaceAll(format, "{version+}", "{version}-{prerelease}-{commits}-{timestamp}+{metadata}")

	// Build {version} = major.minor.patch
	baseVersion := fmt.Sprintf("%s.%s.%s", info.Major, info.Minor, info.Patch)

	// Replace all placeholders
	result := format
	result = strings.ReplaceAll(result, "{major}", info.Major)
	result = strings.ReplaceAll(result, "{minor}", info.Minor)
	result = strings.ReplaceAll(result, "{patch}", info.Patch)
	result = strings.ReplaceAll(result, "{version}", baseVersion)
	result = strings.ReplaceAll(result, "{prerelease}", info.Prerelease)
	result = strings.ReplaceAll(result, "{commits}", info.Commits)
	result = strings.ReplaceAll(result, "{timestamp}", info.Timestamp)
	result = strings.ReplaceAll(result, "{metadata}", info.Metadata)

	// Clean up empty segments - remove separators around empty values
	// Handle patterns like "1.0.0--" or "1.0.0-+" or "--5" etc.
	for {
		prev := result
		result = strings.ReplaceAll(result, "--", "-")
		result = strings.ReplaceAll(result, "++", "+")
		result = strings.ReplaceAll(result, "-+", "+")
		result = strings.ReplaceAll(result, "+-", "-")
		if result == prev {
			break
		}
	}

	// Remove trailing separators
	result = strings.TrimRight(result, "-+")

	return result
}

func printHelp() {
	banner := `
 █  █ █▀▀ █▀▀█ █▀▄▀█ █▀▀█ █  █ ▀▀█▀▀ █  █
 █  █ █▀▀ █▄▄▀ █ ▀ █ █  █ █  █   █   █▀▀█
  ▀▀  ▀▀▀ ▀ ▀▀ ▀   ▀ ▀▀▀▀  ▀▀▀   ▀   ▀  ▀`

	divider := "───────────────────────────────────────────"

	fmt.Println()
	fmt.Println(colorMuted + divider + colorReset)
	fmt.Println(colorPrimary + banner + colorReset)
	fmt.Println(colorWhite + " v" + Version + colorReset)
	fmt.Println()
	fmt.Println(colorMuted + divider + colorReset)
	fmt.Println()
	fmt.Println("Semantic version detection from git tags")
	fmt.Println()
	fmt.Println(colorPrimary + "Usage:" + colorReset + " vermouth [options]")
	fmt.Println()
	fmt.Println(colorPrimary + "Options:" + colorReset)
	fmt.Println("  -h, --help             Show this help message")
	fmt.Println("  -v, --version          Show vermouth version")
	fmt.Println("  --timestamp=FORMAT     Timestamp format (default: YYYYMMddHHmmss)")
	fmt.Println("  --metadata=VALUE       Sets the metadata part of the version")
	fmt.Println("  --default=VERSION      Default version if none found (default: 0.0.1)")
	fmt.Println("  --pattern=PATTERN      Git tag pattern to match (default: v*.*.*)")
	fmt.Println("  --format=FORMAT        Output format (default: {version+})")
	fmt.Println()
	fmt.Println(colorPrimary + "Format placeholders:" + colorReset)
	fmt.Println("  {major}       Major version number")
	fmt.Println("  {minor}       Minor version number")
	fmt.Println("  {patch}       Patch version number")
	fmt.Println("  {version}     {major}.{minor}.{patch}")
	fmt.Println("  {prerelease}  Pre-release identifier (e.g., beta1)")
	fmt.Println("  {commits}     Commits since tag")
	fmt.Println("  {timestamp}   Timestamp for uncommitted changes")
	fmt.Println("  {metadata}    Build metadata")
	fmt.Println("  {version+}    Full version: {version}-{prerelease}-{commits}-{timestamp}+{metadata}")
	fmt.Println()
	fmt.Println(colorPrimary + "Format examples:" + colorReset)
	fmt.Println("  {version+}              1.2.3-beta1-5-20251205143022+build")
	fmt.Println("  v{version+}             v1.2.3-beta1-5-20251205143022+build")
	fmt.Println("  v{version}              v1.2.3")
	fmt.Println("  {major}.{minor}         1.2")
	fmt.Println("  {version}-SNAPSHOT      1.2.3-SNAPSHOT")
	fmt.Println()
}

func getVersion(dir string, timestampFormat string, metadata string, defaultVersion string, pattern string, format string) (string, error) {
	// Get version from git describe
	cmd := exec.Command("git", "describe", "--tags", "--match", pattern)
	cmd.Dir = dir
	output, err := cmd.Output()

	var gitDescribe string
	if err != nil {
		gitDescribe = "v" + defaultVersion
	} else {
		gitDescribe = strings.TrimSpace(string(output))
	}

	// Parse git describe output
	// Format: v0.1.0, v0.1.0-beta1, v0.1.0-5-g1a2b3c4, or v0.1.0-beta1-5-g1a2b3c4
	versionRegex := regexp.MustCompile(`^v(\d+)\.(\d+)\.(\d+)(-([a-zA-Z][a-zA-Z0-9.]*))?(-(\d+)-g([0-9a-f]+))?$`)
	matches := versionRegex.FindStringSubmatch(gitDescribe)

	info := VersionInfo{
		Metadata: metadata,
	}

	if matches != nil {
		info.Major = matches[1]
		info.Minor = matches[2]
		info.Patch = matches[3]
		info.Prerelease = matches[5]
		info.Commits = matches[7]
	} else {
		// Parse default version if it looks like X.Y.Z
		defaultRegex := regexp.MustCompile(`^(\d+)\.(\d+)\.(\d+)$`)
		defaultMatches := defaultRegex.FindStringSubmatch(defaultVersion)
		if defaultMatches != nil {
			info.Major = defaultMatches[1]
			info.Minor = defaultMatches[2]
			info.Patch = defaultMatches[3]
		} else {
			// Non-standard default, just use it as-is
			info.Major = defaultVersion
			info.Minor = ""
			info.Patch = ""
		}
	}

	// Check for uncommitted changes
	cmd = exec.Command("git", "status", "--porcelain")
	cmd.Dir = dir
	output, err = cmd.Output()
	if err == nil && len(strings.TrimSpace(string(output))) > 0 {
		goFormat := convertTimeFormat(timestampFormat)
		info.Timestamp = time.Now().Format(goFormat)
	}

	return formatVersion(info, format), nil
}
