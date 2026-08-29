package sabuj.m.truedistance.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * §8 Data Model — Data Access Object for [SavedLocation] bookmarks.
 */
@Dao
interface SavedLocationDao {

    /** Observes all saved locations sorted chronologically newest-first. */
    @Query("SELECT * FROM saved_locations ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<SavedLocation>>

    /** Retrieves a specific saved location by its primary key. */
    @Query("SELECT * FROM saved_locations WHERE id = :id")
    suspend fun getById(id: String): SavedLocation?

    /** Inserts a new bookmark or replaces an existing one. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: SavedLocation)

    /** Updates bookmark properties. */
    @Update
    suspend fun update(location: SavedLocation)

    /** Deletes an existing bookmark. */
    @Delete
    suspend fun delete(location: SavedLocation)
}
