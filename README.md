# 🛠️ Brillian Tools Suite

> **The Ultimate On-Device, High-Precision Offline Companion for Advanced Trades & Industrial Engineering.**

[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android)](https://developer.android.com)
[![Language](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=flat-square&logo=kotlin)](https://kotlinlang.org)
[![UI Framework](https://img.shields.io/badge/UI-Jetpack%20Compose%20%28Material%203%29-4285F4?style=flat-square&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Local DB](https://img.shields.io/badge/Database-Room%20SQLite-005C97?style=flat-square&logo=sqlite)](https://developer.android.com/training/data-storage/room)
[![AI Engine](https://img.shields.io/badge/AI_Engine-On--Device%20LLM%20%28Offline%29-FF6F00?style=flat-square)](https://ai.google.dev)

---

## 📖 Overview

**Brillian Tools Suite** is a desktop-grade, offline-first mobile toolkit engineered specifically for forestry, woodworking, electrical engineering, masonry, general construction, and on-site industrial paint specialists. 

Unlike conventional utility apps, the Brillian Tools Suite is built for **100% off-grid operation** on remote jobsites with zero cell reception or internet dependency. It features a fully on-device, lightweight AI engine (**Brillian Copilot**) running locally optimized models like **SmolLM2 360M** and **Qwen2.5 1.5B** to compute complex trade calculations and reference formulas without latency or privacy concerns.

---

## 🎨 Visual Identity & Architecture

```
                  ┌─────────────────────────────────────┐
                  │        Brillian Tools Suite         │
                  │        (Jetpack Compose UI)         │
                  └──────────────────┬──────────────────┘
                                     │
         ┌───────────────────────────┼───────────────────────────┐
         ▼                           ▼                           ▼
┌─────────────────┐         ┌─────────────────┐         ┌─────────────────┐
│  Local AI RAG   │         │  Hardware APIs  │         │ Persistent Logs │
│   LLM Engine    │         │ (Compass/Level) │         │ (Room Database) │
│ (SmolLM2/Qwen)  │         │  Sensor Telemetry│        │ Offline Queue   │
└─────────────────┘         └─────────────────┘         └─────────────────┘
```

The application is built on a highly-optimized, reactive, MVVM-based native architecture:
1. **Presentation Layer**: 100% declarative UI built with Jetpack Compose (Material 3), featuring a gorgeous high-contrast theme, generous negative space, dynamic visual ripples, and fluid layout transitions.
2. **Local AI Inference (RAG Engine)**: A snappy, offline-first rule engine that parses user intent and serves exact technical calculations from a local trade database, embedding interactive Deep Link action cards inside the chat bubble to open corresponding calculator screens instantly.
3. **Hardware Telemetry Interface**: Real-time integration with direct physical sensors (barometer, light meter, compass, accelerometer/gyroscope) to provide microsecond accurate readings.
4. **Data Persistence**: Offline state, work logs, safety checklists, and active calculations are securely persisted in a relational Room SQLite Database.

---

## 🚀 Core Features

### 1. 🤖 On-Device Brillian Copilot & Trade RAG
*   **Fully Offline Conversational AI**: Converse with a local LLM to query complex equations, NEC guidelines, or material properties with zero cell reception.
*   **Dynamic Intent Interception**: Friendly greeting detection, quick capability overviews, and direct troubleshooting guides.
*   **Intelligent Deep Linking**: The assistant parses technical queries and appends clickable action cards (with custom titles and navigation routes) right inside the chat bubble, enabling one-tap transitions from the discussion to the exact utility calculator needed.
*   **Model Selection & Diagnostics (Settings Page)**: Integrated model weights manager letting you select active downloaded LLMs, track CPU/Vulkan shader compiles, monitor memory footprint, and check hardware capability.

### 2. 🪚 Professional Woodworking Studio
*   **Timber Sagulator**: Calculate allowable wood shelf deflection under uniform loads based on wood species stiffness indices (Red Oak, Walnut, Pine).
*   **2D Cutlist Optimizer**: Pack nested rectangular parts onto standard sheets of material (e.g., 48"x96" plywood) while strictly accounting for a standard **1/8" table saw blade kerf** to maximize material yield.
*   **Board Footage Estimator**: Instantly compute rough-sawn dimensional board feet and maintain running cost logs.
*   **Moisture Tracker**: Monitor timber hydration levels to prevent post-build warping.

### 3. ⚡ Electrical & Conduit Tools
*   **Voltage Drop Calculator**: Keep branch circuits within NEC 210.19(A) guidelines (under 3% drop) by calculating voltage drop based on copper/aluminum gauge, length, and circuit current.
*   **Conduit Fill Estimator**: Avoid wire insulation shearing by computing NEC conduit fill percentages for multiple THHN wire sizes.
*   **Conduit Bender (3-Point Saddle / Offset)**: Calculate exact multiplier markings, shrink factors, and saddle dimensions.

### 4. 🧱 Masonry & Coatings Studio
*   **Paint & Coating Studio**: Accurately predict wet-to-dry gauge conversions and paint volume requirements by applying multi-factor substrate porosity allowances (e.g. 1.6x multiplier for porous rough unsealed brick).
*   **Concrete Volume Sizer**: Slabs, footings, and cylindrical columns volume requirements, calculated with automated bag-count conversions (80lb and 60lb mixes).
*   **Rebar Estimator**: Determine exact rebar lengths, overlap spacing, and weight requirements for reinforced slabs.

### 5. 🎛️ Real-Time Hardware Diagnostics & Sensor Suite
*   **Digital Level**: Dynamic surface tilt and grade meter using the device accelerometer.
*   **Magnetic Compass**: Full orientation heading tracker with jobsite heading locking.
*   **Barometric Altimeter**: Real-time altitude estimation based on atmospheric pressure.
*   **Lux Light Meter**: Measures illumination to verify safety compliance in industrial work areas.
*   **Decibel Sound Tracker**: Live microphone decibel telemetry for OSHA hearing-protection compliance.
*   **BLE Multimeter integration**: Live wireless voltage and current monitoring (mocked/API ready).

---

## 📦 Libraries & Technology Stack

The Brillian Tools Suite relies exclusively on robust, developer-trusted libraries to maintain professional-grade stability:

*   **Kotlin Coroutines & Flow**: Powering synchronous local calculations and streaming reactive sensor states.
*   **Jetpack Compose**: Declarative, Material 3-compliant layouts utilizing unified typography (Plus Jakarta Sans & Playfair Display), custom shapes, and highly accessible touch targets (min 48dp).
*   **Jetpack Navigation Compose**: Handles type-safe, fluid screen transitions and dynamic floating assistant integration.
*   **Room Database (SQLite + KSP)**: Enterprise-grade offline relational persistence for tasks, notes, inventory logs, and history files.
*   **Android WorkManager**: Orchestrates background synchronization queues safely in compliance with OS battery-saver states.
*   **AndroidX Sensor Framework**: Binds physical device hardware sensors for altimeter, level, light, and compass readings.

---

## 🛠️ Installation & Getting Started

### Prerequisites
*   Android Studio Ladybug (or newer)
*   Android SDK 34+
*   Kotlin 1.9.0+

### Setup Instructions
1.  **Clone the Repository**:
    ```bash
    git clone https://github.com/brillian-dsgn/brillian-tools-suite.git
    cd brillian-tools-suite
    ```
2.  **Open in Android Studio**:
    *   Select **File > Open** and choose the `brillian-tools-suite` folder.
    *   Allow Gradle to sync and download necessary dependencies.
3.  **Download Local AI Weights**:
    *   Run the app on a device/emulator.
    *   Navigate to **Settings > Local AI Model Manager**.
    *   Select and activate an offline weight file (e.g. SmolLM2 360M or Qwen2.5 1.5B).
4.  **Run the App**:
    *   Click **Run 'app'** (`Shift + F10`) to compile and install the APK on your device.

---

## 👥 Authors & Core Team

*   **brillian.dsgn** — *Lead Systems Architect & UX/UI Designer*
*   **AI Coding Assistant (Google DeepMind)** — *Co-Developer & local RAG Integration Engineer*

---

*Engineered with precision. Secure. Offline. Built for the Trades.*
