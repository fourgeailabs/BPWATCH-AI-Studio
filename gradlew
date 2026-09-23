#!/bin/bash
set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
WRAPPER_JAR="$DIR/gradle/wrapper/gradle-wrapper.jar"

# 1. If wrapper jar exists and is valid, run it
if [ -f "$WRAPPER_JAR" ]; then
    if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
        exec "$JAVA_HOME/bin/java" -jar "$WRAPPER_JAR" "$@"
    else
        exec java -jar "$WRAPPER_JAR" "$@"
    fi
fi

# 2. If 'gradle' binary in PATH is 9.x, run it
if command -v gradle >/dev/null 2>&1; then
    GRADLE_VER=$(gradle --version 2>/dev/null | grep -E "^Gradle " | head -n 1 | awk '{print $2}')
    if [[ "$GRADLE_VER" == 9* ]]; then
        exec gradle "$@"
    fi
fi

# 3. Bootstrap Gradle 9.3.1 if missing
GRADLE_CACHE_DIR="$HOME/.gradle/wrapper/dists/gradle-9.3.1-bin"
if [ ! -d "$GRADLE_CACHE_DIR" ]; then
    mkdir -p "$GRADLE_CACHE_DIR"
    echo "Downloading Gradle 9.3.1..."
    curl -sSL "https://services.gradle.org/distributions/gradle-9.3.1-bin.zip" -o "$GRADLE_CACHE_DIR/gradle-9.3.1-bin.zip"
    unzip -q -o "$GRADLE_CACHE_DIR/gradle-9.3.1-bin.zip" -d "$GRADLE_CACHE_DIR"
fi

GRADLE_BIN=$(find "$GRADLE_CACHE_DIR" -type f -name "gradle" -perm -111 2>/dev/null | head -n 1)
if [ -n "$GRADLE_BIN" ] && [ -x "$GRADLE_BIN" ]; then
    exec "$GRADLE_BIN" "$@"
fi

echo "Falling back to system gradle..."
exec gradle "$@"
