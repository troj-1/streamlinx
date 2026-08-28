# Streamlinx (Alpha 0.0.1)

A high-performance, native cross-platform Desktop & Linux streaming application built with Compose Desktop and LibVLC.

---

## Quick Start (1-Line Copy & Paste)

### 🐧 Linux (Ubuntu / Linux Mint / Debian / Pop!_OS):
```bash
sudo apt update && sudo apt install -y git openjdk-17-jdk openjdk-17-jdk-headless vlc libvlc-dev libvlc5 && git clone https://github.com/troj-1/streamlinx.git && cd streamlinx && ./run_linux.sh
```

### 🍓 Raspberry Pi OS (ARM64 / 32-bit):
```bash
sudo apt update && sudo apt install -y git openjdk-17-jdk vlc libvlc-dev libvlc5 && git clone https://github.com/troj-1/streamlinx.git && cd streamlinx && ./run_linux.sh
```

---

## Features

- **Multi-Provider & Multi-Language Support**: Complete video and audio extraction across English, German, Russian, Spanish, French, Italian, Polish, and Portuguese providers, as well as standalone Anime catalogs.
- **Native LibVLC Video Engine**: Ultra-fast direct hardware-accelerated playback with auto audio-track language matching, subtitle styling/switching, and aspect ratio controls.
- **Continue Watching with 3-Dots Menu**: Instant resume from your last position, season/episode details breakdown, dynamic removal from history, and automatic cross-provider fallback.
- **Full Desktop Keyboard Navigation**: 
  - `Space`: Play / Pause toggle
  - `Left` / `Right` Arrow: Skip 10 seconds backward / forward
  - `F11`: Toggle Fullscreen
  - `ESC`: Exit player / Go back

---

## Installation & Running Options

### Option 1: Native `.deb` Installer (Ubuntu / Linux Mint / Debian)

Download **`streamlinx_0.0.1_amd64.deb`** from the [GitHub Releases Page](https://github.com/troj-1/streamlinx/releases) and install (auto-resolves all dependencies):
```bash
sudo apt install ./streamlinx_0.0.1_amd64.deb
```

---

### Option 2: Portable Linux Tarball (No building needed)

Download **`streamlinx-0.0.1-linux-x64.tar.gz`** from [Releases](https://github.com/troj-1/streamlinx/releases):
```bash
tar -xvf streamlinx-0.0.1-linux-x64.tar.gz
./streamlinx/bin/streamlinx
```

---

### Option 3: Universal Executable JAR (Raspberry Pi / Any OS)

Download **`streamlinx-0.0.1-all.jar`** from [Releases](https://github.com/troj-1/streamlinx/releases):
```bash
java -jar streamlinx-0.0.1-all.jar
```

---

### Option 4: Run directly from Source

```bash
git clone https://github.com/troj-1/streamlinx.git
cd streamlinx
./run_linux.sh
```

---

## Roadmap

- [x] Multi-Language audio stream decoder & track auto-selection
- [x] Multi-Provider aggregation (RU, DE, EN, IT, ES, FR, PL, PT, Anime)
- [x] Continue Watching with 3-dots management
- [x] Universal runnable packaging
- [ ] **Streamlinx Lite**: Ultra-lightweight headless/embedded UI mode optimized for low-power Raspberry Pi setups.
