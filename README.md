# True Distance

> Privacy-first Android app for live straight-line ("as the crow flies") distance tracking to any destination on Google Maps.

---

## ✨ Features

- **Live Straight-Line Distance**: Computes great-circle distance in real time using the Haversine algorithm with continuous GPS updates.
- **Smart Destination Input**:
  - Search places with Google Places Autocomplete.
  - Interactive map picker (tap to drop pin).
  - Quick-select shortcut from Saved Locations.
- **Visual Map Tracking**:
  - Live current location marker (Red) & static destination marker (Green).
  - Solid dark teal polyline (`#00796B`) connecting both points.
  - Auto-fitting camera bounds with dynamic padding for UI overlays.
  - Glassmorphic distance overlay with 36sp bold numerals.
  - Auto-arrival detection (≤ 10m) with "Destination Reached" celebration dialog.
- **Saved Locations**: Save favorite destinations locally with customizable names and addresses.
- **Distance History & Time-Based Snapshots**:
  - Auto-saved trip history grouped by date (**Today / Yesterday / Older**).
  - Card preview: Destination name, tracked distance (`initial − final`), start/stop timestamps, and elapsed time.
  - **Single-expand 3-column table**: Displays interval snapshots (`Elapsed Mark` | `Clock Time` | `Distance`) derived post-hoc from raw GPS fixes across 4 duration tiers (A–D).
- **Background Tracking**: Foreground service with ongoing status bar notification and synchronized Stop controls.
- **Global Units & Precision**: Default **KM** (configurable to Miles or Both), with auto-meters under 1 km and customizable decimal precision.
- **Modern UI & Full Dark Mode**:
  - 24dp rounded cards with an **80/20 layout split** (content | centered delete action).
  - Pastel gradients (Mint, Peach, Lavender, Blue) with tone-matched high-contrast typography.
  - Complete dark theme overrides (`values-night/` and `drawable-night/`).
  - Elevated, soft-bordered map frames (16dp radius).
  - Floating bottom navigation bar.

---

## 🧭 Navigation & Screens

| Tab / Screen | Description |
|---|---|
| 📍 **True Distance** | Main hub with destination search, map picker, quick-select, and trip launcher. |
| 🗺️ **Tracking Screen** | Full map tracking view with soft border, glassmorphic readout, recenter FAB, and stop action. |
| 🔖 **Saved Locations** | Full list of stored destinations with 80/20 card layout and add FAB. |
| 🕒 **Distance History** | Date-grouped trip log with single-expand 3-column interval snapshots. |
| ⏱️ **Speedometer** | Placeholder in V1 (full live trip stats & breadcrumb trail in V2). |
| ⚙️ **Settings** | Theme (Light/Dark/System), Units, GPS accuracy, update frequency, and About info. |

---

## 🛠️ Tech Stack & Architecture

- **Language & Runtime**: Kotlin & Coroutines / StateFlow
- **Architecture**: Clean MVVM + Repository Pattern
- **Persistence**: Room Database (SQLite) with local-only storage (no login, no cloud dependency)
- **Mapping & Location**: Google Maps SDK, Google Places API, FusedLocationProviderClient
- **Background Work**: Android Foreground Service with notification channels
- **UI Framework**: Android Jetpack, View Binding, Material Components with DayNight theming

---

## 📋 Roadmap

- **V1 (Current)**: Core True Distance tracking, Saved Locations, Distance History, Settings, and full Dark Mode.
- **V2**: Speedometer with live gauge, average/max speed filtering, paused trip handling, and actual-path GPS breadcrumb trails.
- **V3**: Home-screen widgets (4×2), App shortcuts, sticky notification toggles, and auto-pause.

---

## 📄 License & Documentation

Detailed design specifications and implementation details can be found in:
- [Final UI Design.md](./Final%20UI%20Design.md) — Comprehensive visual design specification (v1.1).
- [Detailed Project Overview.md](./Detailed%20Project%20Overview.md) — Architectural overview and technical requirements.
- [Time Based Log.md](./Time%20Based%20Log.md) — Snapshot interval tier logic.
