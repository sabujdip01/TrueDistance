package sabuj.m.truedistance.utils

import sabuj.m.truedistance.database.DistanceSnapshot
import kotlin.math.abs

/** §6.1.3 — smart interval-row count for expanded Distance History entries. */
object DistanceSnapshotFormatter {

    data class DisplayRow(val label: String, val distanceMeters: Double)

    fun buildDisplayRows(
        snapshots: List<DistanceSnapshot>,
        sessionDurationMillis: Long
    ): List<DisplayRow> {
        if (snapshots.isEmpty()) return emptyList()

        val targetPercents: List<Int> = when {
            sessionDurationMillis < 60_000L -> listOf(0, 100)
            sessionDurationMillis < 5 * 60_000L -> listOf(0, 50, 100)
            sessionDurationMillis < 20 * 60_000L -> listOf(0, 25, 50, 75, 100)
            else -> (0..100 step 10).toList()
        }

        val byPercent = snapshots.associateBy { it.elapsedPercent }

        return targetPercents.mapNotNull { pct ->
            val closest = byPercent[pct]
                ?: snapshots.minByOrNull { abs(it.elapsedPercent - pct) }
            closest?.let {
                val label = when (pct) {
                    0 -> "Start time"
                    100 -> "End time"
                    else -> "After ${pct}% of elapsed time"
                }
                DisplayRow(label, it.distanceMeters)
            }
        }
    }
}
