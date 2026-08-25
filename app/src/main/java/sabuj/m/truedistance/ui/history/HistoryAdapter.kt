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
 * §6.1.3 Distance History Adapter — Sectioned RecyclerView adapter rendering sticky date group headers
 * and 80/20 card rows with rotating gradient backgrounds, dynamic contrast typography, and lazy snapshot expansion.
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

    fun getItemAt(position: Int): HistoryListItem? = items.getOrNull(position)

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

            // --- Card colour (mint/lavender/peach/blue cycle) ---
            val backgroundRes = when (bindingAdapterPosition % 4) {
                0 -> sabuj.m.truedistance.R.drawable.bg_card_mint
                1 -> sabuj.m.truedistance.R.drawable.bg_card_lavender
                2 -> sabuj.m.truedistance.R.drawable.bg_card_peach
                else -> sabuj.m.truedistance.R.drawable.bg_card_blue
            }
            binding.rootContainer.setBackgroundResource(backgroundRes)

            // Text colors: in light theme, darker shade of card color for row 1 and slightly lighter for row 2
            val isNightMode = (ctx.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
            val (primaryTextColor, secondaryTextColor) = if (isNightMode) {
                val prim = androidx.core.content.ContextCompat.getColor(ctx, sabuj.m.truedistance.R.color.text_charcoal)
                val sec = androidx.core.content.ContextCompat.getColor(ctx, sabuj.m.truedistance.R.color.text_gray_purple)
                prim to sec
            } else {
                val darkColor = when (bindingAdapterPosition % 4) {
                    0 -> 0xFF00695C.toInt()    // deep teal
                    1 -> 0xFF6A1B9A.toInt()    // deep purple
                    2 -> 0xFFBF360C.toInt()    // deep orange
                    else -> 0xFF1565C0.toInt() // deep blue
                }
                val lightDarkColor = (darkColor and 0x00FFFFFF) or (0xD9 shl 24)
                darkColor to lightDarkColor
            }

            binding.destinationText.setTextColor(primaryTextColor)
            binding.trackedDistanceText.setTextColor(primaryTextColor)
            binding.deleteButton.imageTintList = android.content.res.ColorStateList.valueOf(primaryTextColor)

            // Row 2: secondary color (lighter shade / 60% alpha)
            binding.startTimeText.setTextColor(secondaryTextColor)
            binding.stopTimeText.setTextColor(secondaryTextColor)
            binding.elapsedText.setTextColor(secondaryTextColor)

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
                    setBackgroundColor(if (isNightMode) 0x33FFFFFF else (primaryTextColor and 0x00FFFFFF) or (0x22 shl 24))
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
                        setTextColor(primaryTextColor)
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.2f)
                        setTypeface(null, Typeface.BOLD)
                    }

                    // Column 2: Clock time
                    val clockTv = TextView(ctx).apply {
                        text = row.clockTime
                        textSize = 12f
                        setTextColor(secondaryTextColor)
                        gravity = Gravity.CENTER
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.8f)
                    }

                    // Column 3: Distance
                    val distTv = TextView(ctx).apply {
                        text = DistanceCalculator.format(
                            row.distanceMeters, unit, decimalPrecision, autoMetersUnder1km
                        )
                        textSize = 12f
                        setTextColor(primaryTextColor)
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
