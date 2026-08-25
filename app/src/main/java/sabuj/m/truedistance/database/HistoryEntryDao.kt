package sabuj.m.truedistance.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * §8 Data Model — Data Access Object for point-to-point [HistoryEntry] sessions.
 */
@Dao
interface HistoryEntryDao {

    /** Observes all tracking history entries chronologically newest-first. */
    @Query("SELECT * FROM history_entries ORDER BY startedAt DESC")
    fun observeAll(): Flow<List<HistoryEntry>>

    /** Retrieves an individual history entry by ID. */
    @Query("SELECT * FROM history_entries WHERE id = :id")
    suspend fun getById(id: String): HistoryEntry?

    /** Inserts a new tracking history entry. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: HistoryEntry): Unit

    /** Updates an existing tracking history entry. */
    @Update
    suspend fun update(entry: HistoryEntry)

    /** Deletes an individual history entry. */
    @Delete
    suspend fun delete(entry: HistoryEntry)

    /** Clears all tracking history entries. */
    @Query("DELETE FROM history_entries")
    suspend fun clearAll()
}
