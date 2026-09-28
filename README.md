# Doom RPG — Android Native Port

Native Android port of **Doom RPG** (Fountainhead / id Software, 2005) based on the **DoomRPG-RE** reverse-engineered C engine, running under **SDL2**.

This repository provides the application shell, JNI bridge, touch UI, and engine integration. **Game data files are not distributed.**

---
## Download

Latest APK: [Releases](https://github.com/CanavarIT/DoomRPG-RE-Android/releases)

## Table of contents

1. [Overview](#overview)
2. [Legal notice](#legal-notice)
3. [Requirements](#requirements)
4. [Repository contents](#repository-contents)
5. [Required data files](#required-data-files)
6. [Installing data (end users)](#installing-data-end-users)
7. [Build instructions](#build-instructions)
8. [Runtime layout](#runtime-layout)
9. [Controls](#controls)
10. [Save system](#save-system)
11. [Architecture](#architecture)
12. [Engine modules](#engine-modules)
13. [Maps](#maps)
14. [Port-specific fixes](#port-specific-fixes)
15. [Debugging](#debugging)
16. [License notes](#license-notes)

---

## Overview

Doom RPG is a turn-based first-person RPG set in the Doom universe, originally released for Java ME and BREW handsets. Gameplay is grid-based, combat is statistical, and levels are stored as compact BSP-style resources.

This project embeds the reverse-engineered client logic as native C code (`DoomRPG-RE`), linked against SDL2 / SDL2_mixer, and packaged as a standard Android application (`com.doom.rpg`).

**Package identity**

| Field | Value |
|-------|--------|
| Application ID | `com.doom.rpg` |
| Namespace | `com.doom.rpg` |
| Launcher activity | `com.doom.rpg.MainActivity` |
| Orientation | Landscape (forced) |
| Primary ABI | `arm64-v8a` |
| minSdk | 29 |
| targetSdk | 34 |
| compileSdk | 36 |

---

## Legal notice

**Doom**, **Doom RPG**, and all associated names, characters, and assets are property of their respective rights holders.

This repository does **not** include:

- `DoomRPG.zip` (game resource archive)
- `gm.sf2` (General MIDI soundfont used for music playback)

Distributing those files would infringe copyright. Builds published from this tree are expected to ship **without** game data. Users must supply the two files themselves from a legally obtained copy of the original game / compatible resource pack.

See the **Releases** page of this repository for a short end-user procedure on where to place the files.

---

## Requirements

### Build machine

- JDK 17
- Android SDK (API 34+), CMake 3.31.x (or SDK-managed equivalent)
- NDK (project pins `29.0.14033849` in `app/build.gradle`; other recent NDKs may work)
- Gradle 9.x (wrapper as provided)

### Device / emulator

- Android 10 or newer (`minSdk 29`)
- `arm64-v8a` preferred (only ABI enabled by default)

### Optional

- Physical device recommended for touch testing
- `adb` for logcat and file inspection

---

## Repository contents

```text
.
├── app/
│   ├── build.gradle
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/doom/rpg/MainActivity.java
│       ├── java/org/libsdl/app/          # SDL Activity glue
│       ├── cpp/
│       │   ├── CMakeLists.txt
│       │   ├── native-lib.c             # JNI + SDL_main entry
│       │   ├── DoomRPG-RE/              # Engine sources
│       │   └── third_party/             # SDL headers, mixer headers, fluidsynth stub
│       ├── assets/                      # Intentionally empty of game data
│       ├── jniLibs/<abi>/               # Prebuilt libSDL2.so, libSDL2_mixer.so
│       └── res/
├── build.gradle
├── settings.gradle
└── README.md
```

`app/src/main/assets/` in the public tree must **not** contain `DoomRPG.zip` or `gm.sf2`. CI and release packages should keep that directory free of copyrighted payloads.

---

## Required data files

| File | Role |
|------|------|
| **DoomRPG.zip** | Resource archive: maps (`*.bsp`), `entities.db`, sprites, strings, audio samples, event data |
| **gm.sf2** | SoundFont used by the audio path for sequenced music |

Exact internal layout of `DoomRPG.zip` must match what the engine expects (entry names such as `entities.db`, `/intro.bsp`, level BSPs, etc.). Sources for these files are outside the scope of this repository.

---

## Installing data (end users)

Release APKs built from this project do not embed game data. After installing the APK, the two files must be provided.

**Canonical procedure** (also summarized on the **Releases** page):

1. Obtain `DoomRPG.zip` and `gm.sf2` from a lawful source.
2. Install the APK once so Android creates the app private directory.
3. Push both files into the application files directory:

```bash
adb push DoomRPG.zip /sdcard/DoomRPG.zip
adb push gm.sf2 /sdcard/gm.sf2

adb shell run-as com.doom.rpg cp /sdcard/DoomRPG.zip files/DoomRPG.zip
adb shell run-as com.doom.rpg cp /sdcard/gm.sf2 files/gm.sf2
```

Alternatively, on a rooted device or with a file manager that can access app-private storage, copy directly to:

```text
/data/data/com.doom.rpg/files/DoomRPG.zip
/data/data/com.doom.rpg/files/gm.sf2
```

4. Launch the application. Native startup performs `chdir()` to `getFilesDir()` and opens the archive from the current working directory.

If either file is missing, startup or first level load will fail (zip open errors in logcat, missing music, or immediate exit depending on failure point).

**Note:** `MainActivity` can also copy these names from APK `assets/` when present. That path is intended for private / local builds only. Public GitHub releases must not ship the files inside the APK.

---

## Build instructions

### Android Studio

1. Open the repository root (directory containing `settings.gradle`).
2. Let Gradle sync complete.
3. **Build → Rebuild Project**.
4. Run on device or emulator.

### Command line

```bash
./gradlew :app:assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk

./gradlew :app:assembleRelease
```

### Native build graph

- CMake root: `app/src/main/cpp/CMakeLists.txt`
- Shared libraries produced:
  - `libDoomRPG.so` — engine
  - `libnative-lib.so` — JNI bridge and `SDL_main`
- Links against prebuilt `libSDL2.so` and `libSDL2_mixer.so` under `jniLibs`
- Default ABI filter: `arm64-v8a` only

To enable additional ABIs, extend `ndk.abiFilters` and supply matching prebuilt SDL libraries under `app/src/main/jniLibs/<abi>/`.

---

## Runtime layout

On first launch (or after install), relevant paths are:

```text
/data/user/0/com.doom.rpg/files/
├── DoomRPG.zip      # user-supplied
├── gm.sf2           # user-supplied
├── Config           # created at runtime
├── Player           # save slot
├── Player2          # continue / secondary slot
└── World            # world state paired with player save
```

`nativeSetDataDir` receives `getFilesDir().getAbsolutePath()`. `SDL_main` changes the process working directory to that path before calling `DoomRPG_main`. All relative file I/O (saves, zip open) is therefore rooted at the app files directory.

---

## Controls

### On-screen game pad

| Control | SDL scancode | Function |
|---------|--------------|----------|
| D-pad | Up / Down / Left / Right | Move / turn |
| **E** | Return | Attack / talk / confirm |
| **X** | Escape | Menu / back |
| **M** | Tab | Automap |

Implemented in `MainActivity` via `MotionEvent` → `nativeSendKey` → SDL keyboard events and `g_virtualKeys[]`.

### Password keypad

Certain map events require a numeric passcode (`ST_DIALOGPASSWORD`).

- Entering that state calls `MainActivity.setPasswordKeypadVisible(true)` from native code.
- A 0–9 grid is shown, plus delete (mapped to Left) and confirm (Return).
- The movement pad is hidden while the keypad is active.
- Leaving the state hides the keypad.

Digit keys are injected as `SDL_SCANCODE_0` … `SDL_SCANCODE_9`, which the engine maps to `AVK_0` … `AVK_9` and handles in `DoomCanvas_handlePasswordEvents`.

### Hardware input

Standard SDL keyboard / game controller paths remain available. Default binds are defined in `keyMappingDefault` inside `DoomRPG.c`.

---

## Save system

| File | Purpose |
|------|---------|
| `Config` | Settings, config version, input binds |
| `Player` | Primary player snapshot |
| `Player2` | Secondary / continue snapshot |
| `World` | Entity flags, sprite state, map flags, tile events |

Load sequence (simplified):

1. Menu selects Continue or Load.
2. `Game_loadState` → `Game_loadPlayerState` (binary `rb`).
3. Map load from the stored map path.
4. Entity rebuild from the BSP / sprite list.
5. If the load type requires world restore, `Game_loadWorldState` applies `World`.

Binary modes and null-handle checks are required; incorrect modes or closing a null `SDL_RWops` historically caused native crashes on Android.

Sprite indices stored in `World` may reference slots beyond base map sprites. The render buffer is allocated as:

```text
numSprites = numMapSprites + MAX_CUSTOM_SPRITES (16) + MAX_DROP_SPRITES (8)
```

Bounds checks during load must use `numSprites`, not `numMapSprites` alone.

---

## Architecture

```text
MainActivity (Java, SDLActivity)
        │
        │  JNI
        ▼
native-lib.c
  · nativeSetDataDir
  · nativeSendKey
  · chdir + DoomRPG_main
        │
        ▼
DoomRPG-RE (C shared library)
  DoomRPG / DoomCanvas / Game / Player
  Entity / Combat / Render / Menu / Sound / Zip
```

### JNI surface

```text
Java_com_doom_rpg_MainActivity_nativeSetDataDir
Java_com_doom_rpg_MainActivity_nativeSendKey
```

Native → Java callback used for the password UI:

```text
MainActivity.setPasswordKeypadVisible(boolean)
```

Invoked from `DoomCanvas_setState` when transitioning into or out of `ST_DIALOGPASSWORD` (Android builds only).

---

## Engine modules

| Module | Responsibility |
|--------|----------------|
| `DoomRPG.c` | Init, main loop, key → AVK translation, file helpers |
| `DoomCanvas.c` | State machine, dialogs, password UI logic, map load orchestration |
| `Game.c` | Map logic, entities, tile events, save / load |
| `Player.c` | Player stats, inventory, weapons, notebook |
| `Entity*.c` / `Combat*.c` | Actors and turn-based combat |
| `Render.c` | BSP, sprites, walls, automap, palettes |
| `Menu*.c` / `MenuSystem.c` | Menus |
| `Hud.c` | Status bar and messages |
| `Sound.c` | SFX / music |
| `Z_Zip.c` / `Z_Zone.c` | Resource archive and allocator |
| `SDL_Video.c` | SDL window / renderer glue |

---

## Maps

Logical map IDs include:

- `MAP_INTRO`
- `MAP_SECTOR01` … `MAP_SECTOR07`
- `MAP_JUNCTION`, `MAP_JUNCTION_DESTROYED`
- `MAP_ITEMS`, `MAP_REACTOR`, `MAP_END_GAME`

Corresponding BSP names are resolved through internal `mapFiles[]` tables (e.g. `/intro.bsp`, `/level01.bsp`, …). Tile events include map change, dialog, shop, combat, and password gates (`EV_PASSWORD`).

---

## Port-specific fixes

Changes applied for stable Android behaviour:

1. Guarded `SDL_RWclose` — never close a null handle when a save file is missing.
2. Binary open modes (`"rb"` / `"wb"`) for player and world files.
3. `Game_loadPlayerState` returns success/failure; failed loads abort before map load with invalid state.
4. World load sprite index validation against `numSprites` (includes custom and drop slots).
5. Menu switch brace correctness for in-game load paths.
6. Password numeric keypad wired to engine state transitions.
7. Working directory set to application `filesDir` after optional asset copy.

---

## Debugging

### Logcat filters

```bash
adb logcat -s DoomRPG:I SDL/APP:I SDL:V
```

Useful markers:

```text
cwd changed to /data/user/0/com.doom.rpg/files
Z_Zip: entry_count=...
Game_loadState type=1 file=Player2
loadWorldState entities cnt=... numMapSprites=... numSprites=...
Password keypad visible=true
```

`SDL_Log` output appears under the **SDL/APP** tag.

### Common failures

| Symptom | Check |
|---------|--------|
| Immediate failure at start | Missing `DoomRPG.zip` in `files/` |
| Load / Continue crash | Prior sprite-bound / RWops issues; inspect load logs |
| No music | Missing `gm.sf2` |
| Empty continue slot | No valid `Config` + player/world files yet |
| Numpad never shows | Rebuild after `DoomCanvas` / `MainActivity` password hooks |

### Inspect app files (debuggable builds)

```bash
adb shell run-as com.doom.rpg ls -la files/
```

---

## License notes

- **Engine integration and Android shell:** see repository license file if present; otherwise treat as provided by the repository owner.
- **SDL2 / SDL2_mixer:** zlib-style licenses; retain upstream notices.
- **Doom RPG assets and trademarks:** not granted by this repository. Do not commit or attach `DoomRPG.zip` or commercial soundfonts to public forks.
- **DoomRPG-RE:** respect the terms of the reverse-engineering project you derived sources from.

Public distributions of this port should document clearly that **game data is user-supplied** and point to the **Releases** instructions for installation of `DoomRPG.zip` and `gm.sf2`.

---

## Quick verification checklist

1. `assembleDebug` / `assembleRelease` completes; `libDoomRPG.so` and `libnative-lib.so` link.
2. APK installed; `files/DoomRPG.zip` and `files/gm.sf2` present.
3. Logcat shows successful `chdir` and non-zero zip entry count.
4. New Game reaches gameplay; Save then Load / Continue restores state.
5. Password event shows numeric keypad; digits register in the dialog.

---

*This document describes the Android packaging and runtime contract for the native Doom RPG client. It does not redistribute original game content.*
