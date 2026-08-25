package sabuj.m.truedistance.utils

import sabuj.m.truedistance.database.DistanceSnapshot
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

/**
 * Time-Based Distance History Log Formatter.
 *
 * Builds formatted 3-column display rows for an expanded history session card:
 *   1. Elapsed time label: "+0:00 (Start)", "+2:30", "+5:00 (End)"
 *   2. Clock time:    "9:45 AM"
 *   3. Distance:      raw distance in meters for formatting by caller per unit settings
 */
object DistanceSnapshotFormatter {

    /**
     * @param elapsedLabel  e.g. "0:00 (Start)", "+ 2:30", "+ 5:00 (End)"
     * @param clockTime     e.g. "9:45 PM"
     * @param distanceMeters raw distance for the caller to format per unit settings
     */
    data class DisplayRow(
        val elapsedLabel: String,
        val clockTime: String,
        val distanceMeters: Double,
        // Legacy compat — label combines elapsed + clock for old callers
        val label: String = elapsedLabel
    )

    private val clockFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    fun buildDisplayRows(
        snapshots: List<DistanceSnapshot>,
        startedAt: Long,
        endedAt: Long
    ): List<DisplayRow> {
        if (snapshots.isEmpty()) return emptyList()

        val sorted = snapshots.sortedBy { it.timestamp }
        val totalDuration = endedAt - startedAt
        val targetPercents = chooseTier(totalDuration)

        val rows = mutableListOf<DisplayRow>()
        val usedIndices = mutableSetOf<Int>()

        for (pct in targetPercents) {
            val targetTime = startedAt + (pct / 100.0 * totalDuration).toLong()

            var bestIdx = 0
            var bestDiff = Long.MAX_VALUE
            for (i in sorted.indices) {
                val diff = abs(sorted[i].timestamp - targetTime)
                if (diff < bestDiff) {
                    bestDiff = diff
                    bestIdx = i
                }
            }

            if (bestIdx in usedIndices && pct != 0 && pct != 100) continue
            usedIndices.add(bestIdx)

            val sample = sorted[bestIdx]
            val elapsed = sample.timestamp - startedAt
            val elapsedStr = formatElapsed(elapsed)
            val clock = clockFormat.format(Date(sample.timestamp))

            val elapsedLabel = when (pct) {
                0 -> "$elapsedStr (Start)"
                100 -> "$elapsedStr (End)"
                else -> elapsedStr
            }
            rows.add(DisplayRow(
                elapsedLabel = elapsedLabel,
                clockTime = clock,
                distanceMeters = sample.distanceMeters
            ))
        }

        return rows
    }

    /** Legacy overload. */
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

    private fun chooseTier(durationMillis: Long): List<Int> {
        val tenMinutes = 10 * 60_000L
        val twoMinutes = 2 * 60_000L
        val twentySeconds = 20_000L

        return when {
            durationMillis >= tenMinutes -> (0..100 step 10).toList()
            durationMillis >= twoMinutes -> listOf(0, 15, 30, 45, 60, 75, 90, 100)
            durationMillis >= twentySeconds -> listOf(0, 50, 100)
            else -> listOf(0, 100)
        }
    }

    /** Formats elapsed millis as "H:MM:SS" or "M:SS" or "0:SS". */
    private fun formatElapsed(millis: Long): String {
        val totalSeconds = millis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return if (hours > 0) {
            String.format(Locale.US, "+ %d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "+ %d:%02d", minutes, seconds)
        }
    }
}
