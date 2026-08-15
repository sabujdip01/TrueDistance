# True Distance — App Design Doc

> This document is the single source of truth for building the "True Distance" Android
> app. It is written to be detailed enough that an AI coding agent or developer can
> implement the app directly from this spec, with minimal additional clarification.

---

## 1. Overview
**True Distance** is an Android app that shows the straight-line ("as the crow flies")
distance between the user's current location and a chosen destination, plotted live on
Google Maps with markers and a connecting line. A secondary Speedometer feature is
planned for a future release (placeholder in v1).

## 2. Core Concept
- Get current location from system (GPS/network), tracked live.
- Get destination from user (search, map tap, or a saved location).
- Draw a straight line (great-circle line, **not** a driving route) between the two
  points on a Google Map.
- Show markers on both points.
- Continuously show the computed straight-line distance while tracking is active.

---

## 3. Screen Inventory (quick reference)
1. Splash Screen (system-level, cold start)
2. True Distance → Main Screen
3. True Distance → Saved Locations
4. True Distance → Distance History
5. True Distance → Tracking Screen
6. Speedometer → Speedometer Screen (V2)
7. Speedometer → Past Trips Screen (V2)
8. Settings (includes About sub-section: Privacy Policy, Version, Credits)

---

## 4. App Navigation Structure

The app uses a **bottom footer navigation bar with 3 tabs**, always visible at the
bottom of the screen (except possibly on the full-screen Tracking screen — see 5.1.4):

| Icon | Tab Name | Purpose |
|---|---|---|
| 📍 (location/ruler icon) | **True Distance** | Core feature — the app's main function |
| ⏱️ (speed/gauge icon) | **Speedometer** | Placeholder for now, built later |
| ⚙️ (gear icon) | **Settings** | App preferences and About section |

