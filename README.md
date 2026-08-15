# True Distance

> An Android app for tracking the straight-line distance between your live location and a selected destination.

**True Distance** is a privacy-focused Android application designed to answer a simple question: **how far away is a destination right now, in a straight line?** It tracks the device's current location, displays both points on Google Maps, draws a live connecting line, and continuously updates the distance as the user moves.

The project is designed around a phased roadmap: **V1 focuses on the True Distance experience**, **V2 introduces a full Speedometer and trip-tracking feature**, and **V3 adds widgets, app shortcuts, enhanced notifications, and other convenience features**.

---

## ✨ Key Features

### True Distance

- Live current-location tracking using device location services
- Straight-line / great-circle distance calculation
- Google Maps visualization with:
  - Live current-location marker
  - Static destination marker
  - Continuously updated connecting polyline
- Destination selection through:
  - Place search with autocomplete
  - Map point picker
  - Saved Locations quick selection
- Prominent live distance readout
- Recenter map control
- Start and stop tracking controls
- Optional background tracking with persistent notification
- One active destination tracking session at a time

### Saved Locations

Save frequently used destinations locally with:

- Custom editable name
- Resolved address
- Latitude and longitude
- Quick reuse from the main screen
- Add via place search or map selection
- Delete individual locations

### Distance History

Every tracking session is automatically recorded.

History entries include:

- Destination name or address
- Tracking date
- Initial straight-line distance
- Final distance when the session ends
- Start and end timestamps
- Expandable distance snapshots during the session

Snapshot presentation adapts to session duration:

| Session Duration | Snapshot Detail |
| --- | --- |
| Under 1 minute | Start + End |
| 1–5 minutes | Start + 50% + End |
| 5–20 minutes | Start + 25% + 50% + 75% + End |
| Over 20 minutes | Start + approximately every 10% + End |

History can be grouped by date and supports individual deletion and a confirmed **Clear All** action.

---

## 🧭 Navigation

The app uses a fixed bottom navigation bar with three tabs:

| Tab | Status | Purpose |
| --- | --- | --- |
| 📍 **True Distance** | V1 | Core live straight-line distance tracking |
| ⏱️ **Speedometer** | Placeholder in V1 | Full trip tracking planned for V2 |
| ⚙️ **Settings** | V1 | App preferences and About information |

Navigation preserves in-progress state where possible, so switching tabs does not unnecessarily reset destination selections.

Future versions may display an activity badge on a tab when its background tracking session is active.

---

## 📱 Screens

### Splash Screen

The application launches with a branded Android splash screen containing the custom app icon and **True Distance** name. The splash should initialize required state without introducing an artificial delay.

Android 12+ uses the SplashScreen API, with a compatible fallback strategy for older supported Android versions.

### Main Screen

The default True Distance screen provides:

1. Saved Locations access
2. Distance History access
3. Destination search and selection
4. Current origin and selected destination details
5. Quick selection from Saved Locations
6. Map picker access
7. A **Start Tracking** action enabled only after a valid destination is selected

### Tracking Screen

The tracking screen is centered around a full Google Map and provides:

- Live current-location updates
- Fixed destination marker
- Live straight-line polyline
- Continuously updated distance
- Recenter control
- Stop Tracking control below the map

When background tracking is enabled, leaving the app does not end the session. When disabled, leaving the tracking experience stops or ends live tracking according to the configured behavior.

### Saved Locations Screen

Users can:

- Browse saved destinations
- Add a location using search or a map picker
- Rename locations
- Review address/coordinate summaries
- Delete locations
- Select a location and return to the main screen with it pre-filled as the destination

### Distance History Screen

Tracking sessions are automatically saved and displayed in date-based groups such as **Today**, **Yesterday**, and **Older**.

Entries can expand to reveal session distance snapshots without storing a visually overwhelming list of every raw GPS update.

---

## ⚙️ Settings

### Theme

- Light
- Dark
- Follow System

Theme changes apply dynamically without requiring an app restart.

### Location Accuracy

Supported location behavior is configurable through:

- **High Accuracy** — GPS + network, prioritizing precision
- **Balanced** — lower battery usage
- **Device Only** — GPS-focused behavior

### Update Frequency

Location update intervals can be configured, for example:

- Every 1 second
- Every 3 seconds
- Every 5 seconds
- Every 10 seconds

### Units

Distance formatting supports:

- Kilometers
- Miles
- Both

Additional formatting preferences include:

