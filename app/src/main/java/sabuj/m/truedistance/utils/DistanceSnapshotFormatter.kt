package sabuj.m.truedistance.utils

import sabuj.m.truedistance.database.DistanceSnapshot
import kotlin.math.abs

/**
 * Time-Based Distance History Log (per time-based-log.md spec).
 *
 * Builds display rows for a completed trip's expanded history card.
 * Rows are selected post-hoc from raw (timestamp, distanceMeters) samples
 * using time-based percentage tiers — never during live tracking.
 *
 * Tier selection (by total trip duration):
 *   A (≥10min): 11 bars at 0,10,20,...,100%
 *   B (2–10min): 8 bars at 0,15,30,45,60,75,90,100%
 *   C (20s–2min): 3 bars at 0,50,100%
 *   D (<20s):     2 bars at 0,100%
 *
 * Each percentage mark is converted to a target timestamp, then snapped
 * to the closest real recorded sample. Labels show elapsed time ("+2m 14s"),
 * not percentages.
 */
object DistanceSnapshotFormatter {

    data class DisplayRow(val label: String, val distanceMeters: Double)

    /**
     * @param snapshots     All raw GPS samples for this trip, sorted by timestamp.
     * @param startedAt     Trip start timestamp (millis).
     * @param endedAt       Trip end timestamp (millis).
     */
    fun buildDisplayRows(
        snapshots: List<DistanceSnapshot>,
        startedAt: Long,
        endedAt: Long
    ): List<DisplayRow> {
        if (snapshots.isEmpty()) return emptyList()

        val sorted = snapshots.sortedBy { it.timestamp }
        val totalDuration = endedAt - startedAt

        // Step 1 — Choose tier based on total trip duration
        val targetPercents = chooseTier(totalDuration)

        // Step 2+3 — Convert each % to a target timestamp, snap to nearest real sample
        val rows = mutableListOf<DisplayRow>()
        val usedIndices = mutableSetOf<Int>()

        for (pct in targetPercents) {
            val targetTime = startedAt + (pct / 100.0 * totalDuration).toLong()

            // Find the sample closest to targetTime
            var bestIdx = 0
            var bestDiff = Long.MAX_VALUE
            for (i in sorted.indices) {
                val diff = abs(sorted[i].timestamp - targetTime)
                if (diff < bestDiff) {
                    bestDiff = diff
                    bestIdx = i
                }
            }

            // Avoid duplicate rows for the same sample (can happen on very short trips)
            if (bestIdx in usedIndices && pct != 0 && pct != 100) continue
            usedIndices.add(bestIdx)

            val sample = sorted[bestIdx]
            val elapsed = sample.timestamp - startedAt
            val label = when (pct) {
                0 -> "Start  •  ${formatElapsed(0)}"
                100 -> "End  •  ${formatElapsed(elapsed)}"
                else -> formatElapsed(elapsed)
            }
            rows.add(DisplayRow(label, sample.distanceMeters))
        }

        return rows
    }

    /**
     * Legacy overload for existing callers that pass sessionDurationMillis.
     * Converts to the new (startedAt, endedAt) signature.
     */
    fun buildDisplayRows(
        snapshots: List<DistanceSnapshot>,
        sessionDurationMillis: Long
    ): List<DisplayRow> {
        if (snapshots.isEmpty()) return emptyList()
        val sorted = snapshots.sortedBy { it.timestamp }
        val startedAt = sorted.first().timestamp
        val endedAt = startedAt + sessionDurationMillis
        return buildDisplayRows(sorted, startedAt, endedAt)
    }

    /** Step 1 — Choose bar-count tier based on total trip duration. */
    private fun chooseTier(durationMillis: Long): List<Int> {
        val tenMinutes = 10 * 60_000L
        val twoMinutes = 2 * 60_000L
        val twentySeconds = 20_000L

        return when {
            durationMillis >= tenMinutes -> (0..100 step 10).toList()           // Tier A: 11 bars
            durationMillis >= twoMinutes -> listOf(0, 15, 30, 45, 60, 75, 90, 100) // Tier B: 8 bars
            durationMillis >= twentySeconds -> listOf(0, 50, 100)               // Tier C: 3 bars
            else -> listOf(0, 100)                                              // Tier D: 2 bars
        }
    }

    /** Formats elapsed milliseconds as a human-readable duration string. */
    private fun formatElapsed(millis: Long): String {
        val totalSeconds = millis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return when {
            hours > 0 -> "+${hours}h ${minutes}m"
            minutes > 0 -> "+${minutes}m ${seconds}s"
            else -> "+${seconds}s"
        }
    }
}
