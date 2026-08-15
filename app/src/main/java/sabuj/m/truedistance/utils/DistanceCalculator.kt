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
     * Formats a distance for display per §6.3.1: auto-meters under 1km, up to 2
     * decimal places (configurable), Km/Miles/Both per Settings.
     */
    fun format(
        meters: Double,
        unit: UnitPreference,
        decimalPrecision: Int,
        autoMetersUnder1km: Boolean
    ): String {
        val km = meters / 1000.0
        val miles = meters / 1609.344

        fun fmt(value: Double) = "%.${decimalPrecision}f".format(value)

        if (autoMetersUnder1km && meters < 1000.0) {
            return "${meters.toInt()} m"
        }

        return when (unit) {
            UnitPreference.KM -> "${fmt(km)} km"
            UnitPreference.MILES -> "${fmt(miles)} mi"
            UnitPreference.BOTH -> "${fmt(km)} km / ${fmt(miles)} mi"
        }
    }
}
