# Streamlinx (Alpha 0.0.1)

A high-performance, native cross-platform Desktop & Linux streaming application built with Compose Desktop and LibVLC.

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

## Installation & Running

### Option 1: Universal Executable JAR (Easiest — Works on all Linux, Mac, Windows, Raspberry Pi)

1. Download **`streamlinx-0.0.1-all.jar`** from the [GitHub Releases Page](https://github.com/troj-1/streamlinx/releases).
2. Ensure Java 17+ and VLC are installed on your Linux system:
   ```bash
   # Debian / Ubuntu / Linux Mint / Pop!_OS:
   sudo apt update && sudo apt install -y openjdk-17-jdk vlc libvlc-dev

   # Fedora / RHEL:
   sudo dnf install -y java-17-openjdk-devel vlc vlc-devel

   # Arch Linux / Manjaro:
   sudo pacman -S jdk17-openjdk vlc

   # Raspberry Pi OS (ARM64 / 32-bit):
   sudo apt update && sudo apt install -y openjdk-17-jdk vlc libvlc-dev
   ```
3. Run Streamlinx:
   ```bash
   java -jar streamlinx-0.0.1-all.jar
   ```

---

### Option 2: Run directly from Source (Linux / macOS / Windows)

```bash
git clone https://github.com/troj-1/streamlinx.git
cd streamlinx
chmod +x gradlew run_linux.sh
./run_linux.sh
```

---

### Option 3: Package Standalone Linux App / .deb / .rpm

On your Linux machine, you can generate a native desktop package:
```bash
./gradlew :app:packageDistributionForCurrentOS :app:packageDeb
```
*The packaged `.deb` installer and standalone app directory will be created under `app/build/compose/binaries/main/`.*

---

## Roadmap

- [x] Multi-Language audio stream decoder & track auto-selection
- [x] Multi-Provider aggregation (RU, DE, EN, IT, ES, FR, PL, PT, Anime)
- [x] Continue Watching with 3-dots management
- [x] Universal runnable packaging
- [ ] **Streamlinx Lite**: Ultra-lightweight headless/embedded UI mode optimized for low-power Raspberry Pi setups.
