package sabuj.m.truedistance.repository

import kotlinx.coroutines.flow.Flow
import sabuj.m.truedistance.database.Trip
import sabuj.m.truedistance.database.TripDao
import javax.inject.Inject
import javax.inject.Singleton

/** §6.2.2 Past Trips — Speedometer trip persistence */
@Singleton
class TripRepository @Inject constructor(
    private val tripDao: TripDao
) {
    fun observeAll(): Flow<List<Trip>> = tripDao.observeAll()

    suspend fun getById(id: String): Trip? = tripDao.getById(id)

    suspend fun saveTrip(trip: Trip) = tripDao.insert(trip)

    suspend fun updateTrip(trip: Trip) = tripDao.update(trip)

    suspend fun delete(trip: Trip) = tripDao.delete(trip)

    suspend fun deleteAll() = tripDao.deleteAll()
}
