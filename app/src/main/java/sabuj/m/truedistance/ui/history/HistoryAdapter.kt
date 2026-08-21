package sabuj.m.truedistance.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import sabuj.m.truedistance.database.HistoryEntry
import sabuj.m.truedistance.databinding.ItemHistoryEntryBinding
import sabuj.m.truedistance.databinding.ItemHistoryHeaderBinding
import sabuj.m.truedistance.utils.DistanceCalculator
import sabuj.m.truedistance.database.UnitPreference

private const val TYPE_HEADER = 0
private const val TYPE_ENTRY = 1

/**
 * §6.1.3 — plain RecyclerView.Adapter (not ListAdapter) since item shape mixes
 * headers + expandable rows; DiffUtil isn't a great fit here without more
 * bookkeeping. Revisit with ListAdapter + custom DiffCallback if list perf matters.
 */
class HistoryAdapter(
    private val onEntryClick: (HistoryEntry) -> Unit,
    private val onDeleteClick: (HistoryEntry) -> Unit,
    private var unit: UnitPreference = UnitPreference.BOTH,
    private var decimalPrecision: Int = 2,
    private var autoMetersUnder1km: Boolean = true
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items: List<HistoryListItem> = emptyList()

    fun submitList(newItems: List<HistoryListItem>) {
        items = newItems
        notifyDataSetChanged() // simple approach; swap for DiffUtil if lists grow large
    }

    fun updateUnitSettings(unit: UnitPreference, decimalPrecision: Int, autoMetersUnder1km: Boolean) {
        this.unit = unit
        this.decimalPrecision = decimalPrecision
        this.autoMetersUnder1km = autoMetersUnder1km
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is HistoryListItem.DateHeader -> TYPE_HEADER
        is HistoryListItem.EntryRow -> TYPE_ENTRY
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_HEADER) {
            HeaderViewHolder(ItemHistoryHeaderBinding.inflate(inflater, parent, false))
        } else {
            EntryViewHolder(ItemHistoryEntryBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is HistoryListItem.DateHeader -> (holder as HeaderViewHolder).bind(item)
            is HistoryListItem.EntryRow -> (holder as EntryViewHolder).bind(item)
        }
    }

    inner class HeaderViewHolder(private val binding: ItemHistoryHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: HistoryListItem.DateHeader) {
            binding.headerLabel.text = item.label
        }
    }

    inner class EntryViewHolder(private val binding: ItemHistoryEntryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HistoryListItem.EntryRow) {
            val entry = item.entry
            binding.destinationText.text = entry.destinationName
            binding.initialDistanceText.text = DistanceCalculator.format(
                entry.initialDistanceMeters, unit, decimalPrecision, autoMetersUnder1km
            )

            // Show trip date + time + duration in preview
            val dateFormat = java.text.SimpleDateFormat("MMM d, h:mm a", java.util.Locale.getDefault())
            val startTime = dateFormat.format(java.util.Date(entry.startedAt))
            val durationText = if (entry.endedAt != null) {
                val durationMs = entry.endedAt - entry.startedAt
                val mins = durationMs / 60_000
                val secs = (durationMs % 60_000) / 1_000
                if (mins > 0) "  •  ${mins}m ${secs}s" else "  •  ${secs}s"
            } else ""
            binding.dateTimeText.text = "$startTime$durationText"

            // Cycle through pastel gradients
            val backgroundRes = when (bindingAdapterPosition % 3) {
                0 -> sabuj.m.truedistance.R.drawable.bg_card_lavender
                1 -> sabuj.m.truedistance.R.drawable.bg_card_mint
                else -> sabuj.m.truedistance.R.drawable.bg_card_peach
            }
            binding.rootContainer.setBackgroundResource(backgroundRes)

            binding.root.setOnClickListener { onEntryClick(entry) }
            binding.deleteButton.setOnClickListener { onDeleteClick(entry) }

            binding.snapshotContainer.removeAllViews()
            if (item.expanded) {
                binding.snapshotContainer.visibility = View.VISIBLE
                item.snapshotRows.forEach { row ->
                    val rowView = android.widget.TextView(binding.root.context).apply {
                        text = "${row.label}: ${DistanceCalculator.format(
                            row.distanceMeters, unit, decimalPrecision, autoMetersUnder1km
                        )}"
                        setPadding(0, 8, 0, 0)
                    }
                    binding.snapshotContainer.addView(rowView)
                }
            } else {
                binding.snapshotContainer.visibility = View.GONE
            }
        }
    }
}
