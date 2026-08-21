package sabuj.m.truedistance.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        SavedLocation::class,
        HistoryEntry::class,
        DistanceSnapshot::class,
        Trip::class
    ],
    version = 2,
    exportSchema = false
)
abstract class TrueDistanceDatabase : RoomDatabase() {
    abstract fun savedLocationDao(): SavedLocationDao
    abstract fun historyEntryDao(): HistoryEntryDao
    abstract fun distanceSnapshotDao(): DistanceSnapshotDao
    abstract fun tripDao(): TripDao
}
