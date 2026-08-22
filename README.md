# True Distance

> Privacy-first Android app for live straight-line ("as the crow flies") distance tracking to any destination on Google Maps and live Speedometer trip recording.

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
- **Speedometer & Live Trip Tracking (V2)**:
  - Immediate initial location fetch and centering on map ready (`17f` zoom).
  - High street-level zoom on trip start (`18.5f`).
  - Continuous breadcrumb route polyline (`#00796B`, 8px).
  - Dynamic camera auto-dragging and auto-zooming out with UI overlay padding.
  - Stationary GPS noise deadband filtering (`0.6 m/s`) to eliminate stationary speed/distance jitter.
  - Interactive foreground service notification with live Speed, Distance, Elapsed time, and synced Pause/Resume and Stop controls.
- **Past Trips History (V2)**:
  - 80/20 card layout with single-card exclusive expansion.
  - Embedded Google Map route snapshot with Start (Green) and End (Red) markers, route polyline, Max Speed, and End timestamp.
- **Saved Locations**: Save favorite destinations locally with customizable names and addresses.
- **Distance History & Time-Based Snapshots**:
  - Auto-saved trip history grouped by date (**Today / Yesterday / Older**).
  - Card preview: Destination name, tracked distance (`initial − final`), start/stop timestamps, and elapsed time.
  - **Single-expand 3-column table**: Displays interval snapshots (`Elapsed Mark` | `Clock Time` | `Distance`) derived post-hoc from raw GPS fixes across 4 duration tiers (A–D).
- **Background Tracking**: Dedicated foreground services (`TrackingService`, `SpeedometerService`) with persistent notifications and synchronized action controls.
- **Global Units & Precision**: Default **KM** (configurable to Miles or Both), with 3-digit meters (`000 M` / `000 M/H`) under 1 KM and 2 decimals (`%.2f KM` / `%.2f KM/H`) at or above 1 KM.
- **Modern UI & Full Dark Mode**:
  - **Compact Material3 Floating Pill Navigation Bar**: Sleek, less wide floating card container with 36dp rounded corners, 12dp elevation shadow, and `labelVisibilityMode="selected"` — only the currently open tab ("True Distance" / "Speedometer" / "Settings") shows its filled capsule pill and text label, while inactive tabs display icons only.
  - Smart tab reselection: Tapping any active bottom nav item or switching tabs pops back stack directly to the tab's root destination (`DistanceFragment`, `SpeedometerFragment`, `SettingsFragment`).
  - **Screen Headers & Unified Header Actions**: Each main tab features a Medium Big screen title header ("True Distance" / "Speedometer" / "Settings", 24sp bold) on the top-left, with Saved Locations (`ic_bookmark`) placed on the top-right BEFORE Trip History (`ic_history`) with clean padding.
  - **Unified Header & Map Controls**: Identical top-right history icon chip button position across True Distance and Speedometer screens, and a standardized custom 3-button stack (`[Current Location]`, `[+] Zoom In`, `[-] Zoom Out`) anchored at bottom-right of all map views with 10dp vertical spacing and chip button styling (`bg_icon_chip.xml`).
  - **Instant Custom Splash Screen**: Omitted the initial system splash delay to immediately display the branded splash screen with app title and dynamic version number.
  - **Stable Launcher Icon**: Dark/light mode independent launcher icon that maintains original colors across system themes and Android 13+ wallpaper tinting.
  - 24dp rounded cards with an **80/20 layout split** (content | centered delete action).
  - Pastel gradients (Mint, Peach, Lavender, Blue) with tone-matched high-contrast typography.
  - Complete dark theme overrides (`values-night/` and `drawable-night/`).
  - Elevated, soft-bordered map frames (16dp radius).

---

## 🧭 Navigation & Screens

| Tab / Screen | Description |
|---|---|
| 📍 **True Distance** | Main hub with destination search, map picker, quick-select, and trip launcher. |
| 🗺️ **Tracking Screen** | Full map tracking view with soft border, glassmorphic readout, recenter FAB, and stop action. |
| 🔖 **Saved Locations** | Full list of stored destinations with 80/20 card layout and add FAB. |
| 🕒 **Distance History** | Date-grouped trip log with single-expand 3-column interval snapshots. |
| ⏱️ **Speedometer** | Live trip tracking map with floating speed readout, stats card, dynamic camera auto-fit, and interactive notification. |
| 📜 **Past Trips** | Speedometer trip history with single-card expandable route map snapshots. |
| ⚙️ **Settings** | Theme (Light/Dark/System), Units, GPS accuracy, update frequency, and About info. |

---

## 🛠️ Tech Stack & Architecture

- **Language & Runtime**: Kotlin & Coroutines / StateFlow / Hilt Dependency Injection
- **Architecture**: Clean MVVM + Repository Pattern
- **Persistence**: Room Database v2 (SQLite) with local-only storage (no login, no cloud dependency)
- **Mapping & Location**: Google Maps SDK, Google Places API, FusedLocationProviderClient
- **Background Work**: Android Foreground Service with notification channels (`tracking_channel`, `speedometer_channel`)
- **UI Framework**: Android Jetpack, View Binding, Material3 Components with DayNight theming

---

## 📋 Roadmap

- **V1 (Completed)**: Core True Distance tracking, Saved Locations, Distance History with time-based tier snapshots, Settings, and full Dark Mode.
- **V2 (Completed - Current `v2.0.0 (2)`)**: Speedometer live trip tracking, breadcrumb polyline, dynamic camera auto-fit bounds, stationary noise filtering, interactive notification, Past Trips expandable map snapshots, and Material3 floating pill navigation bar.
- **V3 (Upcoming)**: Home-screen widgets (4×2), App shortcuts, sticky notification toggles, and auto-pause.

---

## 📄 License & Documentation

Detailed design specifications and implementation details can be found in:
- [Final UI Design.md](./Final%20UI%20Design.md) — Comprehensive visual design specification (v1.1).
- [Detailed Project Overview.md](./Detailed%20Project%20Overview.md) — Architectural overview and technical requirements.
- [Time Based Log.md](./Time%20Based%20Log.md) — Snapshot interval tier logic.
