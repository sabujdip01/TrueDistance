# Time-Based Distance History Log — Feature Spec

> This document is self-contained. It is written for an AI agent (or developer) with
> no prior context on the project, and can be read/implemented independently of any
> other project documentation.

## 1. App Context

**True Distance** is an Android app. Its core feature lets a user pick a
destination, then live-tracks the straight-line ("as the crow flies," not
driving-route) distance from the user's current GPS location to that destination
while they move. Each tracking session is called a **Trip**. Completed trips are
saved and shown in a **Distance History** screen.

## 2. Screen & UI Element

**Screen:** Distance History screen — a list of past trips, each shown as a card.

**Element:** Tapping a trip's card expands it to reveal a **progress log** — a short
list of "bars," each bar showing a timestamp and the distance-to-destination
recorded at that moment during the trip. This document specifies how that progress
log is generated.

## 3. Why This Design

- Logging every raw GPS update would produce too much data to usefully display.
- A naive fixed-**distance**-percentage approach (e.g. "log every time distance
  drops another 10%") breaks in practice: distance-to-destination does not decrease
  monotonically. A curved route, a wrong turn, or backtracking can make it
  temporarily *increase*. Distance is therefore not a reliable axis to index the log
  by.
- The log must instead be indexed by **elapsed time**, computed once the trip has
  fully ended (not live, since total duration is unknown while a trip is still in
  progress).
- The number of bars shown must scale down for short trips, so bars on a short trip
  aren't uselessly close together in time.

## 4. Prerequisite: Raw Sample Data

While a trip is active, the app already records `(timestamp, distanceMeters)`
samples repeatedly, driven by the existing live GPS/distance-tracking loop. **This
feature does not add that recording** — it only needs read access to the full raw
sample list for a trip once that trip has ended.

## 5. When This Logic Runs

Only **after** a trip has stopped — never while a trip is in progress. A trip can
stop for any of these reasons:

1. **User manually taps Stop.**
2. **Auto-stop — destination reached:** distance-to-destination ≤ ~10 meters.
3. **Auto-stop — GPS failure:** 3 consecutive failed location-fetch attempts.

At stop time, the total elapsed trip duration `T_total` (from the trip's first
recorded sample to its last) is now known. This value drives everything below.

## 6. Step 1 — Choose a Bar-Count Tier

Pick the **densest tier** whose minimum gap between bars does not fall below that
tier's floor. Conceptually: evaluate tiers from most bars to fewest, and use the
first one that produces reasonably spaced bars for this trip's actual duration.

| Tier | # bars | Marks (% of `T_total`) | Approx. minimum gap this tier needs |
|---|---|---|---|
| A | 11 | 0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100 | ~1 minute per 10% step |
| B | 8  | 0, 15, 30, 45, 60, 75, 90, 100 | smaller floor, proportional to step size |
| C | 3  | 0, 50, 100 | smaller still |
| D | 2  | 0, 100 | always valid — the unconditional fallback |

Rough resulting duration bands (exact cutoffs are tunable defaults, not fixed
constants — implementers may adjust them, but the tiered *rule* itself should stay):

- `T_total` ≥ 10 minutes → **Tier A**
- 2 minutes ≤ `T_total` < 10 minutes → **Tier B**
- ~20 seconds ≤ `T_total` < 2 minutes → **Tier C**
- `T_total` < ~20 seconds → **Tier D**

## 7. Step 2 — Convert Each Percentage Mark to a Target Timestamp

For each percentage mark `p` in the chosen tier:

```
targetTime = trip.startedAt + (p / 100) * T_total
```

## 8. Step 3 — Snap Each Target Timestamp to a Real Recorded Sample

For each `targetTime`, find the raw `(timestamp, distanceMeters)` sample from that
trip whose `timestamp` is closest to it. Use that sample's **actual**
`distanceMeters` value.

- **Never interpolate or fabricate** a distance value for a time mark that has no
  nearby real sample.
- If the trip stopped early (any auto-stop condition), some higher-percentage marks
  may fall outside the actually-recorded time range. In that case, only produce
  marks that land within real recorded data. This means a trip can end up with
  **fewer bars than its tier's maximum** — e.g., a trip whose duration places it in
  Tier A (11 bars) but which was cut short by a GPS-failure auto-stop partway
  through might only produce 6–7 usable bars, because that's all that was actually
  recorded before the stop.

## 9. Step 4 — Always Include Start and End

Regardless of tier, **always** include:

- **Start (0%)**: the trip's actual first recorded sample.
- **End (100%)**: the trip's actual last recorded sample.

Never synthesize these — use real recorded values. Note that **End's distance value
is not guaranteed to be near zero.** It is only near zero if the trip's stop reason
was "destination reached." For a manual stop or a GPS-failure stop, End reflects
whatever distance was last actually measured, which could be large.

## 10. Display Format (Expanded Card)

Each bar in the UI shows:

- **Time label**: elapsed time or clock time (e.g. `"+2m 14s"` or `"6:47 AM"`) —
  **not** a percentage label like "10%". The percentage is only an internal
  selection mechanism, not something shown to the user.
- **Distance value**: the recorded `distanceMeters` at that bar, formatted per the
  user's unit settings (meters if under 1 km, otherwise km or miles per whatever the
  app's unit preference is set to).

## 11. Explicit Non-Goals

- Do **not** recompute or update this log while a trip is still in progress. It is a
  post-hoc summary generated once, after the trip has fully ended.
- Do **not** re-trigger, remove, or "undo" a bar if distance later moves the other
  direction. Marks are computed once from final, complete trip data — not from live
  threshold-crossing events during tracking.
- Do **not** backfill or interpolate a distance value for a time mark that has no
  sufficiently close real sample. Absence is acceptable; fabrication is not.

## 12. Worked Example

Given a trip where `startedAt = 00:00` and the trip runs long enough to land in
Tier A (`T_total` ≥ 10 minutes), the 11 target timestamps are simply 0%, 10%, 20%,
… 100% of the total duration — e.g., for a 10-minute trip: `00:00, 01:00, 02:00,
03:00, 04:00, 05:00, 06:00, 07:00, 08:00, 09:00, 10:00`. Each of these target times
is then matched to whichever real GPS sample was closest to it, and that sample's
actual distance value is what gets shown — not a computed/interpolated value.