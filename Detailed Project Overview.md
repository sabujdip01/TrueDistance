# True Distance — App Design Doc

> This document is the single source of truth for building the "True Distance" Android
> app. It is written to be detailed enough that an AI coding agent or developer can
> implement the app directly from this spec, with minimal additional clarification.

---

## 1. Overview
**True Distance** is an Android app (`v2.0.0 (2)`) that shows the straight-line ("as the crow flies")
distance between the user's current location and a chosen destination, plotted live on
Google Maps with markers and a connecting line, alongside a complete live Speedometer trip tracking system.

## 2. Core Concept
- Get current location from system (GPS/network), tracked live.
- Get destination from user (search, map tap, or a saved location).
- Draw a straight line (great-circle line, **not** a driving route) between the two
  points on a Google Map.
- Show markers on both points.
- Continuously show the computed straight-line distance while tracking is active.
- Speedometer: Record live trips with continuous breadcrumb polyline, floating speed counter, stats card, interactive notifications, and expandable past trip route snapshots.

---

## 3. Screen Inventory (quick reference)
1. Splash Screen (system-level, cold start)
2. True Distance → Main Screen
3. True Distance → Saved Locations
4. True Distance → Distance History
5. True Distance → Tracking Screen
6. Speedometer → Speedometer Screen (V2 - Complete)
7. Speedometer → Past Trips Screen (V2 - Complete)
8. Settings (includes About sub-section: Privacy Policy, Version, Credits)

---

### 4. App Navigation Structure

The app uses a **Material3 Floating Pill Navigation Bar with 3 tabs** (36dp corner radius CardView container with 12dp shadow elevation), visible across the main screens:

| Icon | Tab Name | Purpose |
|---|---|---|
| 📍 (`ic_tab_distance`) | **True Distance** | Core feature — straight-line tracking, saved locations & history |
| ⏱️ (`ic_speedometer`) | **Speedometer** | Live trip tracking map, floating speed readout, stats card & past trips |
| ⚙️ (`ic_settings`) | **Settings** | App preferences, theming, GPS mode, units, and About section |

- **Active Tab Style**: Filled primary violet (`#7C4DFF`) capsule pill indicator with white icon/text (`text_on_primary`).
- **Inactive Tab Style**: Circular icon-only layout in secondary grey (`#90A4AE`).
- **Smart Reselection**: Tapping the active tab or switching tabs automatically pops the back stack back to the tab's root destination (`DistanceFragment`, `SpeedometerFragment`, `SettingsFragment`).
- **Floating Pill Container**: 20dp horizontal/bottom margin, 36dp corner radius, 12dp card shadow.

---

## 5. Branding

### 5.1 App Logo
- Custom app icon with adaptive layers (`ic_launcher_foreground`, `ic_launcher_background`).

### 5.2 Splash Screen
- Branded splash screen on cold start with 32sp bold title and version metadata.
- Theme-aware background (`background_soft`).

---

## 6. Screens

### 6.1 Tab 1: True Distance

#### 6.1.1 Main Screen (default screen on this tab)
Layout:
1. **Header row**: Two circular icon chips (`bg_icon_chip.xml`, 48x48dp):
   - **Saved Locations icon** (`ic_bookmark`) → opens Saved Locations screen (§6.1.2).
   - **Distance History icon** (`ic_history`) → opens Distance History screen (§6.1.3).
2. **Location selection card** (`CardView`, 20dp radius, 4dp elevation):
   - Search box with Places Autocomplete.
   - Map picker icon (`ic_map_pin`) & Saved locations dropdown (`ic_chevron_down`).
   - "From: Current Location" and "To: [Destination]" labels.
   - **"Start Tracking" button** (12dp radius, `primary_violet`).

#### 6.1.2 Saved Locations Screen (full screen)
- Header: title "Saved Locations", back button.
- **Card layout (80% / 20% split)**:
  - **80% Left (Content, 20dp padding)**:
    - Row 1: Location Name (`17sp` bold, deep card color in light mode: `#00695C` Mint, `#6A1B9A` Lavender, `#BF360C` Peach).
    - Row 2: Address (`13sp`, same hue at 85% opacity in light mode).
  - **20% Right (Action)**: Centered delete button (`36x36dp`).
- Card backgrounds: Pastel 45° gradients cycling Mint / Lavender / Peach (24dp corner radius). In dark mode, deep desaturated gradients with high-contrast text (`text_charcoal` / `text_gray_purple`).
- Floating Action Button (FAB) at bottom-right to add new location via Places search or Map picker.
- Tapping card body sets destination on Main Screen.

#### 6.1.3 Distance History Screen (full screen)
- Header: title "Distance History", back button, "Clear All" in overflow menu.
- Date sections: **Today / Yesterday / Older**.
- **Card layout (80% / 20% split)**:
  - **80% Left (Content, 20dp padding)**:
    - Row 1: Destination Name (`17sp` bold) + Tracked Distance (`13sp`, initial distance − final distance) in deep card hue.
    - Row 2: Start Timestamp (`13sp`) | Stop Timestamp (`13sp`) | Elapsed Duration (`13sp`) in matching hue at 85% opacity.
  - **20% Right (Action)**: Centered delete button (`36x36dp`).
