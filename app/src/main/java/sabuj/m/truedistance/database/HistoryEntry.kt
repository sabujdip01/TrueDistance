package sabuj.m.truedistance.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * §8 Data Model — Room Entity representing a completed True Distance point-to-point tracking session.
 *
 * @property id Unique UUID string primary key.
 * @property destinationName User-visible destination name.
 * @property destinationLat Target destination latitude coordinate.
 * @property destinationLng Target destination longitude coordinate.
 * @property initialDistanceMeters Initial direct distance calculated at session start.
 * @property finalDistanceMeters Final distance remaining when tracking stopped.
 * @property startedAt Epoch timestamp in milliseconds when tracking began.
 * @property endedAt Epoch timestamp in milliseconds when tracking ended.
 * @property savedLocationId Foreign key reference to [SavedLocation] if launched from bookmark.
 */
@Entity(tableName = "history_entries")
data class HistoryEntry(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val destinationName: String,
    val destinationLat: Double,
    val destinationLng: Double,
    val initialDistanceMeters: Double,
    val finalDistanceMeters: Double? = null,
    val startedAt: Long,
    val endedAt: Long? = null,
    val savedLocationId: String? = null
)
