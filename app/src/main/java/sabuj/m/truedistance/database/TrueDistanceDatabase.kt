package sabuj.m.truedistance.database

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * §8 Data Model — TrueDistanceDatabase is the primary Room database for True Distance.
 *
 * Entities:
 * - [SavedLocation]: Bookmarked destination points.
 * - [HistoryEntry]: Point-to-point tracking session summaries.
 * - [DistanceSnapshot]: Raw GPS distance samples captured during tracking sessions.
 * - [Trip]: Speedometer trip records with breadcrumb path coordinate JSON.
 */
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
