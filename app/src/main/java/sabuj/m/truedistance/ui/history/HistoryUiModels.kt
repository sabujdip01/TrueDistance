package sabuj.m.truedistance.ui.history

import sabuj.m.truedistance.database.HistoryEntry
import sabuj.m.truedistance.utils.DistanceSnapshotFormatter

/** §6.1.3 — list item types: date section headers + expandable entry rows. */
sealed class HistoryListItem {
    data class DateHeader(val label: String) : HistoryListItem()
    data class EntryRow(
        val entry: HistoryEntry,
        val expanded: Boolean,
        val snapshotRows: List<DistanceSnapshotFormatter.DisplayRow> = emptyList()
    ) : HistoryListItem()
}