- Active tab is visually highlighted (per style guide — dark pill background behind the
  active icon, matching the reference UI kit's nav bar treatment).
- Navigation state persists app data (switching tabs does not reset in-progress
  selections on the True Distance tab, e.g., a destination typed into the search box).
- Bottom nav bar is a standard fixed bottom bar (not floating), consistent across all
  three tabs.
- **Active-tracking indicator badge (V2 scope)**: since True Distance tracking and a
  Speedometer trip can each run independently in the background (§12.5 Concurrent
  Sessions), the bottom nav icon for a tab with an active background session shows a
  small badge/dot (e.g., a small colored dot in the corner of the tab icon, using the
  app's accent color per §9 Visual Style Guide) — so the user doesn't forget a session
  is silently running and consuming battery/location. Badge disappears when that
  tab's session is stopped. Applies independently to the True Distance icon and the
  Speedometer icon (both, one, or neither may show a badge at any given time).

---

## 5. Branding

### 5.1 App Logo
- Custom app icon required (not a default/placeholder icon).
- Icon concept: should visually represent "distance" / "straight line between two
  points" — e.g., a stylized pin-to-pin line, a compass/ruler motif, or two dots
  connected by a dashed/solid line. Final artwork TBD in a design pass, but the concept
  must clearly relate to distance/location, not be generic.
- Icon needed in standard Android adaptive icon format (foreground + background layers)
  plus all required density buckets (mdpi through xxxhdpi) and a Play Store listing
  icon (512x512).

### 5.2 Splash Screen
- Branded splash screen shown on app cold start.
- Contains: app logo (centered), app name "True Distance" (below or beside logo).
- Background: matches app's primary theme color (light/dark aware — see §9 Visual
  Style Guide).
- Uses Android 12+ SplashScreen API for proper system-level splash behavior (with a
  legacy fallback splash Activity/theme for older Android versions if targeting below
  API 31).
- Duration: brief, just long enough to initialize app state (location permission
  check, theme load, DB init) — not an artificial delay.

---

## 6. Screens

### 6.1 Tab 1: True Distance

This tab has its own internal navigation: a **Main Screen** that links out to two
full-screen destinations (**Saved Locations** and **Distance History**) via icons, plus
a **Tracking Screen** reached after starting tracking.

#### 6.1.1 Main Screen (default screen on this tab)
Layout, top to bottom:
1. **Header row**: Two icon buttons, top-of-screen (per reference style — icons in
   rounded card/chip buttons, top corners):
   - **Saved Locations icon** (e.g., bookmark/star icon) → opens Saved Locations screen
     (full screen, §6.1.2).
   - **Distance History icon** (e.g., clock/history icon) → opens Distance History
     screen (full screen, §6.1.3).
2. **Location selection card** (main white/light rounded card, central focus of the
   screen):
   - **Destination input field** — combined control that supports:
     a. **Search box** with place autocomplete (type-ahead, Places API) — primary
        input method, shown by default.
     b. **Map picker icon/button** next to the search box — opens a map view where the
        user taps a point to drop a pin as the destination.
     c. **Saved Locations dropdown** — a dropdown/chevron control on the same card that
        lets the user quickly pick from their saved locations without leaving this
        screen (separate from the full Saved Locations screen, this is a quick-select
        shortcut).
   - **Labels**: once a destination is chosen (by any method), the card shows two
     clearly labeled rows:
     - "From: [Current Location]" (label for the origin point — always current
       location, not user-editable as text, but shown for clarity)
     - "To: [destination name/address]" (label for the chosen destination)
   - **"Start Tracking" button** — primary call-to-action button on this card. Disabled
     (visually greyed out) until a valid destination is selected. Tapping it:
     - Validates location permission is granted (prompts if not).
     - Navigates to the Tracking Screen (§6.1.4) and begins live tracking.
3. Rest of screen (below the card): may show recent/quick-glance content in later
   iterations (e.g., a mini list of recent history) — not required for v1, card is the
   primary content.

#### 6.1.2 Saved Locations Screen (full screen)
- Header: title "Saved Locations", back button to return to Main Screen.
- List of saved locations, each row shows: name (editable), address/coordinates
  summary, delete icon/swipe-to-delete.
- Floating action button (FAB) or header "+" button to add a new saved location:
  - Opens an "Add Saved Location" flow with two input methods:
    a. Search place with autocomplete (Places API), OR
    b. Pick a point on the map (tap to drop pin).
  - After picking a location, user enters/edits a name for it, then saves.
- Tapping a saved location row (not the delete icon) → returns to Main Screen with that
  location pre-filled as the destination (ready to tap "Start Tracking").
- Data model: minimal — name + location (lat/lng + resolved address string). No
  categories/tags in v1 (kept intentionally simple per requirements).

#### 6.1.3 Distance History Screen (full screen)
- Header: title "Distance History", back button to return to Main Screen.
- Auto-saved every time tracking is started for a destination (no manual "save" step).
- Entries grouped by date sections: **Today / Yesterday / Older** (Older may be further
  broken into "This Week" / "This Month" / etc. if the list grows long).
- **Each entry (collapsed/summary row) shows**: Date, destination name/address,
  **Initial Distance** (the straight-line distance at the moment tracking started),
  and a delete button (swipe-to-delete or inline icon).
- **Expanding an entry reveals interval-based distance snapshots** for that session —
  since distance-to-destination changes continuously as the user moves, the app
  records/derives distance at time intervals through the session:
  - **Start time**: distance at tracking start.
  - **~10% of elapsed time**: distance at that point.
  - **~20% of elapsed time**: distance at that point.
  - ... continuing at ~10% intervals through the session ...
  - **End time**: distance when tracking was stopped (or destination reached).
  - **Smart interval count**: the number of snapshot rows shown must adapt to session
    duration so short sessions don't show a wall of near-identical rows. Suggested
    rule (exact thresholds tunable at implementation time; same logic/helper should be
    shared with §6.2's trip-summary formatting if a similar need arises there):
    - Session < 1 minute: show only Start + End (2 rows).
    - Session 1–5 minutes: show Start, 50%, End (3 rows).
    - Session 5–20 minutes: show Start, 25%, 50%, 75%, End (5 rows).
    - Session > 20 minutes: show Start, every 10% (10 intermediate points), End
      (12 rows) — full detail as originally specified.
    - This logic should live in a single reusable helper (e.g.
      `DistanceSnapshotFormatter`) so thresholds are easy to tune without touching UI
      code.
  - Snapshot recording: rather than storing every raw GPS update, the app should
    periodically record a `(timestamp, distanceMeters)` snapshot during an active
    tracking session (e.g., at a fixed cadence such as every 10–30 seconds, or
    computed retroactively by resampling raw location updates at ~10% elapsed-time
    marks once the session ends) — exact recording strategy TBD at implementation,
    but the display logic above is the target output either way.
- "Clear All" option (with confirmation dialog) available in the header overflow menu.

#### 6.1.4 Tracking Screen (full screen, launched from "Start Tracking")
- Full-screen Google Map view.
- **Current location marker** (live, updates continuously as user moves).
- **Destination marker** (static, at the chosen destination).
- **Straight line (polyline)** connecting the two markers — re-drawn live as current
  location updates.
- **Distance readout**: prominent card/banner (top or bottom overlay on the map)
  showing live-updating distance, formatted per unit settings (§6.3, and see §7 Units
  logic).
- **Recenter button** (FAB) to re-center map on current location.
- **Start / Stop controls placed directly under the map** (not floating over it) —
  this placement is intentional for UI consistency with the Speedometer tab's control
  layout (§6.2.1), so both tabs feel like the same app:
  - **Stop Tracking button** — ends the live tracking session, returns to Main Screen.
    The completed/ended session remains in Distance History (with its interval
    snapshots, §6.1.3).
- If Background Tracking is enabled in Settings (§6.3): leaving this screen (e.g.
  pressing Home) does not stop tracking — a persistent notification continues showing
  live distance, with a "Stop Tracking" action directly in the notification.
- If Background Tracking is disabled: navigating away from this screen pauses/stops the
  live location updates (tracking effectively ends).

---

### 6.2 Tab 2: Speedometer

> **Release phasing**: this tab ships as a lightweight placeholder in **V1** (icon +
> "Coming soon" text) and receives its full implementation, detailed below, in **V2**
> (see §14 for the updated phased-release plan: V1 = True Distance core, V2 =
> Speedometer, V3 = sticky notification / widget / shortcuts / changelog).

This tab has two screens: the main **Speedometer Screen** (live trip tracking) and a
**Past Trips Screen** (trip history), reached via a header icon — mirroring the True
Distance tab's Saved Locations / History icon pattern (§6.1.1) for UI consistency.

#### 6.2.1 Speedometer Screen (default screen on this tab, V2)
Layout, top to bottom (exact vertical order/sizing may adapt per screen size — see
§12.4 Responsive/Adaptive Layout):
1. **Header row**: icon button → opens Past Trips Screen (§6.2.2).
2. **Speed Gauge**: a medium-large gauge (circular or semi-circular analog style)
   showing current speed, live-updating.
   - Numeric speed value shown at gauge center/prominently, up to 2 decimal places.
   - Unit comes from the app-wide Unit setting (§6.3.1) — km/h or mph — not a
     separate speedometer-only unit setting.
   - Gauge needs a sensible max-scale so it doesn't clip/break visually at high
     speeds (e.g., auto-scaling, or a fixed sane upper bound such as 180 km/h /
     120 mph) — exact scaling behavior TBD at implementation.
3. **Trip stats**, shown as small stat cards/labels (per §9 Visual Style Guide):
   - **Start Time** — timestamp the current trip began.
   - **Elapsed Time** — running trip duration (pauses when trip is paused, §6.2.3).
   - **Distance** — cumulative distance actually traveled (real path, not straight
     line — see map below), formatted per Unit settings.
   - **Average Speed** — total distance ÷ elapsed moving time.
   - **Max Speed** — highest instantaneous speed recorded so far this trip.
4. **Map**: full Google Map view showing:
   - Marker for the trip's **starting location** (fixed once trip starts).
   - Marker for **current location** (live-updating).
   - A **polyline following the actual road/path traveled** — this differs from the
     True Distance tab's straight-line polyline; here the line is the real GPS
     breadcrumb trail of the trip, updated as the trip progresses.
5. **Control buttons, directly under the map** (same placement principle as the True
   Distance Tracking Screen, §6.1.4):
   - **Start** — begins a new trip: starts GPS tracking, resets all stats to zero,
     records start time/location. Disabled while a trip is already in progress or has
     ended but not yet reset.
   - **Pause / Resume** — single toggling button:
     - *Pause*: freezes Elapsed Time, Distance, Average, and Max Speed accumulation
       (GPS listener may keep running for position awareness, but stats stop
       updating).
     - *Resume*: continues accumulation from where it left off.
     - Disabled when no trip is in progress (before Start or after Stop).
   - **Stop** — ends the trip:
     - Shows a trip summary (final Distance, Elapsed Time, Average Speed, Max Speed,
       start/end time, start/end location) — inline on this screen or a brief summary
       state/dialog, exact presentation TBD.
     - Saves the completed trip to local storage (Trip entity, §8 Data Model),
       including its full path polyline.
     - Disabled when no trip is in progress.

#### 6.2.2 Past Trips Screen (full screen, V2)
- Header: title "Past Trips", back button to Speedometer Screen.
- List of past trips, **most recent first**.
- Each entry shows: trip start time, end time, start location, end location,
  distance, elapsed time, max speed, average speed.
- Delete button (swipe-to-delete or inline icon) per entry; optional "Clear All" in
  header overflow menu, matching the True Distance History screen's pattern (§6.1.3).
- Tapping an entry may show the trip's actual road-path polyline replayed on a map
  (reusing the stored full path from §6.2.4) — exact interaction (inline expand vs.
  separate detail screen) TBD at implementation.
- **Note**: unlike True Distance's Distance History (§6.1.3), Past Trips does **not**
  use the interval-snapshot (10%/20%/.../end) format — that format is specific to
  True Distance History. Past Trips shows standard start/end summary stats only.

#### 6.2.3 Pause Behavior (detail)
- While paused: Elapsed Time stops incrementing; Distance/Average/Max Speed stats
  freeze. The app must not accumulate distance from GPS jitter that occurs while
  paused.
- The current-location marker on the map may still update live while paused (so the
  user can see where they are), but the traveled-path polyline must **not** extend
  during the paused interval — avoids drawing a route segment for time the user
  wasn't actively on-trip.

#### 6.2.4 Trip Data Captured (feeds §8 Data Model)
Per trip, at minimum:
- Start time, end time
- Start location (lat/lng), end location (lat/lng)
- Total distance traveled (actual path, meters)
- Elapsed/moving time (excludes paused duration)
- Average speed, max speed (see 6.2.5 for spike filtering on max speed)
- Full path polyline (ordered list of lat/lng points captured during the trip) — used
  to redraw the actual route on a map when reviewing a past trip, not just summary
  numbers.
- **Trip name** (auto-generated, see 6.2.6) — shown in Past Trips (§6.2.2) list rows.

#### 6.2.5 Max Speed Spike Filtering (V2 scope)
Raw GPS speed readings can momentarily spike due to signal jumps/multipath
reflections, which would otherwise corrupt the recorded Max Speed. To guard against
this:
- Before accepting a new instantaneous speed reading as a candidate for Max Speed,
  sanity-check it against what's physically plausible given the time since the last
  reading — e.g., reject a reading if it implies an acceleration beyond a reasonable
  threshold (a tunable constant, e.g., ~10 m/s² as a generous upper bound for typical
  vehicle/foot travel) compared to the previous accepted speed.
- Rejected/implausible readings should still be allowed to update the live gauge
  display cosmetically if desired, but must **not** be written into Max Speed or
  factored into Average Speed/Distance calculations.
- This filtering logic should live in a single reusable helper (e.g.
  `SpeedSpikeFilter`) alongside `DistanceSnapshotFormatter` (§6.1.3), so the exact
  threshold is easy to tune without touching tracking/service code.

#### 6.2.6 Auto-Generated Trip Names (Past Trips, §6.2.2)
- Trips do not have manual user-editable names for now. Instead, each trip is
  auto-labeled at save time using one of two templates, chosen based on available
  data:
  - **"Trip to \<Destination\>"** — used when a recognizable destination/place name can
    be resolved (e.g., via reverse-geocoding the trip's end location) at the time the
    trip is stopped.
  - **"Trip on \<Date\>"** — fallback used when no meaningful destination name is
    available (e.g., reverse geocoding fails, no internet, or the end point resolves
    to a generic/unnamed area) — uses the trip's start date, formatted per the
    device's locale/date settings.
- This naming logic should live in its own small helper (e.g. `TripNamer`) so the
  destination-resolution vs. date-fallback decision is centralized and easy to test.

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
- **Unit selector**: Kilometers / Miles / Both (default: Both). Applies to all distance
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
- **Units**: Kilometers / Miles / Both, up to 2 decimal places (configurable), with
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
| elapsedPercent | Int (0–100) | approx. % of session elapsed time at recording (0 = start, 100 = end) |
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
| unit | enum(Km, Miles, Both) | Both |
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

Style is based on a soft, card-based, pastel aesthetic (referenced from a provided UI
kit sample) — **not a literal clone of any specific app**, but matching this design
language:

- **Background**: soft, muted neutral tone (e.g., blush/cream in light mode) rather
  than stark white — gives a calm, premium feel. Dark mode equivalent: deep neutral
  charcoal (not pure black), maintaining the same soft/premium feel.
- **Cards**: white (light mode) / elevated dark-grey (dark mode) rounded-corner cards
  (large radius, ~16–24dp) with soft drop shadows, floating above the background.
  Primary content (location card, list rows, distance readout) lives inside these
  cards.
- **Accent/active elements**: dark pill-shaped buttons/badges (near-black in light
  mode) used for primary actions and the active bottom-nav icon highlight — creates
  strong contrast against the soft background.
- **Typography**: clean sans-serif, bold large numerals for key data (e.g., distance
  value should be large/bold like the reference's "$231.68" balance treatment),
  medium-weight labels for secondary text.
- **Iconography**: minimal line icons (thin stroke weight), consistent style across
  nav bar and in-card action icons (bookmark for Saved Locations, clock for History,
  map-pin for location, gear for Settings).
- **List rows**: rounded thumbnail/icon on the left, title + subtitle stacked, value/
  metadata right-aligned — mirrors the reference's transaction-list row pattern,
  adapted to show destination name + address (left) and distance + date (right).
- **Bottom nav bar**: fixed, 3 icons (per §4), active tab shown inside a dark pill
  background, inactive tabs plain/muted icon color.
- **Color accents**: a single warm accent color (e.g., a soft coral/red, matching the
  reference's chart-line accent) reserved for live/active states — e.g., the live
  tracking line on the map, "recording" indicators, or highlight badges — used
  sparingly, not as a dominant color.
- **Map styling**: custom Google Map style (via Maps SDK style JSON) to desaturate
  default Google Maps colors slightly so the map fits the app's soft palette rather
  than looking like stock Google Maps.
- (Exact hex palette and finalized icon/logo artwork to be produced in a dedicated
  design pass before development — this section defines direction/constraints for
  that pass.)

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
  `SettingsRepository`, `TripRepository`).
- Existing source files (as of last review):
  - `MainActivity`, `TrueDistanceApp`
  - `database/`: `SavedLocation`, `SavedLocationDao`, `Trip`, `TripDao`,
    `TrueDistanceDatabase`
  - `di/DatabaseModule`
  - `repository/`: `SavedLocationRepository`, `SettingsRepository`, `TripRepository`
  - `ui/distance/`: `DistanceFragment`, `DistanceViewModel`
  - `ui/savedlocations/`: `SavedLocationsFragment`, `SavedLocationAdapter`,
    `SavedLocationsViewModel`
  - `ui/settings/`: `SettingsFragment`, `SettingsViewModel`
  - `ui/speedometer/`: `SpeedometerFragment`, `SpeedometerViewModel`,
    `TripHistoryAdapter`
  - `ui/SharedDestinationViewModel`
  - `utils/`: `DistanceCalculator`, `GpsStatusHelper`, `LocationPermissionHelper`,
    `LocationTrackingHelper`, `MapUtils`, `NetworkStatusHelper`

### 15.2 Known Doc-vs-Code Naming Mismatch
- Design doc §8 names the history entity `HistoryEntry`; actual code uses `Trip`
  (`Trip.kt`, `TripDao.kt`, `TripRepository.kt`, `TripHistoryAdapter.kt`). Either the
  doc's naming should be reconciled to `Trip`, or the code renamed to match the doc —
  not yet decided; flagged here so it isn't lost.

### 15.3 Known Gaps vs. This Doc (as of last review)
- `AndroidManifest.xml` currently declares only `ACCESS_FINE_LOCATION`,
  `ACCESS_COARSE_LOCATION`, `INTERNET`, `ACCESS_NETWORK_STATE`. Missing (needed for
  §6.1.4 background tracking and §14.1 sticky notification): background location
  permission, `POST_NOTIFICATIONS`, and a declared foreground service.
- No `AppWidgetProvider`/widget XML present yet — §14.2 (4x2 widget) not started
  (expected, it's V3 scope now).
- No `shortcuts.xml`/dynamic shortcuts present yet — §14.3 (app icon long-press
  shortcuts) not started (expected, V3 scope now).
- Speedometer (§6.2) is not yet implemented at all in the reference repo (expected,
  it's V2 scope) — the person has decided to start a fresh implementation rather than
  build on the existing reference repo (see §15.4).
- `DistanceCalculator.kt` already includes an `isDestinationReached()` helper (10m
  threshold) not currently documented anywhere in this spec — worth deciding if
  "destination reached" should be a documented app behavior (e.g., a notification/
  toast/vibration when reached) and adding it to §6.1.4 if so.

### 15.4 Development Workflow (this conversation)
- The person pulls/maintains the actual working copy of the repository locally (not
  edited directly by Claude in this session).
- When a code change is discussed, Claude specifies the exact action per file:
  - **Update**: existing file path + description of the change (and/or a diff/snippet).
  - **Add**: new file path + full content.
  - **Delete**: file path to remove, with reason.
- The person applies these changes to their own local checkout and commits/pushes as
  they see fit.
