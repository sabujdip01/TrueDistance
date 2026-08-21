package sabuj.m.truedistance.ui.savedlocations

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import sabuj.m.truedistance.database.SavedLocation
import sabuj.m.truedistance.databinding.ItemSavedLocationBinding

/** §6.1.2 — list rows: name + address, delete icon, tap to select as destination. */
class SavedLocationAdapter(
    private val onItemClick: (SavedLocation) -> Unit,
    private val onDeleteClick: (SavedLocation) -> Unit
) : ListAdapter<SavedLocation, SavedLocationAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(val binding: ItemSavedLocationBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSavedLocationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.nameText.text = item.name
        holder.binding.addressText.text = item.address
        
        // Cycle through pastel gradients (mint/lavender/peach per spec §2.1)
        val backgroundRes = when (position % 3) {
            0 -> sabuj.m.truedistance.R.drawable.bg_card_mint
            1 -> sabuj.m.truedistance.R.drawable.bg_card_lavender
            else -> sabuj.m.truedistance.R.drawable.bg_card_peach
        }
        holder.binding.rootContainer.setBackgroundResource(backgroundRes)

        // Theme-aware text colors: in light theme, darker shade of card color for name and slightly lighter for address
        val ctx = holder.binding.root.context
        val isNightMode = (ctx.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
        val (primaryTextColor, secondaryTextColor) = if (isNightMode) {
            val prim = androidx.core.content.ContextCompat.getColor(ctx, sabuj.m.truedistance.R.color.text_charcoal)
            val sec = androidx.core.content.ContextCompat.getColor(ctx, sabuj.m.truedistance.R.color.text_gray_purple)
            prim to sec
        } else {
            val darkColor = when (position % 3) {
                0 -> 0xFF00695C.toInt()    // deep teal matching mint card
                1 -> 0xFF6A1B9A.toInt()    // deep purple matching lavender card
                else -> 0xFFBF360C.toInt() // deep orange matching peach card
            }
            val lightDarkColor = (darkColor and 0x00FFFFFF) or (0xD9 shl 24)
            darkColor to lightDarkColor
        }
        holder.binding.nameText.setTextColor(primaryTextColor)
        holder.binding.addressText.setTextColor(secondaryTextColor)

        holder.binding.root.setOnClickListener { onItemClick(item) }
        holder.binding.deleteButton.setOnClickListener { onDeleteClick(item) }
    }

    object DiffCallback : DiffUtil.ItemCallback<SavedLocation>() {
        override fun areItemsTheSame(old: SavedLocation, new: SavedLocation) = old.id == new.id
        override fun areContentsTheSame(old: SavedLocation, new: SavedLocation) = old == new
    }
}
