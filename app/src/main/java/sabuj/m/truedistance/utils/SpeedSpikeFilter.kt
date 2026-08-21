package sabuj.m.truedistance.utils

/**
 * §6.2.5 — Guards against raw GPS speed spikes due to multipath reflections
 * or momentary signal jumps. Validates acceleration between successive fixes.
 */
class SpeedSpikeFilter(
    private val maxAccelerationMps2: Double = 10.0,
    private val maxRealisticSpeedMps: Double = 70.0 // ~252 km/h
) {
    private var lastAcceptedSpeedMps: Double = 0.0
    private var lastAcceptedTimeMillis: Long = 0L

    fun filter(rawSpeedMps: Float, timeMillis: Long): Double {
        val speed = rawSpeedMps.toDouble().coerceAtLeast(0.0)

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
