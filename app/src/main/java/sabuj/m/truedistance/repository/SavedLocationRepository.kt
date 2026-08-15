package sabuj.m.truedistance.repository

import kotlinx.coroutines.flow.Flow
import sabuj.m.truedistance.database.SavedLocation
import sabuj.m.truedistance.database.SavedLocationDao
import javax.inject.Inject

class SavedLocationRepository @Inject constructor(
    private val dao: SavedLocationDao
) {
    fun observeAll(): Flow<List<SavedLocation>> = dao.observeAll()

    suspend fun getById(id: String): SavedLocation? = dao.getById(id)

    suspend fun save(location: SavedLocation) = dao.insert(location)

    suspend fun delete(location: SavedLocation) = dao.delete(location)
}
