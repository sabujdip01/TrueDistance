package sabuj.m.truedistance.repository

import kotlinx.coroutines.flow.Flow
import sabuj.m.truedistance.database.DistanceSnapshot
import sabuj.m.truedistance.database.DistanceSnapshotDao
import sabuj.m.truedistance.database.HistoryEntry
import sabuj.m.truedistance.database.HistoryEntryDao
import javax.inject.Inject

/** §6.1.3 Distance History — session + interval-snapshot persistence */
class HistoryRepository @Inject constructor(
    private val historyDao: HistoryEntryDao,
    private val snapshotDao: DistanceSnapshotDao
) {
    fun observeAll(): Flow<List<HistoryEntry>> = historyDao.observeAll()

    suspend fun getById(id: String): HistoryEntry? = historyDao.getById(id)

    suspend fun startSession(entry: HistoryEntry) = historyDao.insert(entry)

    suspend fun updateSession(entry: HistoryEntry) = historyDao.update(entry)

    suspend fun delete(entry: HistoryEntry) = historyDao.delete(entry)

    suspend fun clearAll() = historyDao.clearAll()

    fun observeSnapshots(historyEntryId: String): Flow<List<DistanceSnapshot>> =
        snapshotDao.observeForEntry(historyEntryId)

    suspend fun getSnapshots(historyEntryId: String): List<DistanceSnapshot> =
        snapshotDao.getForEntry(historyEntryId)

    suspend fun recordSnapshot(snapshot: DistanceSnapshot) = snapshotDao.insert(snapshot)
}