- Card backgrounds: Pastel 45° gradients cycling Blue (`#BBDEFB` → `#E3F2FD`) / Peach (`#FFCCBC` → `#FFF3E0`) / Lavender (`#E1BEE7` → `#F3E5F5`). In dark mode, deep desaturated variants with high-contrast text.
- **Expanded snapshot rows (Single-card exclusive expand)**:
  - Tapping an entry expands it and collapses any previously expanded card.
  - Displays a clean **3-column table** (`12sp`):
    - Col 1: Elapsed label (`+0:00 (Start)`, `+2:30`, `+5:00 (End)`)
    - Col 2: Clock time (`9:45 PM`, centered)
    - Col 3: Distance (`12.40 KM`, right-aligned)
- **Time-Based Snapshot Logic** (`DistanceSnapshotFormatter` per `time-based-log.md`):
  - Every raw GPS fix during tracking is stored as `(timestamp, distanceMeters)`.
  - When viewing history, snapshots are derived post-hoc using duration tiers:
    - **Tier A (≥ 10 min)**: 11 marks at 0%, 10%, 20%, ..., 100% of duration.
    - **Tier B (2–10 min)**: 8 marks at 0%, 15%, 30%, 45%, 60%, 75%, 90%, 100%.
    - **Tier C (20s–2 min)**: 3 marks at 0%, 50%, 100%.
    - **Tier D (< 20s)**: 2 marks at 0%, 100%.
  - Each mark snaps to the closest real recorded GPS sample (never interpolated).

#### 6.1.4 Tracking Screen (full screen)
- **Map Framing**: Map wrapped in a `CardView` with `16dp` corner radius, `12dp` margins, and `2dp` elevation for a modern soft border.
- **Markers**:
  - **Current location**: Red marker dot / pin.
  - **Destination**: Green marker pin.
- **Camera Behavior**: Auto-fits both markers within visible map bounds (`LatLngBounds`) with dynamic padding accounting for UI overlay cards.
- **Polyline**: Thick solid 8px dark teal line (`#00796B`) connecting current location and destination.
- **Distance Readout Overlay**: Floating translucent glass card (`card_white_translucent`, 16dp radius) showing live distance in `36sp` bold numerals.
- **Auto-Stop on Arrival**: When distance to destination reaches ≤ ~10 meters, tracking automatically completes and presents an animated "Destination Reached" celebration dialog.
- **Controls**: "Stop Tracking" button (`primary_violet`, 12dp radius) placed beneath the map card.
- **Foreground Notification Sync**: Stopping tracking from the status bar notification immediately cancels the notification, resets state, and auto-navigates the Tracking screen back to Main screen.
- **Background Tracking**: When enabled in Settings (§6.3), leaving the app keeps tracking active via foreground service and persistent notification. When disabled, navigating away pauses/stops live location updates.

---

### 6.2 Tab 2: Speedometer

The Speedometer tab provides live trip tracking, real-time GPS breadcrumbs, speed metrics with spike filtering, interactive background foreground notifications, and a Past Trips history screen with expandable route map snapshots.

This tab contains two primary screens: the **Speedometer Screen** (live trip tracking) and the **Past Trips Screen** (trip log), accessible via a header history icon chip (`ic_history`).

#### 6.2.1 Speedometer Screen (`SpeedometerFragment`)
Layout structure (top to bottom):
1. **Header Row**: History icon chip button (`@id/historyButton`) navigating to the Past Trips screen (`@id/action_speedometer_to_pastTrips`).
2. **Interactive Map**: Google Map inside a 16dp rounded `CardView` frame with 12dp margins:
   - **Initial Centering**: Loads user's current location immediately on map ready (`17f` zoom) with a Red pin marker.
   - **Trip Start Zoom**: On clicking Start, zooms tightly into the user's location at street level (`18.5f`).
   - **Breadcrumb Polyline**: Traveled path polyline (`#00796B` dark teal, 8px solid) drawn continuously as the user travels.
   - **Auto-Drag & Auto-Zoom Out**: Smoothly follows movement in real time. When the route expands (>15m), camera automatically scales and zooms out to fit the full path polyline and current location with top and bottom UI overlay padding.
3. **Floating Live Speed Counter**:
   - Glassmorphic card (`card_white_translucent`, 16dp radius, 4dp elevation) floating at the top of the map.
   - Large bold speed readout (`36sp` bold) with unit label (`KM/H`, `M/H`, or `MPH` per global Settings).
   - Speed formatting: 3-digit meters per hour (`000 M/H`) under 1 KM/H; 2-decimal format (`%.2f KM/H`) at or above 1 KM/H.
4. **Recenter FAB**: Mini floating action button (`@id/recenterButton`) at bottom-right of the map to center on current location.
5. **Gradient Statistics Card** (`@id/statsCard`):
   - 24dp rounded corners, 20dp padding, pastel/dark gradient surface.
   - **Distance Covered**: 3-digit meters (`000 M`) under 1 KM; 2 decimal places (`%.2f KM`) at or above 1 KM.
   - **Average Speed & Max Speed**: Sanitized via `SpeedSpikeFilter` (deadband threshold at 0.6 m/s to eliminate stationary jitter).
   - **Start Timestamp & Elapsed Time**: Running duration (`HH:MM:SS`) excluding paused intervals.
