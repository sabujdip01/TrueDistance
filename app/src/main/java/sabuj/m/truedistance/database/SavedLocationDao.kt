package sabuj.m.truedistance.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedLocationDao {

    @Query("SELECT * FROM saved_locations ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<SavedLocation>>

    @Query("SELECT * FROM saved_locations WHERE id = :id")
    suspend fun getById(id: String): SavedLocation?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: SavedLocation)

    @Update
    suspend fun update(location: SavedLocation)

    @Delete
    suspend fun delete(location: SavedLocation)
}