- 0, 1, or 2 decimal places
- Optional automatic meter display for distances below 1 km

### Background Tracking

When enabled, active tracking can continue while the app is backgrounded through the appropriate foreground-service and notification architecture.

---

## 🗺️ How Distance Tracking Works

True Distance measures the **great-circle distance** between two geographic coordinates rather than calculating a road route.

The calculation is based on the user's current latitude/longitude and the destination's latitude/longitude using the **Haversine formula or an equivalent geodesic calculation**.

This means:

- The displayed distance is **not driving distance**
- No turn-by-turn route is required
- The result represents the approximate shortest distance over the Earth's surface between the two points
- Distance calculation itself can continue without internet when valid location data is available

Google Maps is used for visualization, while place search and address resolution depend on the configured Google Maps Platform services.

---

## 📡 Connectivity and GPS Behavior

The application treats connectivity and location availability as explicit states.

### No Internet

When internet is unavailable:

- Place search is unavailable or clearly degraded
- Map rendering may be unavailable
- Saved Locations remain available because they are stored locally
- A destination selected from Saved Locations can still be tracked if a valid current location is available
- Straight-line distance calculation continues because it is coordinate-based
- Active sessions retain distance tracking even if map tiles stop updating

### GPS or Location Unavailable

Before tracking:

- The app prompts the user to enable device location services
- Tracking cannot begin without a valid current location fix

During tracking:

- The last known marker and distance are retained
- The session does not automatically stop because of temporary signal loss
- The UI communicates that displayed data may be stale
- Live updates resume when location becomes available again

### Permission Denial

If location permission is denied:

- The app explains why the permission is required
- Users can be directed to app settings
- Non-location features such as browsing Saved Locations, History, and Settings remain usable where possible

---

## 🔐 Privacy and Data

True Distance is designed as a **local-first application**.

- No account required
- No login system
- No cloud backend
- No cloud synchronization
- Saved Locations, history, and settings are stored on-device

External services are used only where required for map rendering, place search, and geocoding through Google Maps Platform APIs.

The app's privacy policy should clearly explain permission usage and external API data flows.

---

## 🔑 Permissions

Depending on the implemented release scope, the application requires or reserves the following permissions:

| Permission / Capability | Purpose |
| --- | --- |
| Foreground Location | Detect and track current location |
| Background Location | Continue tracking when enabled and permitted |
| Notifications | Show persistent tracking notifications on supported Android versions |
| Internet | Maps, place autocomplete, and geocoding |
| Network State | Detect connectivity changes |
| Storage / Export Capability | Reserved for potential future history export |

Permissions should be requested only when needed and accompanied by clear rationale where appropriate.

---

## 🗃️ Data Model

### `SavedLocation`

Stores reusable destinations.

```text
id
name
address
latitude
longitude
createdAt
```

### `HistoryEntry`

Represents a True Distance tracking session.

```text
id
destinationName
destinationLat
destinationLng
initialDistanceMeters
finalDistanceMeters
startedAt
endedAt
savedLocationId
```

### `DistanceSnapshot`

Stores periodic distance samples associated with a tracking session.

```text
id
historyEntryId
timestamp
elapsedPercent
distanceMeters
```

### `AppSettings`

Stores application preferences such as:

```text
theme
unit
decimalPrecision
autoMetersUnder1km
gpsAccuracyMode
updateFrequencySeconds
backgroundTrackingEnabled
```

Runtime state for an active tracking session links the currently running process to its corresponding persisted history entry.

> **Note:** The source specification identifies a naming mismatch between the design-level `HistoryEntry` concept and an existing repository implementation that uses `Trip`. This should be reconciled during development to avoid ambiguity.

---

## 🏗️ Architecture and Technical Direction

The documented technical direction includes:

- Android application architecture
- MVVM presentation structure
- Fragments in the existing reference implementation
- Repository pattern
- Room / SQLite or equivalent local persistence
- Dependency injection through Hilt/Dagger in the existing implementation
- Continuous location updates rather than one-shot location retrieval
- Foreground services for supported background tracking
- Google Maps SDK
- Places Autocomplete
- Geocoding for destination/address resolution
- Dynamic Light / Dark / System theming

The existing repository referenced by the specification contains components such as location utilities, database entities and DAOs, repositories, view models, and screen-level UI modules.

---

## 🎨 Design System

True Distance follows a soft, card-based visual language.

### Design Principles

