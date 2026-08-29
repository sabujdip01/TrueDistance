package sabuj.m.truedistance.repository

import kotlinx.coroutines.flow.Flow
import sabuj.m.truedistance.database.Trip
import sabuj.m.truedistance.database.TripDao
import javax.inject.Inject
import javax.inject.Singleton

/**
 * §6.2.2 Past Trips — Repository managing Speedometer trip records and breadcrumb paths.
 */
@Singleton
class TripRepository @Inject constructor(
    private val tripDao: TripDao
) {
    /** Observes all recorded trips sorted chronologically newest-first. */
    fun observeAll(): Flow<List<Trip>> = tripDao.observeAll()

    /** Retrieves an individual trip by its UUID primary key. */
    suspend fun getById(id: String): Trip? = tripDao.getById(id)

    /** Inserts a new trip record into the database. */
    suspend fun saveTrip(trip: Trip) = tripDao.insert(trip)

    /** Updates an existing trip record. */
    suspend fun updateTrip(trip: Trip) = tripDao.update(trip)

    /** Deletes an individual trip record. */
    suspend fun delete(trip: Trip) = tripDao.delete(trip)

    /** Clears all past trips from the database. */
    suspend fun deleteAll() = tripDao.deleteAll()
}
