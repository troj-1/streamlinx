#!/usr/bin/env bash
set -e

echo "=== Starting Streamlinx Alpha 0.0.1 ==="

# Auto-detect and install VLC / Java dependencies on Debian / Ubuntu / Linux Mint
if ! dpkg -s vlc libvlc-dev >/dev/null 2>&1 && command -v apt-get >/dev/null 2>&1; then
    echo "Installing required media dependencies (VLC & LibVLC)..."
    sudo apt-get update && sudo apt-get install -y vlc libvlc-dev libvlc5 openjdk-17-jdk
fi

export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH

chmod +x ./gradlew
./gradlew :app:run
