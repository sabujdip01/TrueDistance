package sabuj.m.truedistance.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        SavedLocation::class,
        HistoryEntry::class,
        DistanceSnapshot::class
    ],
    version = 1,
    exportSchema = true
)
abstract class TrueDistanceDatabase : RoomDatabase() {
    abstract fun savedLocationDao(): SavedLocationDao
    abstract fun historyEntryDao(): HistoryEntryDao
    abstract fun distanceSnapshotDao(): DistanceSnapshotDao
}
