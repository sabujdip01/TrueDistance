package sabuj.m.truedistance.utils

/**
 * §6.2.5 — Guards against raw GPS speed spikes due to multipath reflections
 * or momentary signal jumps. Validates acceleration and velocity between successive fixes.
 *
 * @param maxAccelerationMps2 Maximum allowed acceleration rate in m/s².
 * @param maxRealisticSpeedMps Upper bound velocity cap in m/s (~252 km/h).
 * @param stationaryThresholdMps Stationary jitter deadband in m/s (~2.16 km/h).
 */
class SpeedSpikeFilter(
    private val maxAccelerationMps2: Double = 10.0,
    private val maxRealisticSpeedMps: Double = 70.0, // ~252 km/h
    private val stationaryThresholdMps: Double = 0.6 // ~2.16 km/h deadband
) {
    private var lastAcceptedSpeedMps: Double = 0.0
    private var lastAcceptedTimeMillis: Long = 0L

    fun filter(rawSpeedMps: Float, timeMillis: Long): Double {
        var speed = rawSpeedMps.toDouble().coerceAtLeast(0.0)

        // Stationary deadband: filter out minor GPS jitter when standing still
        if (speed < stationaryThresholdMps) {
            speed = 0.0
        }

        if (lastAcceptedTimeMillis == 0L) {
            lastAcceptedSpeedMps = if (speed <= maxRealisticSpeedMps) speed else 0.0
            lastAcceptedTimeMillis = timeMillis
            return lastAcceptedSpeedMps
        }

        val deltaSeconds = (timeMillis - lastAcceptedTimeMillis) / 1000.0
        if (deltaSeconds <= 0.0) {
            return lastAcceptedSpeedMps
        }

        val acceleration = kotlin.math.abs(speed - lastAcceptedSpeedMps) / deltaSeconds

        if (speed <= maxRealisticSpeedMps && acceleration <= maxAccelerationMps2) {
            lastAcceptedSpeedMps = speed
            lastAcceptedTimeMillis = timeMillis
        } else {
            // Treat as spike: do not accept as new max, decay gradually
            lastAcceptedTimeMillis = timeMillis
        }

        return lastAcceptedSpeedMps
    }

    fun reset() {
        lastAcceptedSpeedMps = 0.0
        lastAcceptedTimeMillis = 0L
    }
}
