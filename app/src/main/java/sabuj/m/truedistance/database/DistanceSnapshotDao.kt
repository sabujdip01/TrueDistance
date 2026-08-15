package sabuj.m.truedistance.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DistanceSnapshotDao {

    @Query("SELECT * FROM distance_snapshots WHERE historyEntryId = :historyEntryId ORDER BY elapsedPercent ASC")
    fun observeForEntry(historyEntryId: String): Flow<List<DistanceSnapshot>>

    @Query("SELECT * FROM distance_snapshots WHERE historyEntryId = :historyEntryId ORDER BY elapsedPercent ASC")
    suspend fun getForEntry(historyEntryId: String): List<DistanceSnapshot>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(snapshot: DistanceSnapshot)
}
