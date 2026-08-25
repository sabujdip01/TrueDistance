package sabuj.m.truedistance.ui.history

import sabuj.m.truedistance.database.HistoryEntry
import sabuj.m.truedistance.utils.DistanceSnapshotFormatter

/**
 * §6.1.3 Distance History — Polymorphic list item models for sectioned history RecyclerView.
 */
sealed class HistoryListItem {
    /** Sticky date group header (e.g. "Today", "Yesterday", "24 Aug 2026"). */
    data class DateHeader(val label: String) : HistoryListItem()

    /**
     * History session card row.
     *
     * @property entry Underlying [HistoryEntry] entity.
     * @property expanded Whether the snapshot intervals section is currently expanded.
     * @property snapshotRows Formatted display rows for time-tiered distance samples.
     */
    data class EntryRow(
        val entry: HistoryEntry,
        val expanded: Boolean,
        val snapshotRows: List<DistanceSnapshotFormatter.DisplayRow> = emptyList()
    ) : HistoryListItem()
}
