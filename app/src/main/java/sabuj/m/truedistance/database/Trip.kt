package sabuj.m.truedistance.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/** §8 Data Model — Trip (Speedometer tab, §6.2) */
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
