#!/bin/sh

# Attempt to locate gradle in PATH or run via Java wrapper
if command -v gradle >/dev/null 2>&1; then
    exec gradle "$@"
fi

# Resolve wrapper jar
DIRNAME=""
CLNP=""
case "`uname`" in
    CYGWIN*) DIRNAME=`cygpath --mixed "$0"` ;;
    *) DIRNAME="$0" ;;
esac
APP_HOME=`dirname "$DIRNAME"`
APP_HOME=`cd "$APP_HOME" >/dev/null 2>&1 && pwd`

WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

if [ -f "$WRAPPER_JAR" ]; then
    if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
        JAVACMD="$JAVA_HOME/bin/java"
    else
        JAVACMD="java"
    fi
    exec "$JAVACMD" -jar "$WRAPPER_JAR" "$@"
fi

echo "Error: Gradle wrapper jar not found and 'gradle' command not found in PATH." >&2
exit 1
