#!/bin/bash
# Streamflix Linux - Quick Install & Run
# One-liner: curl -sSL [URL] | bash

set -e

echo "🎬 Streamflix Linux - Installer"
echo "==============================="

# Install dependencies
echo "📦 Installing dependencies..."
sudo apt update
sudo apt install -y openjdk-17-jdk mpv git

# Clone if not already present
if [ ! -d "streamflix-linux" ]; then
    echo "📥 Downloading Streamflix Linux..."
    git clone [REPO_URL] streamflix-linux
fi

cd streamflix-linux

# Build and run
echo "🔨 Building..."
chmod +x scripts/build-linux.sh
bash scripts/build-linux.sh

echo ""
echo "✅ Streamflix Linux installed and running!"
echo "   To run again: cd streamflix-linux && ./gradlew :app:run"
