#!/usr/bin/env bash
set -e

echo "=================================================="
echo "          Installing Streamlinx for Linux         "
echo "=================================================="

# 1. Install prerequisites if apt is available
if command -v apt-get >/dev/null 2>&1; then
    echo "[1/4] Checking and installing system dependencies (VLC, LibVLC, OpenJDK 17)..."
    if [ "$EUID" -ne 0 ] && command -v sudo >/dev/null 2>&1; then
        sudo apt-get update -qq
        sudo apt-get install -y -qq vlc libvlc-dev libvlc5 openjdk-17-jdk git curl
    elif [ "$EUID" -eq 0 ]; then
        apt-get update -qq
        apt-get install -y -qq vlc libvlc-dev libvlc5 openjdk-17-jdk git curl
    fi
fi

# Set Java Home if standard location exists
if [ -d "/usr/lib/jvm/java-17-openjdk-amd64" ]; then
    export JAVA_HOME="/usr/lib/jvm/java-17-openjdk-amd64"
    export PATH="$JAVA_HOME/bin:$PATH"
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" 2>/dev/null && pwd || echo "$PWD")"
REPO_DIR="$SCRIPT_DIR"

# If run directly via curl pipe, clone repo to ~/.streamlinx-source
if [ ! -f "$REPO_DIR/gradlew" ]; then
    echo "[2/4] Downloading Streamlinx repository..."
    REPO_DIR="$HOME/.streamlinx-source"
    rm -rf "$REPO_DIR"
    git clone --depth 1 https://github.com/troj-1/streamlinx.git "$REPO_DIR"
fi

cd "$REPO_DIR"
chmod +x ./gradlew

echo "[3/4] Building native Linux application..."
./gradlew :app:packageDistributionForCurrentOS --no-daemon

# 4. Install binary to user directory (~/.local/opt/streamlinx)
INSTALL_DIR="$HOME/.local/opt/streamlinx"
BIN_DIR="$HOME/.local/bin"
APP_DIR="$HOME/.local/share/applications"
ICON_DIR="$HOME/.local/share/icons"

mkdir -p "$INSTALL_DIR" "$BIN_DIR" "$APP_DIR" "$ICON_DIR" "$HOME/Desktop"

rm -rf "$INSTALL_DIR"
cp -r app/build/compose/binaries/main/app/streamlinx/* "$INSTALL_DIR/"

# Copy icon and symlink binary
cp "$INSTALL_DIR/lib/streamlinx.png" "$ICON_DIR/streamlinx.png" 2>/dev/null || cp app/src/main/resources/icons/icon.png "$ICON_DIR/streamlinx.png" 2>/dev/null || true
ln -sf "$INSTALL_DIR/bin/streamlinx" "$BIN_DIR/streamlinx"

# Create Desktop Entry for Application Menu
cat << 'EOF' > "$APP_DIR/streamlinx.desktop"
[Desktop Entry]
Name=Streamlinx
GenericName=Movie & TV Show Streamer
Comment=Watch movies, anime and TV series with multi-language providers
Exec=streamlinx %U
Icon=streamlinx
Terminal=false
Type=Application
Categories=AudioVideo;Video;Player;TV;
Keywords=streaming;movies;tv;series;anime;video;streamflix;streamlinx;
StartupNotify=true
StartupWMClass=com-streamflixreborn-streamflix-MainKt
EOF

# Update Exec path with full path in user desktop entry
sed -i "s|Exec=streamlinx|Exec=$INSTALL_DIR/bin/streamlinx|g" "$APP_DIR/streamlinx.desktop"
sed -i "s|Icon=streamlinx|Icon=$ICON_DIR/streamlinx.png|g" "$APP_DIR/streamlinx.desktop"
chmod +x "$APP_DIR/streamlinx.desktop"

# Create Desktop Shortcut
cp "$APP_DIR/streamlinx.desktop" "$HOME/Desktop/Streamlinx.desktop"
chmod +x "$HOME/Desktop/Streamlinx.desktop"
command -v gio >/dev/null 2>&1 && gio set "$HOME/Desktop/Streamlinx.desktop" metadata::trusted true 2>/dev/null || true

# Refresh desktop database
command -v update-desktop-database >/dev/null 2>&1 && update-desktop-database "$APP_DIR" 2>/dev/null || true

echo ""
echo "=================================================="
echo "    Streamlinx Successfully Installed! ??         "
echo "=================================================="
echo "You can now launch Streamlinx anytime by:"
echo " 1. Clicking 'Streamlinx' on your Desktop"
echo " 2. Searching 'Streamlinx' in your Start / App Menu"
echo " 3. Running 'streamlinx' in any terminal"
echo "=================================================="
