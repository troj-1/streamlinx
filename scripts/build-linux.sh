#!/bin/bash
# Streamflix Linux - Build & Install Script
# Run this on your Linux machine (MacBook or WSL2)

set -e

echo "🎬 Streamflix Linux - Build Script"
echo "=================================="

# Check for Java 17+
if ! command -v java &> /dev/null; then
    echo "📦 Installing Java 17..."
    sudo apt update
    sudo apt install -y openjdk-17-jdk
fi

JAVA_VER=$(java -version 2>&1 | head -n1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VER" -lt 17 ]; then
    echo "📦 Java 17+ required, installing..."
    sudo apt install -y openjdk-17-jdk
    export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
fi

echo "✅ Java: $(java -version 2>&1 | head -n1)"

# Check for mpv
if ! command -v mpv &> /dev/null; then
    echo "📦 Installing mpv..."
    sudo apt install -y mpv
fi
echo "✅ mpv: $(mpv --version | head -n1)"

# Navigate to project
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_DIR"

# Generate Gradle wrapper if not present
if [ ! -f "gradlew" ]; then
    echo "📦 Setting up Gradle wrapper..."
    if command -v gradle &> /dev/null; then
        gradle wrapper --gradle-version 8.5
    else
        echo "📦 Installing Gradle..."
        sudo apt install -y gradle
        gradle wrapper --gradle-version 8.5
    fi
fi

# Build
echo ""
echo "🔨 Building Streamflix Desktop..."
chmod +x gradlew
./gradlew :app:run

echo ""
echo "✅ Build complete!"
