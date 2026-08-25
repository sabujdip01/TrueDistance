package sabuj.m.truedistance.repository

import kotlinx.coroutines.flow.Flow
import sabuj.m.truedistance.database.DistanceSnapshot
import sabuj.m.truedistance.database.DistanceSnapshotDao
import sabuj.m.truedistance.database.HistoryEntry
import sabuj.m.truedistance.database.HistoryEntryDao
import javax.inject.Inject

/**
 * §6.1.3 Distance History — Repository managing tracking session summaries and raw GPS distance snapshots.
 */
class HistoryRepository @Inject constructor(
    private val historyDao: HistoryEntryDao,
    private val snapshotDao: DistanceSnapshotDao
) {
    /** Observes all tracking history sessions ordered chronologically. */
    fun observeAll(): Flow<List<HistoryEntry>> = historyDao.observeAll()

    /** Gets a specific history session by ID. */
    suspend fun getById(id: String): HistoryEntry? = historyDao.getById(id)

    /** Inserts a new session record when tracking begins. */
    suspend fun startSession(entry: HistoryEntry) = historyDao.insert(entry)

    /** Updates session metrics upon completion or stoppage. */
    suspend fun updateSession(entry: HistoryEntry) = historyDao.update(entry)

    /** Deletes an individual history session. */
    suspend fun delete(entry: HistoryEntry) = historyDao.delete(entry)

    /** Clears all tracking history entries. */
    suspend fun clearAll() = historyDao.clearAll()

    /** Observes time-stamped distance snapshots for a specific tracking session. */
    fun observeSnapshots(historyEntryId: String): Flow<List<DistanceSnapshot>> =
        snapshotDao.observeForEntry(historyEntryId)

    /** Gets all snapshots for a specific tracking session. */
    suspend fun getSnapshots(historyEntryId: String): List<DistanceSnapshot> =
        snapshotDao.getForEntry(historyEntryId)

    /** Records a single distance snapshot sample. */
    suspend fun recordSnapshot(snapshot: DistanceSnapshot) = snapshotDao.insert(snapshot)
}
