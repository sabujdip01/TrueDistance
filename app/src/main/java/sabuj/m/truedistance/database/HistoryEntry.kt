package sabuj.m.truedistance.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/** §8 Data Model — HistoryEntry (True Distance tab, §6.1.3) */
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
