package sabuj.m.truedistance.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/** §8 Data Model — DistanceSnapshot, feeds §6.1.3 interval rows */
@Entity(
    tableName = "distance_snapshots",
    foreignKeys = [
        ForeignKey(
            entity = HistoryEntry::class,
            parentColumns = ["id"],
            childColumns = ["historyEntryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("historyEntryId")]
)
data class DistanceSnapshot(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val historyEntryId: String,
    val timestamp: Long,
    val elapsedPercent: Int,
    val distanceMeters: Double
)
