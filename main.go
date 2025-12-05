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

func main() {
	timestampFormat := "YYYYMMddHHmmss" // default
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
		}
	}

	version, err := getVersion(".", timestampFormat, metadata)
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
	fmt.Println("  --metadata=VALUE       Append build metadata with +")
	fmt.Println()
	fmt.Println(colorPrimary + "Supported tag formats:" + colorReset)
	fmt.Println("  v1.0.0          Release version")
	fmt.Println("  v1.0.0-beta1    Pre-release version")
	fmt.Println("  v1.0.0-rc.1     Release candidate")
	fmt.Println()
	fmt.Println(colorPrimary + "Output format:" + colorReset)
	timestamp := time.Now().Format("20060102150405")
	fmt.Println("  1.0.0                            On a tagged commit")
	fmt.Println("  1.0.0-beta1                      On a pre-release tag")
	fmt.Println("  1.0.0-5                          5 commits after release tag")
	fmt.Println("  1.0.0-beta1-5                    5 commits after pre-release tag")
	fmt.Printf("  1.0.0-beta1-5-%s     Uncommitted changes\n", timestamp)
	fmt.Println("  1.0.0+build.123                  With metadata")
	fmt.Printf("  1.0.0-beta1-5-%s+abc With timestamp and metadata\n", timestamp)
	fmt.Println()
}

func getVersion(dir string, timestampFormat string, metadata string) (string, error) {
	// Get version from git describe
	cmd := exec.Command("git", "describe", "--tags", "--match", "v*.*.*")
	cmd.Dir = dir
	output, err := cmd.Output()

	var gitDescribe string
	if err != nil {
		gitDescribe = "v0.0.1"
	} else {
		gitDescribe = strings.TrimSpace(string(output))
	}

	// Parse git describe output
	// Format: v0.1.0, v0.1.0-beta1, v0.1.0-5-g1a2b3c4, or v0.1.0-beta1-5-g1a2b3c4
	pattern := regexp.MustCompile(`^v(\d+)\.(\d+)\.(\d+)(-([a-zA-Z][a-zA-Z0-9.]*))?(-(\d+)-g([0-9a-f]+))?$`)
	matches := pattern.FindStringSubmatch(gitDescribe)

	var major, minor, patch, prerelease, commitCount string
	if matches != nil {
		major = matches[1]
		minor = matches[2]
		patch = matches[3]
		prerelease = matches[5]
		commitCount = matches[7]
	} else {
		major = "0"
		minor = "0"
		patch = "1"
	}

	// Build version string
	var version string
	if prerelease != "" {
		version = fmt.Sprintf("%s.%s.%s-%s", major, minor, patch, prerelease)
	} else {
		version = fmt.Sprintf("%s.%s.%s", major, minor, patch)
	}

	if commitCount != "" {
		version = fmt.Sprintf("%s-%s", version, commitCount)
	}

	// Check for uncommitted changes
	cmd = exec.Command("git", "status", "--porcelain")
	cmd.Dir = dir
	output, err = cmd.Output()
	if err == nil && len(strings.TrimSpace(string(output))) > 0 {
		goFormat := convertTimeFormat(timestampFormat)
		timestamp := time.Now().Format(goFormat)
		version = fmt.Sprintf("%s-%s", version, timestamp)
	}

	// Append metadata if provided
	if metadata != "" {
		version = fmt.Sprintf("%s+%s", version, metadata)
	}

	return version, nil
}