6. **Control Action Buttons**:
   - **Idle State**: Full-width [ START ] button (`primary_violet`).
   - **Active State**: Dual-button layout with [ PAUSE / RESUME ] and [ STOP ] buttons.
   - **Trip Completion**: Tapping Stop shows a `"Trip Saved"` toast notification, resets all screen stats to initial zero values, and clears the polyline while preserving current location.

#### 6.2.2 Foreground Service & Notification Synchronization (`SpeedometerService`)
- **Dedicated Foreground Service**: Controlled via explicit actions (`ACTION_START`, `ACTION_PAUSE`, `ACTION_RESUME`, `ACTION_STOP`).
- **Persistent Notification**:
  - Notification channel: `speedometer_channel`.
  - Content text: Displays live Speed, Distance Covered, and Elapsed Time in uppercase units (`000 M/H • 000 M • 00:00:00`).
  - **Interactive Action Buttons**: `Pause` / `Resume` and `Stop` PendingIntents, fully synchronized with the app UI state via `SpeedometerStateHolder`.
- **Spike Filter** (`SpeedSpikeFilter`): Rejects physically impossible acceleration jumps (> 10 m/s²) and filters stationary noise (< 0.6 m/s) to guard `maxSpeed` from GPS jitter.

#### 6.2.3 Past Trips Screen (`PastTripsFragment`)
- **Header**: Title "Past Trips", back navigation, and overflow menu with "Clear All" confirmation dialog.
- **Card Format**: 80/20 card layout with 24dp rounded corners and pastel gradient fills.
  - **Row 1**: Start Date/Time (`17sp` bold, tone-matched) + Distance (`13sp`, 3-digit `M` / 2-decimal `KM`).
  - **Row 2**: Start Time, Elapsed Duration, and Avg Speed (`13sp` at 85% opacity).
  - **Right 20%**: Centered delete icon button.
- **Single-Card Exclusive Expand**:
  - Expanding a card displays an embedded Google Map snapshot with the full recorded route polyline, Start marker (Green), and End marker (Red).
  - Shows detailed Max Speed and End timestamp.

---

### 6.3 Tab 3: Settings

#### 6.3.1 Preferences Section
- **Theme**: Light / Dark / Follow System (default: Follow System). Applies app-wide
  immediately on change, no restart required.
- **Accuracy**: controls GPS behavior, with two related controls:
  - **GPS Accuracy Mode**: High Accuracy (GPS + network, most precise, more battery) /
    Balanced (network-based, less battery) / Device Only (GPS only). Maps to Android's
    location priority constants (e.g., `PRIORITY_HIGH_ACCURACY`,
    `PRIORITY_BALANCED_POWER_ACCURACY`).
  - **Update Frequency**: how often location updates are requested while tracking,
    e.g. selectable interval such as Every 1s / Every 3s / Every 5s / Every 10s.
    Default: a reasonable middle value (e.g., every 3s) balancing responsiveness and
    battery.
- **Unit selector**: Kilometers / Miles / Both (default: Kilometers). Applies to all distance
  displays app-wide (Main screen post-tracking readouts, Tracking screen, History).
  - Decimal precision: up to 2 decimals, user-configurable (0/1/2 decimal places).
  - Small-distance auto-format: when distance is under 1 km, automatically display in
    meters instead (e.g., "850 m" rather than "0.85 km") — toggle to enable/disable.
- **Background Tracking**: toggle — keep live tracking active (with persistent
  notification) when app is backgrounded, vs. pause when app is not in foreground.
  Default: ON.

#### 6.3.2 About Section
A distinct section (visually separated, e.g. below a divider or in its own card),
containing:
- **Privacy Policy**: link/button opening the app's privacy policy (in-app WebView or
  external browser link to a hosted privacy policy page). Content should disclose:
  fully local data storage (no account/login), what device permissions are used and
  why (Location, Notifications, Storage), and that Google Maps/Places/Geocoding APIs
  are called for map rendering and place search (subject to Google's own privacy
  policy for that portion of data flow).
