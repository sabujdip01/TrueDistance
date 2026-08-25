package sabuj.m.truedistance.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * §8 Data Model — Room Entity representing a completed or active Speedometer trip.
 *
 * @property id Unique UUID primary key string.
 * @property startedAt Epoch timestamp in milliseconds when trip was started.
 * @property endedAt Epoch timestamp in milliseconds when trip was stopped.
 * @property distanceMeters Total accumulated distance in meters.
 * @property elapsedMillis Total active elapsed duration in milliseconds.
 * @property averageSpeedKmh Calculated average speed in KM/H.
 * @property maxSpeedKmh Spike-filtered maximum speed achieved in KM/H.
 * @property startLat Origin latitude coordinate.
 * @property startLng Origin longitude coordinate.
 * @property endLat Final destination latitude coordinate.
 * @property endLng Final destination longitude coordinate.
 * @property pathPointsJson Serialized JSON array of LatLng breadcrumb coordinates.
 */
@Entity(tableName = "trips")
data class Trip(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val startedAt: Long,
    val endedAt: Long? = null,
    val distanceMeters: Double,
    val elapsedMillis: Long,
    val averageSpeedKmh: Double,
    val maxSpeedKmh: Double,
    val startLat: Double,
    val startLng: Double,
    val endLat: Double? = null,
    val endLng: Double? = null,
    val pathPointsJson: String = "[]"
)
