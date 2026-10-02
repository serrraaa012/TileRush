# ⚡ TileRush

> A high-energy, neon-themed Android rhythm game built from scratch in pure Java — featuring dynamic speed scaling, responsive hold tiles, and pulse-pounding zigzag transitions.

---

## 🎮 About The Project

**TileRush** is a fast-paced 4-lane mobile rhythm game inspired by arcade classics like *Magic Tiles* and *Piano Tiles*. 

Instead of relying on heavy third-party game engines (like Unity or Unreal), TileRush is engineered **100% natively using Android's 2D Canvas and pure Java**. It delivers locked 60+ FPS performance, zero-latency touch inputs, fluid bubble pop physics, and an electrifying neon aesthetic.

---

## ✨ Key Features

- **🎧 Interactive Music Jukebox with Live Previews:**
  Preview songs in real time before jumping into a round. Featured tracks:
  - *On The Dance Floor*
  - *Midnight Kisses*
  - *Faded*
  - *Chanel*
  - *Don't Blame Me*
  - *At My Worst*

- **⚡ 3-Tier Dynamic Difficulty Progression:**
  - **Level 1 (Groove / Normal | Score 0–29):** Balanced tempo to build rhythm and confidence.
  - **Level 2 (Rush / Speed Up | Score 30–69):** Triggered with an animated screen flash and a glowing `"⚡ SPEED UP! ⚡"` announcement card with accelerated tempo.
  - **Level 3 (Overdrive / Hard | Score 70+):** Unleashed with a fiery amber screen flash and `"🔥 OVERDRIVE! 🔥"` banner at blistering speeds.

- **🌀 Zigzag Tile Storms:**
  In Overdrive mode, rapid 7-note staircase waves sweep across adjacent lanes (`0 ➔ 1 ➔ 2 ➔ 3 ➔ 2 ➔ 1 ➔ 0`), testing reflexes and precision.

- **🎵 Dynamic Hold Tiles with Real-Time Finger Tracking:**
  Hold tiles feature continuous finger position tracking (`touchY`). The glowing head note block stays pinned directly beneath your contact point while the incoming tail flows smoothly into it with plasma ripples and bubble bursts.

- **🫧 Satisfying Visual Feedback:**
  - Glassmorphic neon-bordered tiles with bevel lighting.
  - Interactive bubble pops and particle explosion effects on tap.
  - Ambient floating background dust motes.
  - Real-time HUD badges displaying your current difficulty mode and personal high score.

---

## 🛠️ Tech Stack & Architecture

| Component | Technology |
| :--- | :--- |
| **Language** | Pure Java (JDK 17) |
| **Platform** | Native Android SDK (Min SDK: API 26 / Android 8.0 Oreo) |
| **Rendering** | Custom Android 2D `Canvas` & `SurfaceView` double-buffered game loop (60 FPS) |
| **Audio Engine** | Dual-channel: `MediaPlayer` (soundtrack & previews) + `SoundPool` (zero-latency SFX) |
| **Touch Handling** | Multi-touch event dispatcher (`ACTION_DOWN`, `ACTION_MOVE`, `ACTION_UP`) |
| **Build System** | Gradle with Android Gradle Plugin (AGP 8.x) |

---

## 📂 Project Structure

```text
TileRush/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/tilerush/
│   │   │   │   ├── MainActivity.java    # Jukebox song selection, audio preview lifecycle & UI
│   │   │   │   ├── GameActivity.java    # Host activity managing full-screen gameplay window
│   │   │   │   ├── GameView.java        # 60 FPS Canvas loop, particle physics, touch tracking & level state machine
│   │   │   │   ├── Tile.java            # Tile model, lane data, hold-state tracking & tap animations
│   │   │   │   └── SoundManager.java    # SoundPool audio manager for low-latency tap SFX
│   │   │   └── res/                     # Layouts, audio files (raw/), and neon drawable vector assets
│   └── build.gradle                     # Module build configuration
└── build.gradle                         # Root project configuration
```

---

## 🚀 Getting Started

### Prerequisites
- [Android Studio](https://developer.android.com/studio) (Koala / Ladybug / Meerkat or newer recommended)
- JDK 17+
- Android Device or Emulator running **Android 8.0 (API 26)** or higher

### Installation & Run

1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-username/TileRush.git
   cd TileRush
   ```

2. **Open in Android Studio:**
   - Launch Android Studio.
   - Select **Open an Existing Project** and navigate to the cloned `TileRush` directory.
   - Let Gradle sync dependencies.

3. **Build the APK:**
   ```bash
   ./gradlew assembleDebug
   ```

4. **Run on Device / Emulator:**
   - Connect your Android device via USB (with USB Debugging enabled) or start an AVD emulator.
   - Press the **Run** button (`Shift + F10`) in Android Studio.

---

## 🎯 Gameplay Controls

- **Single Tap Tile:** Tap the falling neon tile before it reaches the bottom boundary.
- **Hold Tile:** Press and hold down on the tile's head note block. Keep your finger held as the note tail drains into your finger, then release when finished.
- **Miss Penalty:** Tapping an empty lane or letting a tile drop off the bottom triggers Game Over.

---

## 🔮 Roadmap / Future Improvements

- [ ] Custom Beatmap Generator: Automatically generate playable rhythm tiles from user-imported MP3 files.
- [ ] 1v1 Real-Time Multiplayer Battle Mode.
- [ ] Global Online Leaderboards with Firebase.
- [ ] Custom haptic vibration feedback on note hits.

---

## 📄 License

This project is licensed under the [MIT License](LICENSE) — free to use, modify, and distribute.
