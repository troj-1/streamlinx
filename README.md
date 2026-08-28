# Streamlinx (Alpha 0.0.1)

A high-performance, native cross-platform Desktop & Linux streaming application built with Compose Desktop and LibVLC.

## Features

- **Multi-Provider & Multi-Language Support**: Streaming extraction across English, German, Russian, Spanish, French, Italian, Polish, and Portuguese providers, plus Anime sources.
- **Native LibVLC Player**: Ultra-fast direct video rendering with automatic audio track matching, subtitle track switching, and aspect ratio controls.
- **Continue Watching**: Persistent watch history with resume position, quick overflow options (Resume, Details, Remove from List), and automatic cross-provider fallback.
- **Full Desktop UI**: Dark theme, keyboard navigation (Space to toggle play/pause, Left/Right arrows to skip, F11 for fullscreen, ESC to back out).

## Linux Installation (Ubuntu / Debian / Linux Mint / Fedora / Arch)

### Prerequisites
Make sure you have Java 17+ and VLC installed on your system:

`ash
# Ubuntu / Debian / Pop!_OS / Linux Mint:
sudo apt update
sudo apt install -y openjdk-17-jdk vlc libvlc-dev

# Fedora / RHEL:
sudo dnf install -y java-17-openjdk-devel vlc vlc-devel

# Arch Linux / Manjaro:
sudo pacman -S jdk17-openjdk vlc
`

### Running Streamlinx

`ash
git clone <repo-url> streamlinx
cd streamlinx
chmod +x gradlew run_linux.sh
./gradlew :app:run
`

### Building Standalone Distribution (.deb / standalone app)

`ash
./gradlew :app:packageDistributionForCurrentOS
`
The packaged distribution will be generated in pp/build/compose/binaries/main/.
