package sabuj.m.truedistance.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * §8 Data Model — Data Access Object for Speedometer [Trip] records.
 */
@Dao
interface TripDao {

    /** Observes all recorded trips ordered from newest to oldest as a reactive Flow. */
    @Query("SELECT * FROM trips ORDER BY startedAt DESC")
    fun observeAll(): Flow<List<Trip>>

    /** Retrieves a specific trip by its UUID primary key. */
    @Query("SELECT * FROM trips WHERE id = :id")
    suspend fun getById(id: String): Trip?

    /** Inserts a new trip or replaces an existing record. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(trip: Trip)

    /** Updates an existing trip record. */
    @Update
    suspend fun update(trip: Trip)

    /** Deletes an individual trip record. */
    @Delete
    suspend fun delete(trip: Trip)

    /** Clears all trip history from the database. */
    @Query("DELETE FROM trips")
    suspend fun deleteAll()
}
