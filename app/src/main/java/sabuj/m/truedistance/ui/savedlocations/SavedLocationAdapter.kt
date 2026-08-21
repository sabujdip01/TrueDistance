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
        
        // Cycle through pastel gradients
        val backgroundRes = when (position % 3) {
            0 -> sabuj.m.truedistance.R.drawable.bg_card_mint
            1 -> sabuj.m.truedistance.R.drawable.bg_card_lavender
            else -> sabuj.m.truedistance.R.drawable.bg_card_peach
        }
        holder.binding.rootContainer.setBackgroundResource(backgroundRes)

        // Dark text colour matching the card
        val darkColor = when (position % 3) {
            0 -> 0xFF00695C.toInt()    // deep teal
            1 -> 0xFF6A1B9A.toInt()    // deep purple
            else -> 0xFFBF360C.toInt() // deep orange
        }
        holder.binding.nameText.setTextColor(darkColor)
        // Address: same hue but lighter (60% alpha)
        val lightDarkColor = (darkColor and 0x00FFFFFF) or (0x99 shl 24)
        holder.binding.addressText.setTextColor(lightDarkColor)

        holder.binding.root.setOnClickListener { onItemClick(item) }
        holder.binding.deleteButton.setOnClickListener { onDeleteClick(item) }
    }

    object DiffCallback : DiffUtil.ItemCallback<SavedLocation>() {
        override fun areItemsTheSame(old: SavedLocation, new: SavedLocation) = old.id == new.id
        override fun areContentsTheSame(old: SavedLocation, new: SavedLocation) = old == new
    }
}
