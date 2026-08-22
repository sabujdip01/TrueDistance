package sabuj.m.truedistance.utils

import sabuj.m.truedistance.database.UnitPreference
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** §7 / §8 — Haversine straight-line distance + unit formatting (§6.3.1). */
object DistanceCalculator {

    private const val EARTH_RADIUS_METERS = 6_371_000.0

    fun haversineMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_METERS * c
    }

    fun isDestinationReached(distanceMeters: Double, thresholdMeters: Double = 10.0): Boolean =
        distanceMeters <= thresholdMeters

    /**
     * Formats a distance for display:
     * - Under 1 KM (< 1000m): 3-digit meters ("%03d M", e.g. 000 M, 045 M, 850 M)
     * - 1 KM and above: 2 decimal places ("%.2f KM", e.g. 1.25 KM)
     */
    fun format(
        meters: Double,
        unit: UnitPreference,
        decimalPrecision: Int = 2,
        autoMetersUnder1km: Boolean = true
    ): String {
        val safeMeters = meters.coerceAtLeast(0.0)
        val km = safeMeters / 1000.0
        val miles = safeMeters / 1609.344

        fun fmt(value: Double) = "%.${decimalPrecision}f".format(java.util.Locale.US, value)

        if (autoMetersUnder1km && safeMeters < 1000.0 && unit != UnitPreference.MILES) {
            return String.format(java.util.Locale.US, "%d M", safeMeters.toInt())
        }

        if (autoMetersUnder1km && safeMeters < 1609.344 && unit == UnitPreference.MILES) {
            val feet = (safeMeters * 3.28084).toInt()
            return String.format(java.util.Locale.US, "%d FT", feet)
        }

        return when (unit) {
            UnitPreference.KM -> "${fmt(km)} KM"
            UnitPreference.MILES -> "${fmt(miles)} MI"
            UnitPreference.BOTH -> "${fmt(km)} KM / ${fmt(miles)} MI"
        }
    }

    /**
     * Formats speed:
     * - Under 1 KM/H: meters per hour ("%d", unit = "M/H", e.g. 0 M/H, 450 M/H)
     * - 1 KM/H and above: 2 decimal places ("%.2f", unit = "KM/H", e.g. 24.50 KM/H)
     */
    fun formatSpeedParts(
        speedKmh: Double,
        unit: UnitPreference
    ): Pair<String, String> {
        val safeSpeedKmh = speedKmh.coerceAtLeast(0.0)
        return if (unit == UnitPreference.MILES) {
            val speedMph = safeSpeedKmh * 0.621371
            if (speedMph < 1.0) {
                val ftPerHour = (speedMph * 5280.0).toInt()
                Pair(String.format(java.util.Locale.US, "%d", ftPerHour), "FT/H")
            } else {
                Pair(String.format(java.util.Locale.US, "%.2f", speedMph), "MPH")
            }
        } else {
            if (safeSpeedKmh < 1.0) {
                val metersPerHour = (safeSpeedKmh * 1000.0).toInt()
                Pair(String.format(java.util.Locale.US, "%d", metersPerHour), "M/H")
            } else {
                Pair(String.format(java.util.Locale.US, "%.2f", safeSpeedKmh), "KM/H")
            }
        }
    }

    fun formatSpeedString(
        speedKmh: Double,
        unit: UnitPreference
    ): String {
        val (valStr, unitStr) = formatSpeedParts(speedKmh, unit)
        return "$valStr $unitStr"
    }
}
