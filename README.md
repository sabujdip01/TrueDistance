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
  - Auto-fitting camera bounds with dynamic padding for UI overlays and control stacks.
  - **WhatsApp-Style Recenter & Overview**: Tapping Recenter zooms tightly to current location (`17.5f`) and reveals a Return-to-Overview button (`ic_undo`); tapping Return-to-Overview frames both markers and lines with overlay-safe padding.
  - Glassmorphic distance overlay with 36sp bold numerals.
  - Auto-arrival detection (≤ 10m) with "Destination Reached" celebration dialog.
- **Speedometer & Live Trip Tracking (V2)**:
  - Immediate initial location fetch and centering on map ready (`17f` zoom).
  - High street-level zoom on trip start (`18.5f`).
  - Continuous breadcrumb route polyline (`#00796B`, 8px).
  - Dynamic camera auto-dragging and auto-zooming out with UI overlay padding and control stack clearance.
  - **WhatsApp-Style Recenter & Overview**: Smoothly toggle between full route overview framing and tight user location follow mode.
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
- **Global Units & Precision**: Default **Kilometers (KM / M)** (configurable to **Miles (MI / FT)**), with meters formatted cleanly without leading zeros (`87 M` / `187 M` under 1 KM) and 2 decimals (`%.2f KM` / `%.2f KM/H`) at or above 1 KM.
- **Modern UI & Full Dark Mode**:
  - **Compact Material3 Floating Navigation Bar**: Sleek floating pill container (36dp rounded corners, 12dp elevation shadow) matching modern messenger navigation aesthetics. Selected tab displays a soft lavender pill indicator (`#EDE7F6` light / `#3B2D54` dark) with accent violet icon and bold text label (`#7C4DFF` light / `#B388FF` dark), while inactive tabs display existing charcoal/slate icons and regular non-bold text labels (`#2D3748` light / `#E2E8F0` dark) with centered vertical alignment.
  - Smart tab reselection: Tapping any active bottom nav item or switching tabs pops back stack directly to the tab's root destination (`DistanceFragment`, `SpeedometerFragment`, `SettingsFragment`).
  - **Screen Headers & Clean Whitespace**: Every screen (True Distance, Speedometer, Settings, Saved Locations, Distance History, Map Picker, Live Tracking, Past Trips) features a prominent 24sp bold title page header with generous whitespace separation above cards.
  - **Unified Map Controls Stack & Padding**: Standardized custom 3-button stack (`[Current Location]`, `[+] Zoom In`, `[-] Zoom Out`) anchored at bottom-right of all map views with 10dp vertical spacing and camera viewport padding ensuring start/end markers are never obscured by buttons.
  - **Race Flag & Car Map Markers**: Speedometer map initially renders a standard Red Pin marker; upon pressing Start, the origin transforms into a checkered race flag (`ic_race_flag`) and current location is marked with a small car icon (`ic_car`).
  - **Instant Branded Splash Screen**: Bypasses initial system splash delay to immediately display logo, app title, version `v2.0.0 (2)`, and developer credit: `Made with ❤️ by Sabuj`.
  - **Speedometer Stats Card & Header Icons**: Stat headers (Distance, Avg Speed, Max Speed, Start Time, Elapsed Time) feature dedicated vector icons (`ic_distance`, `ic_avg_speed`, `ic_max_speed`, `ic_clock`, `ic_timer`) with theme-adapted contrast typography (`#455A64` Light / `#CBD5E0` Dark).
  - **Revamped Elevated Settings Screen**:
    - Interactive Pill Toggle Selector Groups (`MaterialButtonToggleGroup`) with drop shadows for **Unit Preference** (`Kilometers (KM / M)` / `Miles (MI / FT)`), **App Theme** (`System` / `Light` / `Dark` with instant night mode application), **GPS Accuracy Mode** (`High` / `Balanced` / `Device`), and **Update Frequency** (`1s` / `2s` / `3s` / `5s`).
    - **Keep Screen On Toggle**: Persistent toggle switch that keeps the device screen active during navigation and trip tracking.
    - Dedicated vector icons beside every setting option (`ic_distance`, `ic_theme`, `ic_timer`, `ic_avg_speed`, `ic_clock`, `ic_screen`, `ic_privacy`, `ic_github`, `ic_developer`).
    - **In-App Privacy Policy Dialog**: Built-in `MaterialAlertDialogBuilder` displaying privacy terms cleanly without launching external browser windows.
    - **Open Source Repository Link**: GitHub icon link opening `https://github.com/sabujdip01/TrueDistance.git`.
    - **Developer Credit Link**: `Made with ❤️ by Sabuj Mondal` (`About Me →`) hyperlinked to `https://about.me/sabujdip01`.
  - **Stable Launcher Icon**: Dark/light mode independent launcher icon that maintains original colors across system themes and Android 13+ wallpaper tinting.
  - 24dp rounded cards with an **80/20 layout split** (content | centered delete action).
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

## 💡 Future Improvement Ideas

- **Home Screen Widget (4×2)**: Live distance and destination card on Android home screen.
- **App Shortcuts**: Quick launcher shortcuts for True Distance and Speedometer.
- **Auto-Pause When Stationary**: Automatic pause detection when vehicle/user is stopped.
- **Removable Notification Toggle**: Settings switch to allow dismissing foreground tracking notifications.
- **Landscape Mode & Tablet Split-Screen**: Responsive landscape UI with side-by-side map and telemetry cards for dashboard car mounts and tablets.

---

## 📄 License & Documentation

Detailed design specifications and implementation details can be found in:
- [Final UI Design.md](./Final%20UI%20Design.md) — Comprehensive visual design specification (v1.1).
- [Detailed Project Overview.md](./Detailed%20Project%20Overview.md) — Architectural overview and technical requirements.
- [Time Based Log.md](./Time%20Based%20Log.md) — Snapshot interval tier logic.
