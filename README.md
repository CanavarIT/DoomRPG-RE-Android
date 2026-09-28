# Doom RPG — Native Android Port

A native Android port of the 2005 mobile game **Doom RPG** (id Software).
Based on the reverse-engineered engine **[DoomRPG-RE](https://github.com/Erick194/DoomRPG-RE)** by Erick194.

## Overview

This project ports the original BREW-based Doom RPG to Android using SDL2.
The APK does not contain any copyrighted game assets. The user must provide
the game resources separately.

## Features

- Native SDL2 rendering
- On-screen controls (D-Pad and action buttons)
- Save and load support
- Access code entry keypad
- Fixed portrait orientation
- WAV audio via SDL2_mixer

## Requirements

- Android 10 or later (API 29+)
- Architecture: arm64-v8a
- Approximately 10 MB of free storage for the APK
- Additional storage for game resources

## Installation

### Step 1: Download the APK

Download the latest APK from the [Releases](../../releases) section.

### Step 2: Prepare game resources

The APK does not contain any copyrighted assets. You must create
`DoomRPG.zip` from the original `doomrpg.bar` container yourself.

1. Obtain `doomrpg.bar` — the original BREW container of Doom RPG
   (approximately 500 KB). It can be found in BREW game archives.

2. Download `BarToZip.exe` from the
   [DoomRPG-RE Releases](https://github.com/Erick194/DoomRPG-RE/releases).
   This utility converts the `.bar` container into a `.zip` archive.

3. Run:
