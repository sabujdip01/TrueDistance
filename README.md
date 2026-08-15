# True Distance

**True Distance** is an Android app that shows the straight-line ("as the crow
flies") distance between your current location and any destination — live, on a
map. It also includes a built-in GPS Speedometer with trip tracking.

## Features

### True Distance
- Live straight-line distance to any destination, updated as you move
- Pick a destination by search, map tap, or saved locations
- Background tracking with a live notification
- Distance History with interval-based distance snapshots per session
- Saved Locations for quick reuse

### Speedometer
- Real-time speed gauge (up to 2 decimal precision)
- Trip stats: start time, elapsed time, distance, average speed, max speed
- Actual road-path tracking on map (not straight-line)
- Start / Pause / Resume / Stop trip controls
- Past Trips history with auto-generated trip names

### General
- Fully local, on-device data — no login, no account, no cloud sync
- Light / Dark / System theme
- Configurable units (km, miles, or both), GPS accuracy, and update frequency
- Handles no-internet and no-GPS states gracefully
- Responsive layouts across phones and tablets

## Tech Stack
- Kotlin, Android (MVVM)
- Room (local persistence)
- Hilt (dependency injection)
- Google Maps SDK, Places API, Geocoding API

## Project Status
Actively in development. See [`project-overview.md`](./project-overview.md) for the
full product/design specification (screens, data model, permissions, edge-case
handling, and the phased release plan).

## Release Plan
- **V1** — True Distance core feature, Settings, branding
- **V2** — Speedometer (full trip tracking)
- **V3** — Background sticky notification controls, home screen widget, app
  shortcuts, in-app changelog

## Privacy
All personal data (saved locations, history, trips, settings) is stored locally on
your device. No account or login is required. Google Maps Platform APIs are used
only for map rendering, place search, and geocoding.

## License
TBD

## Contributing
This is currently a solo/early-stage project. Issues and suggestions are welcome.
