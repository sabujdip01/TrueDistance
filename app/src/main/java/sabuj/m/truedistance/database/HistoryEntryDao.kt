package sabuj.m.truedistance.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryEntryDao {

    @Query("SELECT * FROM history_entries ORDER BY startedAt DESC")
    fun observeAll(): Flow<List<HistoryEntry>>

    @Query("SELECT * FROM history_entries WHERE id = :id")
    suspend fun getById(id: String): HistoryEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: HistoryEntry): Unit

    @Update
    suspend fun update(entry: HistoryEntry)

    @Delete
    suspend fun delete(entry: HistoryEntry)

    @Query("DELETE FROM history_entries")
    suspend fun clearAll()
}
