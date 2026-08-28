#!/bin/sh
# Gradle wrapper bootstrap script
# Downloads and caches the Gradle distribution, then runs it

# Determine project root
APP_HOME=$( cd "${APP_HOME:-$(dirname "$0")}" && pwd -P ) || exit

# Add default JVM options
DEFAULT_JVM_OPTS="-Xmx512m -Xms64m"

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

# Download the wrapper jar if not present
if [ ! -f "$CLASSPATH" ]; then
    echo "Downloading Gradle wrapper..."
    WRAPPER_URL="https://raw.githubusercontent.com/gradle/gradle/v8.5.0/gradle/wrapper/gradle-wrapper.jar"
    if command -v curl > /dev/null 2>&1; then
        curl -sL "$WRAPPER_URL" -o "$CLASSPATH"
    elif command -v wget > /dev/null 2>&1; then
        wget -q "$WRAPPER_URL" -O "$CLASSPATH"
    else
        echo "ERROR: Could not find 'curl' or 'wget' to download Gradle wrapper." >&2
        exit 1
    fi
fi

exec java $DEFAULT_JVM_OPTS -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