- **App Version**: displays current version name + build number (e.g., "Version 1.0.0
  (1)"), read from app package info — not hardcoded.
- **Credits**: attribution section — e.g., "Built with Google Maps Platform," any
  open-source libraries used, and author/developer credit line.
- (Optional, not required for v1 but reasonable to reserve space for: "Rate this app,"
  "Send feedback," "Open source licenses" links.)

---

## 7. Features Summary

- **Current location detection**: via system location services (GPS/network), with
  permission handling and graceful denial states.
- **Destination input methods** (on Main Screen): search with autocomplete, map tap
  picker, and quick-select dropdown from Saved Locations.
- **Straight-line distance calculation**: great-circle distance (Haversine formula or
  equivalent), not driving distance — strictly straight-line for v1.
- **Live tracking**: current location, connecting line, and distance all update
  continuously while a tracking session is active; continues in background (via
  foreground service + persistent notification) unless disabled in Settings.
- **Single active destination**: only one destination trackable at a time. Starting a
  new tracking session ends any previous one (previous session is preserved in
  History).
- **Saved Locations**: persistent list of favorite/frequent destinations (name +
  location only, no categories), addable via search or map pick, reusable as a
  destination shortcut.
- **Distance History**: auto-saved log of every tracking session, grouped by date,
  viewable/replayable, deletable (single or all).
- **Units**: Kilometers / Miles / Both (default: KM), up to 2 decimal places (configurable), with
  automatic meters display for sub-1km distances.
- **Settings**: Theme, GPS Accuracy Mode + Update Frequency, Units, Background
  Tracking toggle.
- **About**: Privacy Policy, Version info, Credits.
- **Branding**: custom app icon and splash screen; consistent visual theme across all
  screens (see §9).
- **Fully local/on-device**: no login, no account, no cloud sync/backend — all data
  (Saved Locations, History, Settings) stored locally on-device.
- **Speedometer**: placeholder tab, no functionality in v1.

---

## 8. Data Model (draft)

### SavedLocation
| Field | Type | Notes |
|---|---|---|
| id | UUID / auto-increment | primary key |
| name | String | user-editable label |
| address | String | resolved/display address (may be reverse-geocoded if picked via map) |
| latitude | Double | |
| longitude | Double | |
| createdAt | Timestamp | |

### HistoryEntry (True Distance tab — §6.1.3)
| Field | Type | Notes |
|---|---|---|
| id | UUID / auto-increment | primary key |
| destinationName | String | display name/address of destination at time of tracking |
| destinationLat | Double | |
| destinationLng | Double | |
| initialDistanceMeters | Double | distance at the moment tracking started — shown on the collapsed history row |
| finalDistanceMeters | Double \| null | distance when tracking ended (null if still active) |
| startedAt | Timestamp | when tracking session began |
| endedAt | Timestamp \| null | when tracking session ended (null if still active) |
| savedLocationId | UUID \| null | optional FK if destination came from a Saved Location |

### DistanceSnapshot (True Distance tab — child of HistoryEntry, feeds §6.1.3 interval rows)
| Field | Type | Notes |
|---|---|---|
| id | UUID / auto-increment | primary key |
| historyEntryId | UUID (FK) | parent session this snapshot belongs to |
| timestamp | Timestamp | when this snapshot was recorded |
| elapsedPercent | Int (0–100) | legacy field (0 for raw GPS fixes; derived post-hoc via DistanceSnapshotFormatter) |
| distanceMeters | Double | distance-to-destination at this point |

### Trip (Speedometer tab — §6.2, replaces/parallels HistoryEntry for the Speedometer feature)
| Field | Type | Notes |
|---|---|---|
| id | UUID / auto-increment | primary key |
| startedAt | Timestamp | |
| endedAt | Timestamp \| null | null while trip in progress |
| startLat / startLng | Double | |
| endLat / endLng | Double \| null | null while trip in progress |
| distanceMeters | Double | cumulative actual-path distance (not straight-line) |
| elapsedMillis | Long | moving time only, excludes paused duration |
| averageSpeed | Double | derived (distance / elapsed moving time) or stored directly |
| maxSpeed | Double | highest instantaneous speed recorded, filtered per §6.2.5 spike filtering |
| pathPoints | List\<LatLng\> (JSON-encoded or child table) | full breadcrumb trail for map replay, §6.2.4 |
| name | String | auto-generated, "Trip to \<Destination\>" or "Trip on \<Date\>" fallback — see §6.2.6 |

### AppSettings (single row / key-value store)
| Field | Type | Default |
|---|---|---|
| theme | enum(Light, Dark, System) | System |
| unit | enum(Km, Miles, Both) | Km |
| decimalPrecision | int (0–2) | 2 |
| autoMetersUnder1km | bool | true |
| gpsAccuracyMode | enum(High, Balanced, DeviceOnly) | High |
| updateFrequencySeconds | int | 3 |
| backgroundTrackingEnabled | bool | true |

### ActiveTrackingSession (runtime state, not necessarily persisted beyond the linked HistoryEntry)
| Field | Type | Notes |
|---|---|---|
| historyEntryId | UUID | links to the HistoryEntry being live-updated |
| destinationLat/Lng | Double | |
| isBackgroundServiceRunning | bool | |

### ActiveTrip (runtime state for Speedometer — mirrors ActiveTrackingSession)
| Field | Type | Notes |
|---|---|---|
| tripId | UUID | links to the in-progress Trip |
| isPaused | bool | |
| pausedAtMillis | Long \| null | used to exclude paused duration from elapsedMillis |

---

## 9. Visual Style Guide

> **Important**: The complete, pixel-accurate visual design specification, color tokens,
> component variants, typography scale, and dark theme overrides are defined in
> [UI-design-final.md](./UI/UI-design-final.md) (Version 1.1). That document is the
> authoritative single source of truth for all UI implementation details.

Style summary:
- **Soft Aesthetic**: Blush/cream background (`background_soft` `#F9F4F8`) in light mode; warm charcoal (`#1C1B1A`) in dark mode.
- **Card-First Design**: Elevated rounded cards (24dp radius, `80/20` content/action layout split with 20dp padding) floating over the soft background.
- **Pastel Gradients**: Mint, Peach, Lavender for Saved Locations; Blue, Peach, Lavender for Distance History. In dark mode, deep desaturated variants (`drawable-night/`).
- **Tone-Matched Typography**: Card text colors dynamically use deep shades of the card hue for title row (17sp bold) and 85% opacity for secondary row (13sp) in light mode; clean high-contrast cream/gray in dark mode.
- **Map Framing**: Tracking and Map Picker maps wrapped inside soft-bordered `CardView` frames with 16dp corner radius and 12dp margins.
- **Floating Bottom Nav**: 24dp corner radius, 8dp elevation floating navigation bar with active `primary_violet` indicator.

---

## 10. Permissions

- **Location** (foreground + background): required for current-location detection and
  live tracking; background location required specifically for the optional
  background-tracking mode.
- **Notifications** (Android 13+ runtime permission, `POST_NOTIFICATIONS`): required
  for the persistent live-tracking notification during background tracking.
- **Storage**: reserved for future data export (e.g., exporting History/Saved
  Locations as CSV/JSON) — not core to v1 functionality but planned for.

---

## 11. Error Handling & Connectivity/GPS Edge Cases

The app must gracefully handle loss of internet connectivity and loss of GPS/location
availability, both **before** tracking starts and **while** a tracking session is
already in progress. These are treated as first-class states, not afterthoughts.

### 11.1 No Internet Connectivity

**Before tracking starts (Main Screen):**
- If internet is unavailable, the Main Screen's search box and map should visibly
  indicate the issue (e.g., a banner: "No internet connection — search and map require
  internet"). Map tiles may fail to load; show a placeholder/offline state instead of a
  blank/broken map view.
- Saved Locations quick-select dropdown still works (local data, no internet needed).
- Search-by-place and "pick on map" (which needs map tiles) are disabled/degraded with
  a clear inline message until connectivity returns.
- "Start Tracking" button: if the destination was chosen from Saved Locations (no
  internet needed to resolve it), tracking **can still start** without internet, since
  distance calculation itself (Haversine) requires no network — only the live map
  *rendering* needs internet. See 11.3 for how the Tracking Screen behaves in this case.

**While tracking is in progress:**
- If internet drops mid-session:
  - Live distance calculation **continues working** (it's pure math on GPS
    coordinates — no network dependency).
  - Map tiles may stop refreshing/updating (Google Maps SDK will show last-cached
    tiles or a blank/grey map area depending on cache).
  - Show a non-blocking banner on the Tracking Screen: "No internet — map may not
    update, but distance tracking continues."
  - Notification (background tracking) continues showing live distance normally,
    since it doesn't depend on map rendering.
- When internet returns, map tiles resume loading automatically; banner is dismissed.

### 11.2 No GPS / Location Unavailable

**Before tracking starts:**
- If location permission is granted but GPS/location is off at the OS level (e.g.,
  device location toggle disabled), the Main Screen should detect this and show a
  prompt/banner: "Location is off — enable it to detect your current position," with a
  direct action button that opens the system location settings.
- "Start Tracking" button should be disabled (or tapping it triggers the same
  enable-location prompt) until a valid current-location fix is available.
- If location permission itself was denied, show the standard permission-rationale/
  request flow instead (see 11.4).

**While tracking is in progress:**
- If GPS signal is lost or location becomes unavailable mid-session (e.g., entering a
  tunnel, indoors, airplane mode with GPS also off):
  - The last known distance/marker position is retained on screen (not reset to
    zero/blank) — avoids a jarring "distance disappeared" experience.
  - Show a status indicator on the Tracking Screen (e.g., a small "Signal lost —
    showing last known distance" label near the distance readout) rather than a
    blocking error.
  - The background notification (if active) should similarly indicate stale data,
    e.g., append "(last updated Xs ago)" once the gap exceeds a threshold (e.g., 15–30
    seconds without a fresh fix).
  - Tracking session does **not** auto-stop on temporary signal loss — only an
    explicit "Stop Tracking" tap (or, per Settings, backgrounding the app when
    Background Tracking is disabled) ends the session.
  - When GPS signal resumes, live updates resume automatically and the stale-data
    indicator clears.

### 11.3 Combined Case: No Internet AND No GPS
- If both are unavailable simultaneously before tracking starts, the Main Screen shows
  both relevant prompts (location + internet), and "Start Tracking" remains disabled
  until at least location is available (internet-only tracking from a Saved Location,
  per 11.1, still requires a valid current-location fix to compute distance — GPS is
  the one truly required input for the app's core function; internet is only required
  for map rendering/search).

### 11.4 Permission Denial (related edge case)
- If location permission is denied (not just GPS toggled off), show a clear
  in-app explanation of why it's needed before/alongside the system permission
  dialog, and a persistent but non-blocking state on the Main Screen if the user
  denies it (e.g., "Location permission required to use True Distance" with a button
  to open app settings) — the rest of the app (Settings, Saved Locations list
  browsing, History browsing) should remain usable even without location permission.

### 11.5 Summary Principle
- **GPS/location is the hard dependency** for the app's core function (distance
  calculation) — the app should be as resilient as possible to its temporary loss
  during an active session (retain last-known state, don't crash or reset), while
  clearly communicating degraded/stale status to the user.
- **Internet is a soft dependency** — needed for map visuals and search/autocomplete,
  but distance math itself has no internet dependency, so the app should not block
  core tracking functionality on internet availability alone.

## 12. Tech Notes (draft)

- **Internet required**: no offline mode planned — map tiles, Places Autocomplete, and
  Geocoding all require network connectivity.
- **Live tracking**: implemented via a continuous location listener/callback (e.g.
  `FusedLocationProviderClient.requestLocationUpdates`), not a one-shot
  `getLastLocation()` call. Update interval driven by the Settings "Update Frequency"
  value.
- **Background tracking**: requires a foreground service with a persistent
  notification (shows live distance, includes a "Stop Tracking" action) to keep
  location updates running when backgrounded and to comply with Android's background
  location policies (and Play Store policy requirements for background location
  usage/justification).
- **Single active destination**: state model is simple — one nullable "active
  tracking session" rather than a list/queue.
- **Straight-line only**: Directions API is not required for v1 (only Google Maps SDK,
  Places API for autocomplete, and Geocoding API for address resolution) — keeps API
  surface and cost lower.
- **Fully local storage**: Room/SQLite (or equivalent local DB) for SavedLocation,
  HistoryEntry, and AppSettings — no backend server, no login/account system, no cloud
  sync. All personal data remains on-device; only Google Maps Platform API calls leave
  the device (for map rendering/search/geocoding).
- **Splash screen**: use Android 12+ SplashScreen API with a legacy-compatible fallback
  theme for pre-API-31 devices.
- **Theming**: implement via a single theming system that supports Light/Dark/Follow
  System dynamically (e.g., Jetpack Compose `MaterialTheme` with a custom color
  scheme, or AppCompat DayNight theme if using Views) — must apply without requiring
  app restart.

### 12.4 Responsive / Adaptive Layout (all screen sizes)
The app must handle the full range of Android device sizes/densities correctly —
phones (small to large), foldables, and tablets — not just a single reference screen
size. Concretely:
- Use `ConstraintLayout` (or Compose equivalents like `BoxWithConstraints` /
  `WindowSizeClass`) rather than fixed-dp positioning, so cards/buttons/map reflow
  correctly across widths.
- Cards, buttons, and text should use `wrap_content`/`match_constraint` sizing with
  min/max constraints rather than hardcoded dp widths, so they scale sensibly on both
  small phones and large tablets.
- Use scalable pixel units (`sp`) for all text sizes (already standard practice, but
  worth stating explicitly) so text respects the user's system font-size setting
  without breaking layouts.
- For significantly larger screens (tablets, `sw600dp`+), provide alternate resource
  qualifiers (e.g., `res/layout-sw600dp/`) where a single-column phone layout would
  look sparse — e.g., the Speedometer screen's gauge + stats + map could adopt a
  two-column arrangement (stats/gauge on one side, map on the other) on wide screens,
  while remaining single-column/stacked on phones.
- The Google Map view and gauge view (custom view, §6.2.1) must both be built to
  resize fluidly with their container rather than assuming a fixed aspect ratio or
  pixel size.
- Test/design against at minimum: a small phone (~360dp width), a standard phone
  (~412dp width), and a tablet (~800dp+ width) — both portrait and landscape.
- Landscape orientation: screens with a map + stats/controls (Tracking Screen,
  Speedometer Screen) should re-flow into a side-by-side arrangement in landscape
  rather than a cramped vertical stack, where feasible.

### 12.5 Concurrent Sessions (True Distance + Speedometer)
- **Confirmed: both features can run simultaneously.** A user can have an active True
  Distance tracking session (§6.1.4) and an active Speedometer trip (§6.2.1) running
  at the same time — starting one does not block or pause the other.
- Each feature manages its own independent foreground service + location listener
  (per §6.1.4 / §6.2's background-tracking behavior) rather than sharing a single
  session object — they are logically and technically separate, just both consuming
  GPS concurrently.
- If both are active with Background Tracking enabled (§6.3.1), **two persistent
  notifications** may be visible at once (one per feature) — each with its own
  "Stop" action. This should be considered acceptable/expected behavior, not a bug.
- Battery impact: running two concurrent location listeners will use more battery
  than one. Not a blocker per the person's decision, but worth a brief mention in the
  About/Privacy Policy screen (§6.3.2) so users understand why battery use may be
  higher when both are active.

### 12.6 Process Death Recovery (Speedometer trips)
- If the app process is killed by the OS mid-trip (e.g., low memory) while a
  Speedometer trip is active, the in-progress `Trip`/`ActiveTrip` state (§8 Data
  Model) must be persisted frequently enough (e.g., on every location update, or at
  minimum every 10–30s) so it can be recovered — not just held in memory.
- On next app launch, if a persisted `ActiveTrip` is found with no corresponding
  live foreground service running, **prompt the user**: "You have an unfinished
  trip from [time] — Resume or Discard?"
  - **Resume**: restarts the foreground service/location listener and continues
    accumulating stats from the persisted state (distance/elapsed time/path so far
    are preserved).
  - **Discard**: the partial trip is either deleted entirely or saved as-is to Past
    Trips (§6.2.2) marked as incomplete/ended at the last known point — exact
    behavior TBD, but should not silently vanish without the prompt.
- This same recovery pattern is worth considering for True Distance's
  `ActiveTrackingSession` too, for consistency, even though it wasn't explicitly
  asked — flagged in §13 Open Questions below.

---

## 13. Open Questions / To Be Decided
- Onboarding flow (permission-explainer screens before system prompts) — deferred,
  decide later.
- Final color palette (specific hex values) and finalized logo/icon artwork — pending
  a dedicated design pass.
- Whether Distance History "replay" of an old entry should attempt to re-resolve
  current location live, or show a frozen snapshot of the historical line only.
- Data export feature (CSV/JSON) — Storage permission is reserved for this, but the
  feature itself is not yet scoped in detail.
- **Auto-pause when stationary** (Speedometer) — deferred to V3 per person's
  decision; would auto-pause Elapsed Time/Avg Speed accumulation when the user is
  stationary for a threshold duration, to avoid GPS-jitter drift while stopped
  (e.g., at a red light). Needs a stationary-detection threshold (speed + duration)
  when scoped.
- Whether True Distance's `ActiveTrackingSession` should get the same process-death
  recovery/resume-prompt treatment as Speedometer's `ActiveTrip` (§12.6) — not yet
  explicitly decided, but recommended for consistency.

---

## 14. V3 Features (Planned — Not in V1 or V2 Scope)

> **Phased release plan (updated)**:
> - **V1** — True Distance tab (core feature), Settings, Branding/Splash, all of §6–§12
>   as scoped for V1. Speedometer ships as a placeholder only.
> - **V2** — Full Speedometer tab implementation (§6.2: gauge, trip stats, live map
>   with actual-path polyline, Start/Pause-Resume/Stop, Past Trips screen).
> - **V3** — Everything in this section: sticky/removable tracking notification, home
>   screen widget, app icon long-press shortcuts, version screen changelog. These are
>   documented in detail now so the V1/V2 architecture can be built in a way that
>   doesn't block them (e.g., notification structure, widget-friendly data access,
>   launcher shortcuts).

### 14.1 Sticky/Persistent Tracking Notification
- While distance tracking is in progress **and** the app is in the background, a
  notification shows:
  - Current live straight-line distance (formatted per Unit settings).
  - Destination name/address.
- Notification updates continuously as distance changes (not a one-time post).
- **Sticky by default**: the notification is non-dismissable (ongoing/sticky) while
  tracking is active — matches typical Android foreground-service notification
  behavior (e.g., like a music player or navigation app).
- **User control in Settings**: a toggle to make the notification **removable/
  swipeable** instead of sticky, for users who don't want a persistent notification.
  - Note: if the user makes it removable and swipes it away while background tracking
    is enabled, the app should treat this as "stop tracking" (since Android's
    foreground-service model generally requires the notification to exist while the
    service runs) — this behavior should be clearly explained near the toggle.
- Notification includes a "Stop Tracking" action button (carried over from v1 spec,
  §6.1.4).
- Tapping the notification body opens the app directly to the active Tracking Screen.

### 14.2 Home Screen Widget (4x2)
- A 4x2-cell Android home screen widget for at-a-glance distance tracking.
- **When tracking is active**: widget shows live distance to the current destination,
  destination name, and updates periodically (widget update frequency subject to
  Android's `AppWidgetProvider` update constraints — likely won't be true real-time
  like the in-app Tracking Screen, but refreshed at a reasonable interval, e.g. every
  15–30 seconds, or event-driven from the foreground service if feasible).
- **When no tracking is active**: widget shows an idle/empty state (e.g., app logo +
  "No active tracking" + a tap target to open the app and start one).
- Tapping the widget opens the app directly to the Tracking Screen (if active) or Main
  Screen (if idle).
- Widget should follow the app's visual style (§9) — soft card background, bold
  distance numeral, consistent with in-app design language.

### 14.3 Home Screen App Icon Long-Press Shortcuts (App Shortcuts)
- Long-pressing the app icon on the Android home screen/launcher shows Android's
  standard app shortcuts menu, with:
  - **True Distance** shortcut — icon matches the bottom nav "True Distance" icon;
    opens directly to the True Distance tab (Main Screen).
  - **Speedometer** shortcut — icon matches the bottom nav "Speedometer" icon; opens
    directly to the Speedometer tab.
  - Standard Android **"Uninstall"** option — this is a system-provided shortcut (not
    custom-built), appears automatically as part of Android's long-press app icon
    menu on supported launchers; no custom implementation needed beyond standard app
    manifest/shortcut setup.
- Implemented via Android's Shortcuts API (static shortcuts declared in
  `shortcuts.xml`, or dynamic shortcuts if shortcut availability should depend on
  runtime state, e.g., hiding "True Distance" shortcut variant if tracking is already
  active vs. idle — TBD at implementation time).
- Icons for these shortcuts should directly reuse/match the bottom nav bar icons for
  visual consistency (§4 Navigation Structure, §9 Visual Style Guide).

### 14.4 Version Screen — Feature Changelog
- Enhance the existing About → Version display (§6.3, Settings) so that, in addition
  to showing the current version number/build, it also shows a **changelog summary**
  for that version:
  - Format: version number/date header, followed by a short bulleted list of
    **Added** and **Removed** (and optionally **Changed**/**Fixed**) items for that
    release.
  - Example structure:
    ```
    Version 2.0.0 — [release date]
    Added:
      • Sticky tracking notification with live distance
      • 4x2 home screen widget
      • Home screen shortcuts (True Distance / Speedometer)
    Changed:
      • Version screen now shows release notes
    ```
  - Should support showing history of past versions' changelogs too (not just the
    current one), e.g., a scrollable list grouped by version — exact UI (expandable
    list vs. simple scroll) TBD at implementation time.
  - Changelog content should be stored as structured local data (e.g., a bundled
    JSON/resource file per release) rather than hardcoded UI strings, so it's easy to
    append new entries per release without redesigning the screen.

### 14.5 Speedometer Auto-Pause When Stationary
- Deferred from V2 to V3 per person's decision (§12.6/§13 note).
- When implemented: auto-pause Elapsed Time/Distance/Avg Speed accumulation
  (mirrors manual Pause, §6.2.3) when the user's speed stays below a small threshold
  (e.g., <1 km/h) for a minimum duration (e.g., 10–15 seconds) — auto-resumes once
  movement resumes above threshold.
- Should be a Settings toggle (on/off), since some users may prefer manual-only
  pause control.

### 14.6 V3 Tech/Architecture Notes
- The v1 foreground-service + notification architecture (§12 Tech Notes) should be
  designed with v2's sticky/removable toggle in mind — i.e., notification importance/
  behavior (`setOngoing()` true/false) should be a configurable parameter from the
  start, not hardcoded, even if the Settings toggle itself ships in v2.
- The widget (13.2) will need a lightweight way to read "current tracking state +
  live distance" from the app's local data layer — worth designing the v1 data layer
  (§8 Data Model, `ActiveTrackingSession`) so it's already accessible to a future
  `AppWidgetProvider`/`RemoteViews` implementation without major rework (e.g., via
  `SharedPreferences`/DataStore for the widget-relevant subset of state, since widgets
  can't easily query a full Room DB synchronously).
- App Shortcuts (13.3) require API 25+ (`ShortcutManager`); confirm v1's minSdkVersion
  is compatible or define a graceful fallback for lower API levels.


---

## 15. Codebase Status & Development Workflow

### 15.1 Existing Repository
- An existing partial implementation exists: repo **TrueDistance**
  (github.com/sabujdip01/TrueDistance), package `sabuj.m.truedistance`.
- Architecture in place: MVVM with Fragments, Room database, Hilt/Dagger DI
  (`DatabaseModule`), Repository pattern (`SavedLocationRepository`,
### 15.1 Implemented Architecture & Source Files
- **Application & Activities**:
  - `MainActivity`, `SplashActivity`, `TrueDistanceApp`
- **Database (Room v2)**:
  - Entities: `SavedLocation`, `HistoryEntry`, `DistanceSnapshot`, `Trip`
  - DAOs: `SavedLocationDao`, `HistoryEntryDao`, `DistanceSnapshotDao`, `TripDao`
  - Database: `TrueDistanceDatabase` (Room v2, with fallbackToDestructiveMigration)
- **Repositories & DI**:
  - `SavedLocationRepository`, `HistoryRepository`, `TripRepository`, `SettingsRepository`
  - `DatabaseModule`
- **Background Foreground Services**:
  - `TrackingService` (True Distance tracking, notification channel `tracking_channel`)
  - `SpeedometerService` (Speedometer trip tracking, notification channel `speedometer_channel`)
  - State Holders: `TrackingStateHolder`, `SpeedometerStateHolder`
- **UI & Presentation**:
  - Distance: `DistanceFragment`, `DistanceViewModel`, `TrackingFragment`, `TrackingViewModel`
  - Saved Locations: `SavedLocationsFragment`, `SavedLocationAdapter`, `SavedLocationsViewModel`
  - History: `HistoryFragment`, `HistoryAdapter`, `HistoryViewModel`
  - Speedometer: `SpeedometerFragment`, `SpeedometerViewModel`, `PastTripsFragment`, `PastTripsAdapter`, `PastTripsViewModel`
  - Settings: `SettingsFragment`, `SettingsViewModel`
  - Map Picker: `MapPickerFragment`
- **Utilities**:
  - `DistanceCalculator`, `DistanceSnapshotFormatter`, `SpeedSpikeFilter`, `LocationTrackingHelper`, `NotificationHelper`, `MapUtils`, `GpsStatusHelper`, `NetworkStatusHelper`

### 15.2 Completed Releases
- **V1 (Completed)**: Core True Distance tracking, Saved Locations, Distance History with time-based tier snapshots, full DayNight theme support, and 80/20 card design.
- **V2 (Completed)**: Speedometer live tracking, breadcrumb polyline, floating speed counter, stats card, `SpeedometerService` with interactive notification controls, and Past Trips screen with expandable route map snapshot.
- **V3 (Planned Roadmap)**: Home screen widgets (4x2), Dynamic App Shortcuts, auto-pause on stop detection.
