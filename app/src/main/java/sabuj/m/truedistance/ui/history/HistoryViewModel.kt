package sabuj.m.truedistance.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import sabuj.m.truedistance.database.HistoryEntry
import sabuj.m.truedistance.repository.HistoryRepository
import sabuj.m.truedistance.utils.DistanceSnapshotFormatter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/** §6.1.3 Distance History Screen ViewModel — grouping, expand/collapse, delete. */
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: HistoryRepository
) : ViewModel() {

    private val expandedIds = MutableStateFlow<Set<String>>(emptySet())

    private val _items = MutableStateFlow<List<HistoryListItem>>(emptyList())
    val items: StateFlow<List<HistoryListItem>> = _items.asStateFlow()

    init {
        viewModelScope.launch {
            combine(repository.observeAll(), expandedIds) { entries, expanded ->
                entries to expanded
            }.collect { (entries, expanded) ->
                _items.value = buildListItems(entries, expanded)
            }
        }
    }

    fun toggleExpanded(entryId: String) {
        viewModelScope.launch {
            val current = expandedIds.value
            val isNowExpanding = entryId !in current
            expandedIds.value = if (isNowExpanding) current + entryId else current - entryId

            if (isNowExpanding) {
                // Load snapshots lazily and re-render this entry's rows.
                val entry = repository.getById(entryId) ?: return@launch
                val snapshots = repository.getSnapshots(entryId)
                val endedAt = entry.endedAt ?: System.currentTimeMillis()
                val rows = DistanceSnapshotFormatter.buildDisplayRows(
                    snapshots, entry.startedAt, endedAt
                )
                _items.value = _items.value.map { item ->
                    if (item is HistoryListItem.EntryRow && item.entry.id == entryId) {
                        item.copy(expanded = true, snapshotRows = rows)
                    } else item
                }
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
        expanded: Set<String>
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
                    expanded = entry.id in expanded
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
