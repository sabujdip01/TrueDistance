package sabuj.m.truedistance.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * §8 Data Model — Room Entity representing a time-stamped distance sample recorded during tracking.
 *
 * @property id Unique UUID string primary key.
 * @property historyEntryId Foreign key linking sample to parent [HistoryEntry].
 * @property timestamp Epoch timestamp when the sample was recorded.
 * @property elapsedPercent Percentage progress of the tracking session.
 * @property distanceMeters Remaining straight-line distance in meters at this snapshot.
 */
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
