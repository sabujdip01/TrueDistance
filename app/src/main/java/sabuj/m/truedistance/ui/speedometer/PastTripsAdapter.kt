package sabuj.m.truedistance.ui.speedometer

import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap
import org.json.JSONArray
import sabuj.m.truedistance.R
import sabuj.m.truedistance.database.AppSettingsKeys
import sabuj.m.truedistance.database.Trip
import sabuj.m.truedistance.databinding.ItemPastTripBinding
import sabuj.m.truedistance.utils.DistanceCalculator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * §6.2.2 — Past Trips Adapter with 80/20 card layout and expandable map snapshot.
 */
class PastTripsAdapter(
    private val onItemClick: (Trip) -> Unit,
    private val onDeleteClick: (Trip) -> Unit
) : ListAdapter<PastTripListItem, PastTripsAdapter.ViewHolder>(DiffCallback) {

    private val titleDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
    private val timeFormat = SimpleDateFormat("h:mm:ss a", Locale.US)

    var unit: sabuj.m.truedistance.database.UnitPreference = sabuj.m.truedistance.database.UnitPreference.KM
    var decimalPrecision: Int = 2
    var autoMetersUnder1km: Boolean = true

    inner class ViewHolder(val binding: ItemPastTripBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.mapSnapshotView.onCreate(null)
        }

        fun bind(item: PastTripListItem) {
            val trip = item.trip
            val ctx = binding.root.context

            // Title & formatted distance
            val titleStr = titleDateFormat.format(Date(trip.startedAt))
            binding.tripTitleText.text = titleStr

            val distFormatted = DistanceCalculator.format(
                trip.distanceMeters, unit, decimalPrecision, autoMetersUnder1km
            )
            binding.distanceText.text = distFormatted

            // Row 2: Start time, Elapsed duration, Avg speed
            val startStr = timeFormat.format(Date(trip.startedAt))
            val elapsedStr = formatElapsed(trip.elapsedMillis)

            val avgSpeedFormatted = DistanceCalculator.formatSpeedString(trip.averageSpeedKmh, unit)
            val maxSpeedFormatted = DistanceCalculator.formatSpeedString(trip.maxSpeedKmh, unit)

            binding.startTimeText.text = startStr
            binding.elapsedText.text = elapsedStr
            binding.avgSpeedText.text = "Avg $avgSpeedFormatted"

            // Card background: Cycle through gradient cards
            val backgroundRes = when (bindingAdapterPosition % 3) {
                0 -> R.drawable.bg_card_mint
                1 -> R.drawable.bg_card_peach
                else -> R.drawable.bg_card_lavender
            }
            binding.rootContainer.setBackgroundResource(backgroundRes)

            // Text colors (tone-matched in light mode, high contrast in dark mode)
            val isNightMode = (ctx.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            val (primaryTextColor, secondaryTextColor) = if (isNightMode) {
                val prim = ContextCompat.getColor(ctx, R.color.text_charcoal)
                val sec = ContextCompat.getColor(ctx, R.color.text_gray_purple)
                prim to sec
            } else {
                val darkColor = when (bindingAdapterPosition % 3) {
                    0 -> 0xFF00695C.toInt()    // deep teal
                    1 -> 0xFFBF360C.toInt()    // deep orange
                    else -> 0xFF6A1B9A.toInt() // deep purple
                }
                val lightDarkColor = (darkColor and 0x00FFFFFF) or (0xD9 shl 24)
                darkColor to lightDarkColor
            }

            binding.tripTitleText.setTextColor(primaryTextColor)
            binding.distanceText.setTextColor(primaryTextColor)
            binding.deleteButton.imageTintList = android.content.res.ColorStateList.valueOf(primaryTextColor)

            binding.startTimeText.setTextColor(secondaryTextColor)
            binding.elapsedText.setTextColor(secondaryTextColor)
            binding.avgSpeedText.setTextColor(secondaryTextColor)

            // Single expand container
            if (item.isExpanded) {
                binding.expandedContainer.visibility = View.VISIBLE
                binding.detailMaxSpeedText.setTextColor(secondaryTextColor)
                binding.detailEndedAtText.setTextColor(secondaryTextColor)

                binding.detailMaxSpeedText.text = "Max Speed: $maxSpeedFormatted"
                if (trip.endedAt != null) {
                    binding.detailEndedAtText.text = "Ended: " + timeFormat.format(Date(trip.endedAt))
                } else {
                    binding.detailEndedAtText.text = ""
                }

                setupMapSnapshot(trip)
            } else {
                binding.expandedContainer.visibility = View.GONE
            }

            binding.root.setOnClickListener { onItemClick(trip) }
            binding.deleteButton.setOnClickListener { onDeleteClick(trip) }
        }

        private fun setupMapSnapshot(trip: Trip) {
            val points = parsePoints(trip.pathPointsJson)
            binding.mapSnapshotView.getMapAsync { googleMap ->
                googleMap.clear()
                googleMap.uiSettings.setAllGesturesEnabled(false)
                googleMap.uiSettings.isMapToolbarEnabled = false

                if (points.isNotEmpty()) {
                    val polylineOptions = PolylineOptions()
                        .color(0xFF00796B.toInt())
                        .width(6f)
                        .jointType(JointType.ROUND)
                        .startCap(RoundCap())
                        .endCap(RoundCap())
                        .addAll(points)

                    googleMap.addPolyline(polylineOptions)

                    val startPoint = points.first()
                    val endPoint = points.last()

                    val flagIcon = vectorToBitmapDescriptor(binding.root.context, R.drawable.ic_race_flag)
                    googleMap.addMarker(
                        MarkerOptions()
                            .position(startPoint)
                            .title("Start")
                            .icon(flagIcon)
                            .anchor(0.5f, 0.5f)
                    )

                    if (points.size > 1) {
                        googleMap.addMarker(
                            MarkerOptions()
                                .position(endPoint)
                                .title("End")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        )
                    }

                    val builder = LatLngBounds.Builder()
                    points.forEach { builder.include(it) }
                    try {
                        val bounds = builder.build()
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 40))
                    } catch (e: Exception) {
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPoint, 15f))
                    }
                }
            }
        }

        private fun parsePoints(json: String): List<LatLng> {
            val list = mutableListOf<LatLng>()
            try {
                val array = JSONArray(json)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(LatLng(obj.getDouble("lat"), obj.getDouble("lng")))
                }
            } catch (_: Exception) {}
            return list
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPastTripBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private fun formatElapsed(millis: Long): String {
        val totalSec = millis / 1000
        val hrs = totalSec / 3600
        val mins = (totalSec % 3600) / 60
        val secs = totalSec % 60
        return String.format(Locale.US, "%02d:%02d:%02d", hrs, mins, secs)
    }

    companion object {
        private fun vectorToBitmapDescriptor(context: android.content.Context, @androidx.annotation.DrawableRes vectorResId: Int): BitmapDescriptor {
            val drawable = androidx.core.content.ContextCompat.getDrawable(context, vectorResId)
                ?: return BitmapDescriptorFactory.defaultMarker()
            drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
            val bitmap = android.graphics.Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                android.graphics.Bitmap.Config.ARGB_8888
            )
            val canvas = android.graphics.Canvas(bitmap)
            drawable.draw(canvas)
            return BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<PastTripListItem>() {
        override fun areItemsTheSame(oldItem: PastTripListItem, newItem: PastTripListItem): Boolean {
            return oldItem.trip.id == newItem.trip.id
        }

        override fun areContentsTheSame(oldItem: PastTripListItem, newItem: PastTripListItem): Boolean {
            return oldItem == newItem
        }
    }
}
