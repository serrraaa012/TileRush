# ⚡ TileRush

> **A high-speed, neon-infused 4-lane rhythm mobile game built natively in pure Java for Android.**

![Platform](https://img.shields.io/badge/Platform-Android_8.0+_(API_26+)-3DDC84?logo=android&logoColor=white)
![Language](https://img.shields.io/badge/Language-Pure_Java-ED8B00?logo=openjdk&logoColor=white)
![Build](https://img.shields.io/badge/Build-Gradle_8.13-02303A?logo=gradle&logoColor=white)
![Engine](https://img.shields.io/badge/Engine-Zero_Engine_(Native_Canvas)-FF46C8)
![License](https://img.shields.io/badge/License-MIT-00E1FF)

---

## 🎮 Overview

**TileRush** is an arcade rhythm mobile game engineered from the ground up without heavy third-party game engines (no Unity, Unreal, or Godot). Built entirely in **pure Java** leveraging Android's native **Canvas API**, **ObjectAnimator**, and hardware-accelerated dual audio pipelines, TileRush delivers responsive 60 FPS gameplay, tactile audio feedback, and a striking cyberpunk aesthetic.

---

## ✨ Features

* 🎹 **Dynamic Rhythm Mechanics**:
  * **Short Tap Tiles**: Rapid-fire single taps for quick reflexes (+1 score).
  * **Sustained Hold Tiles**: Long multi-row notes requiring continuous press and hold until the tail finishes (+2 bonus score).
  * **Adaptive Velocity Scaling**: Note fall speed scales dynamically with your score, intensifying the challenge as your combo grows.
* 🎵 **Low-Latency Dual-Audio Architecture**:
  * **SoundPool Engine**: Instantaneous, zero-lag sound effects for tile taps, wrong-tap error buzzes, and UI button clicks.
  * **Interactive Soundtrack Preview**: Tap any track to audition high-fidelity audio snippets directly in the selection menu before starting a run.
  * **Multi-Track Library**: 6 selectable rhythm tracks (*Don't Blame Me*, *Chanel*, *At My Worst*, *Faded*, *Midnight Kisses*, *On The Dance Floor*) with looping and instant rewind (`seekTo(0)`) on restart.
  * **Ambient Menu Theme**: Soft ambient music (`tilerush_theme`) looping seamlessly throughout the Start Menu, Track Select, and Game Over screens.
* 🎨 **Cyberpunk & Synthwave Visuals**:
  * High-contrast dark navy gradient background (`#0A0A22` to `#381270`).
  * Two-tone glowing typography (Electric Cyan `#00E1FF` & Neon Magenta `#FF46C8`).
  * Real-time glow bloom shaders powered by Android's `BlurMaskFilter`.
  * Ambient floating neon decor particles with sinusoidal hovering animations.
* 🏆 **Arcade High Score Persistence**:
  * Local high scores are automatically preserved across sessions using Android's lightweight `SharedPreferences`.
  * Dynamic high-score celebration badge on Game Over.
* 🔊 **Master Audio Controls**:
  * Persistent one-touch mute toggle in the HUD to silence gameplay OST, menu themes, and sound effects.

---

## 🛠️ Tech Stack & Architecture

TileRush/
├── app/src/main/
│   ├── java/com/example/tilerush/
│   │   ├── MainActivity.java     # Screen orchestration, menus, high scores, lifecycle & audio routing
│   │   ├── GameView.java         # 60 FPS Canvas game loop, input dispatch, hold tracking & collision
│   │   └── Tile.java             # Lane positioning, dimensions, hold states & completion flags
│   └── res/
│       ├── raw/
│       │   ├── bg_music1.mp3     # "Don't Blame Me" game track
│       │   ├── bg_music2.mp3     # "Chanel" game track
│       │   ├── bg_music3.mp3     # "At My Worst" game track
│       │   ├── bg_music4.mp3     # "Faded" game track
│       │   ├── bg_music5.mp3     # "Midnight Kisses" game track
│       │   ├── bg_music6.mp3     # "On The Dance Floor" game track
│       │   ├── tilerush_theme.mp3# Soft ambient menu theme
│       │   ├── tap.wav           # Low-latency tile tap sound effect
│       │   ├── button_click.mp3  # UI button click sound effect
│       │   └── error.mp3         # Missed / wrong-tap error buzzer
│       └── values/
│           ├── colors.xml
│           └── strings.xml

---

## Core Technologies
Language: Java 17
Android Target SDK: API 34 (UpsideDownCake)
Minimum SDK: API 26 (Android 8.0 Oreo)
Graphics: Hardware-accelerated android.graphics.Canvas with custom Paint shaders
Audio Pipelines: android.media.SoundPool (SFX) + android.media.MediaPlayer (BGM / OST)
UI & Animation: Native Android View Hierarchy + ObjectAnimator + ValueAnimator

---

## 🚀 Getting Started

**Prerequisites**
Android Studio
(Hedgehog, Ladybug, Meerkat or newer)
JDK 17+
Android device or emulator running Android 8.0 (API 26) or higher

---

## Installation & Run

**Clone the repository:**
bash
git clone https://github.com/serrraaa012/TileRush.git
cd TileRush
