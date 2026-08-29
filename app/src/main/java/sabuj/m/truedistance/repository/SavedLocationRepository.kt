package sabuj.m.truedistance.repository

import kotlinx.coroutines.flow.Flow
import sabuj.m.truedistance.database.SavedLocation
import sabuj.m.truedistance.database.SavedLocationDao
import javax.inject.Inject

/**
 * §6.1.2 Saved Locations — Repository managing destination bookmark persistence.
 */
class SavedLocationRepository @Inject constructor(
    private val dao: SavedLocationDao
) {
    /** Observes all saved locations sorted chronologically. */
    fun observeAll(): Flow<List<SavedLocation>> = dao.observeAll()

    /** Gets a saved location by ID. */
    suspend fun getById(id: String): SavedLocation? = dao.getById(id)

    /** Inserts or updates a bookmarked location. */
    suspend fun save(location: SavedLocation) = dao.insert(location)

    /** Deletes an existing bookmark. */
    suspend fun delete(location: SavedLocation) = dao.delete(location)
}