- Soft neutral backgrounds instead of stark white
- Light rounded cards with generous corner radius
- Elevated dark surfaces in dark mode
- Strong, dark pill-shaped primary actions
- Minimal line-style iconography
- Large, bold numerals for important measurements
- A single warm accent for active/live states
- Fixed bottom navigation
- Slightly desaturated Google Map styling to better fit the app aesthetic

The final color palette, logo artwork, and exact icon assets are intentionally left for a dedicated design pass.

---

## 📐 Responsive Design

The application should support:

- Small phones
- Standard phones
- Large phones
- Foldables
- Tablets
- Portrait orientation
- Landscape orientation

Layouts should avoid fixed positioning and adapt using responsive Android layout techniques such as constraints, window size classes, or equivalent Compose patterns.

Wide layouts may use alternate arrangements, such as placing map and statistics side-by-side where appropriate.

Minimum design testing targets include approximately:

- 360dp-width phones
- 412dp-width phones
- 800dp+ tablet layouts

---

## 🚀 Release Roadmap

### V1 — True Distance Core

Primary release scope:

- True Distance tab
- Destination search and selection
- Saved Locations
- Live straight-line tracking
- Google Maps visualization
- Distance History
- Settings
- Theme and unit preferences
- Background tracking support
- Branding and splash screen
- Speedometer placeholder

### V2 — Speedometer

The Speedometer tab evolves into a complete trip-tracking feature with:

- Live speed gauge
- Speed in km/h or mph using the app-wide unit setting
- Start, Pause/Resume, and Stop controls
- Start time and elapsed moving time
- Actual traveled distance
- Average speed
- Filtered maximum speed
- Start and current location markers
- GPS breadcrumb polyline showing the actual path
- Trip summary
- Local trip persistence
- Past Trips history
- Map-based trip path review

Paused periods do not contribute to elapsed moving time, traveled distance, average speed, or maximum speed accumulation.

GPS spike filtering is planned to reject implausible speed jumps from statistical calculations.

Trips receive automatically generated names such as:

- `Trip to <Destination>`
- `Trip on <Date>` when no meaningful destination can be resolved

V2 also documents process-death recovery for unfinished trips, allowing the user to resume or explicitly discard recovered state.

### V3 — Convenience and Platform Features

Planned features include:

- Configurable sticky/removable tracking notification behavior
- 4×2 home-screen widget
- Android app icon long-press shortcuts
- Version screen with structured changelog history
- Speedometer auto-pause when stationary

The architecture should avoid blocking these future additions.

---

## 🛠️ Development Notes

An existing partial implementation is documented with the package:

```text
sabuj.m.truedistance
```

The reference codebase includes an MVVM-oriented structure with local persistence, dependency injection, repositories, view models, and utilities for distance calculation, GPS status, permissions, location tracking, maps, and network status.

Known gaps documented against the specification include:

- Missing background-location and notification capabilities required for the full background-tracking experience
- Missing foreground-service declaration for background tracking
- No home-screen widget implementation yet
- No Android app shortcuts implementation yet
- Speedometer functionality deferred to V2
- An existing destination-reached helper whose final product behavior should be explicitly defined

---

## ❓ Open Decisions

The following items remain intentionally undecided:

- Onboarding and permission-explainer flow
- Final color palette
- Final logo and icon artwork
- Historical distance replay behavior
- CSV/JSON data export
- True Distance process-death recovery
- Final behavior when a destination-reached threshold is triggered
- Exact implementation thresholds for snapshot recording and speed spike filtering

---

## 📄 Project Status

This repository is based on a detailed application design and implementation specification. The core roadmap is phased so the True Distance experience can be delivered first without blocking later Speedometer and platform enhancements.

**Current intended progression:**

```text
V1 → True Distance
V2 → Speedometer
V3 → Widgets, shortcuts, enhanced notifications, changelog, auto-pause
```

---

## 🤝 Contributing

Before implementing a feature, keep the documented release scope and architecture in mind.

For changes:

- Update existing files with focused modifications
- Add new files with clearly defined responsibilities
- Delete obsolete files only when their replacement or removal is understood
- Keep reusable business logic outside UI code where practical
- Preserve the separation between persisted data, runtime tracking state, repositories, and presentation logic

---

## 📌 License

TBD
---

### Source Basis

This README was prepared from the project's supplied design specification and preserves its documented scope, phased roadmap, architecture direction, data model, feature requirements, and known open questions.
