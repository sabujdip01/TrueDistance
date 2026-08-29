package sabuj.m.truedistance.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * §8 Data Model — Data Access Object for [DistanceSnapshot] samples.
 */
@Dao
interface DistanceSnapshotDao {

    /** Observes all snapshots belonging to a specific tracking session. */
    @Query("SELECT * FROM distance_snapshots WHERE historyEntryId = :historyEntryId ORDER BY elapsedPercent ASC")
    fun observeForEntry(historyEntryId: String): Flow<List<DistanceSnapshot>>

    /** Gets all snapshots belonging to a specific tracking session. */
    @Query("SELECT * FROM distance_snapshots WHERE historyEntryId = :historyEntryId ORDER BY elapsedPercent ASC")
    suspend fun getForEntry(historyEntryId: String): List<DistanceSnapshot>

    /** Inserts a new distance snapshot. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(snapshot: DistanceSnapshot)
}
