package sabuj.m.truedistance.ui.history

import android.graphics.Typeface
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import sabuj.m.truedistance.database.HistoryEntry
import sabuj.m.truedistance.databinding.ItemHistoryEntryBinding
import sabuj.m.truedistance.databinding.ItemHistoryHeaderBinding
import sabuj.m.truedistance.utils.DistanceCalculator
import sabuj.m.truedistance.database.UnitPreference
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TYPE_HEADER = 0
private const val TYPE_ENTRY = 1

/**
 * §6.1.3 — History list adapter with date headers + expandable entry cards.
 */
class HistoryAdapter(
    private val onEntryClick: (HistoryEntry) -> Unit,
    private val onDeleteClick: (HistoryEntry) -> Unit,
    private var unit: UnitPreference = UnitPreference.KM,
    private var decimalPrecision: Int = 2,
    private var autoMetersUnder1km: Boolean = true
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items: List<HistoryListItem> = emptyList()
    private val dateFormat = SimpleDateFormat("MMM dd h:mm a", Locale.getDefault())

    fun submitList(newItems: List<HistoryListItem>) {
        items = newItems
        notifyDataSetChanged()
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
            val ctx = binding.root.context

            // --- Row 1: Destination name + Tracked distance ---
            binding.destinationText.text = entry.destinationName

            // Tracked distance = initialDistance - finalDistance (how much closer they got)
            val trackedDist = if (entry.finalDistanceMeters != null) {
                val diff = entry.initialDistanceMeters - entry.finalDistanceMeters
                DistanceCalculator.format(
                    if (diff > 0) diff else entry.initialDistanceMeters,
                    unit, decimalPrecision, autoMetersUnder1km
                )
            } else {
                DistanceCalculator.format(
                    entry.initialDistanceMeters, unit, decimalPrecision, autoMetersUnder1km
                )
            }
            binding.trackedDistanceText.text = trackedDist

            // --- Row 2: Start time | Stop time | Elapsed ---
            binding.startTimeText.text = dateFormat.format(Date(entry.startedAt))

            if (entry.endedAt != null) {
                binding.stopTimeText.text = dateFormat.format(Date(entry.endedAt))
                val elapsed = entry.endedAt - entry.startedAt
                binding.elapsedText.text = formatDuration(elapsed)
            } else {
                binding.stopTimeText.text = "In Progress"
                binding.elapsedText.text = ""
            }

            // --- Card colour ---
            val backgroundRes = when (bindingAdapterPosition % 3) {
                0 -> sabuj.m.truedistance.R.drawable.bg_card_lavender
                1 -> sabuj.m.truedistance.R.drawable.bg_card_mint
                else -> sabuj.m.truedistance.R.drawable.bg_card_peach
            }
            binding.rootContainer.setBackgroundResource(backgroundRes)

            // Dark text colour matching the card
            val darkColor = when (bindingAdapterPosition % 3) {
                0 -> 0xFF6A1B9A.toInt()   // deep purple
                1 -> 0xFF00695C.toInt()    // deep teal
                else -> 0xFFBF360C.toInt() // deep orange
            }
            binding.destinationText.setTextColor(darkColor)
            binding.trackedDistanceText.setTextColor(darkColor)

            // Row 2: same hue but lighter (60% alpha)
            val lightDarkColor = (darkColor and 0x00FFFFFF) or (0x99 shl 24)
            binding.startTimeText.setTextColor(lightDarkColor)
            binding.stopTimeText.setTextColor(lightDarkColor)
            binding.elapsedText.setTextColor(lightDarkColor)

            binding.root.setOnClickListener { onEntryClick(entry) }
            binding.deleteButton.setOnClickListener { onDeleteClick(entry) }

            // --- Expanded snapshot rows (3-column table) ---
            binding.snapshotContainer.removeAllViews()
            if (item.expanded && item.snapshotRows.isNotEmpty()) {
                binding.snapshotContainer.visibility = View.VISIBLE

                // Add a thin divider
                val divider = View(ctx).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1
                    ).apply { topMargin = 4; bottomMargin = 4 }
                    setBackgroundColor(0x22000000)
                }
                binding.snapshotContainer.addView(divider)

                item.snapshotRows.forEach { row ->
                    val rowLayout = LinearLayout(ctx).apply {
                        orientation = LinearLayout.HORIZONTAL
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply { topMargin = 6 }
                    }

                    // Column 1: Elapsed label
                    val elapsedTv = TextView(ctx).apply {
                        text = row.elapsedLabel
                        textSize = 12f
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.2f)
                        setTypeface(null, Typeface.BOLD)
                    }

                    // Column 2: Clock time
                    val clockTv = TextView(ctx).apply {
                        text = row.clockTime
                        textSize = 12f
                        gravity = Gravity.CENTER
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.8f)
                    }

                    // Column 3: Distance
                    val distTv = TextView(ctx).apply {
                        text = DistanceCalculator.format(
                            row.distanceMeters, unit, decimalPrecision, autoMetersUnder1km
                        )
                        textSize = 12f
                        gravity = Gravity.END
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    }

                    rowLayout.addView(elapsedTv)
                    rowLayout.addView(clockTv)
                    rowLayout.addView(distTv)
                    binding.snapshotContainer.addView(rowLayout)
                }
            } else {
                binding.snapshotContainer.visibility = View.GONE
            }
        }
    }

    /** Formats millis as "H:MM:SS". */
    private fun formatDuration(millis: Long): String {
        val totalSeconds = millis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%d:%02d", minutes, seconds)
        }
    }
}
