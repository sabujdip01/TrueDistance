package sabuj.m.truedistance.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import sabuj.m.truedistance.database.HistoryEntry
import sabuj.m.truedistance.repository.HistoryRepository
import sabuj.m.truedistance.utils.DistanceSnapshotFormatter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * §6.1.3 History UI State — Encapsulates sectioned list items, measurement unit, precision, and auto-meters settings.
 */
data class HistoryUiState(
    val items: List<HistoryListItem> = emptyList(),
    val unit: sabuj.m.truedistance.database.UnitPreference = sabuj.m.truedistance.database.UnitPreference.KM,
    val decimalPrecision: Int = 2,
    val autoMetersUnder1km: Boolean = true
)

/**
 * §6.1.3 Distance History Screen ViewModel — Manages date grouping ("Today", "Yesterday", "Older"),
 * single-card accordion expansion, lazy snapshot loading, and session deletions.
 */
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: HistoryRepository,
    private val settingsRepository: sabuj.m.truedistance.repository.SettingsRepository
) : ViewModel() {

    private val expandedIds = MutableStateFlow<Set<String>>(emptySet())
    private val snapshotCache = MutableStateFlow<Map<String, List<DistanceSnapshotFormatter.DisplayRow>>>(emptyMap())

    val uiState: StateFlow<HistoryUiState> = combine(
        repository.observeAll(),
        expandedIds,
        snapshotCache,
        settingsRepository.unit,
        settingsRepository.decimalPrecision,
        settingsRepository.autoMetersUnder1km
    ) { args: Array<Any?> ->
        val entries = args[0] as List<HistoryEntry>
        val expanded = args[1] as Set<String>
        val snapshots = args[2] as Map<String, List<DistanceSnapshotFormatter.DisplayRow>>
        val unit = args[3] as sabuj.m.truedistance.database.UnitPreference
        val precision = args[4] as Int
        val autoMeters = args[5] as Boolean

        HistoryUiState(
            items = buildListItems(entries, expanded, snapshots),
            unit = unit,
            decimalPrecision = precision,
            autoMetersUnder1km = autoMeters
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        HistoryUiState()
    )

    fun toggleExpanded(entryId: String) {
        viewModelScope.launch {
            val current = expandedIds.value
            val isNowExpanding = entryId !in current
            // Only one card can be expanded at a time
            expandedIds.value = if (isNowExpanding) setOf(entryId) else emptySet()

            if (isNowExpanding && !snapshotCache.value.containsKey(entryId)) {
                // Load snapshots lazily and cache for reactive re-rendering.
                val entry = repository.getById(entryId) ?: return@launch
                val snapshots = repository.getSnapshots(entryId)
                val endedAt = entry.endedAt ?: System.currentTimeMillis()
                val rows = DistanceSnapshotFormatter.buildDisplayRows(
                    snapshots, entry.startedAt, endedAt
                )
                snapshotCache.value = snapshotCache.value + (entryId to rows)
            }
        }
    }

    fun delete(entry: HistoryEntry) {
        viewModelScope.launch { repository.delete(entry) }
    }

    fun clearAll() {
        viewModelScope.launch { repository.clearAll() }
    }

    /** §6.1.3 — groups entries into Today / Yesterday / Older date-header sections. */
    private fun buildListItems(
        entries: List<HistoryEntry>,
        expanded: Set<String>,
        snapshots: Map<String, List<DistanceSnapshotFormatter.DisplayRow>>
    ): List<HistoryListItem> {
        val today = startOfDay(System.currentTimeMillis())
        val yesterday = today - DAY_MILLIS

        val grouped = entries.groupBy { entry ->
            when {
                startOfDay(entry.startedAt) == today -> "Today"
                startOfDay(entry.startedAt) == yesterday -> "Yesterday"
                else -> "Older"
            }
        }

        val result = mutableListOf<HistoryListItem>()
        listOf("Today", "Yesterday", "Older").forEach { section ->
            val sectionEntries = grouped[section] ?: return@forEach
            if (sectionEntries.isEmpty()) return@forEach
            result += HistoryListItem.DateHeader(section)
            sectionEntries.forEach { entry ->
                result += HistoryListItem.EntryRow(
                    entry = entry,
                    expanded = entry.id in expanded,
                    snapshotRows = snapshots[entry.id].orEmpty()
                )
            }
        }
        return result
    }

    private fun startOfDay(millis: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = millis
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    companion object {
        private const val DAY_MILLIS = 24 * 60 * 60 * 1000L
        val timeFormatter = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    }
}
