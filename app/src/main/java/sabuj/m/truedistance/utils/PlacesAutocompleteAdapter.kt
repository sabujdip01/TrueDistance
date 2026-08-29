package sabuj.m.truedistance.utils

import android.content.Context
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.Toast
import com.google.android.gms.tasks.Tasks
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import java.util.concurrent.TimeUnit

/**
 * §6.1.1a — Custom Filterable ArrayAdapter for AutoCompleteTextView to fetch live place predictions
 * from the Google Places SDK and retrieve latitude/longitude details on selection.
 */
class PlacesAutocompleteAdapter(context: Context) :
    ArrayAdapter<AutocompletePrediction>(context, android.R.layout.simple_expandable_list_item_1),
    Filterable {

    private var resultList: List<AutocompletePrediction> = arrayListOf()
    private val placesClient: PlacesClient = Places.createClient(context)

    companion object {
        private const val TAG = "PlacesAutocomplete"
    }

    override fun getCount(): Int = resultList.size

    override fun getItem(position: Int): AutocompletePrediction = resultList[position]

    override fun getView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
        val view = super.getView(position, convertView, parent)
        val prediction = getItem(position)
        val textView = view.findViewById<android.widget.TextView>(android.R.id.text1)
        textView.text = prediction.getFullText(null)
        return view
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                if (constraint != null) {
                    resultList = getAutocomplete(constraint)
                    results.values = resultList
                    results.count = resultList.size
                }
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged()
                } else {
                    notifyDataSetInvalidated()
                }
            }

            override fun convertResultToString(resultValue: Any?): CharSequence {
                return (resultValue as AutocompletePrediction).getFullText(null)
            }
        }
    }

    private fun getAutocomplete(constraint: CharSequence): List<AutocompletePrediction> {
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(constraint.toString())
            .build()

        return try {
            val task = placesClient.findAutocompletePredictions(request)
            val response = Tasks.await(task, 10, TimeUnit.SECONDS)
            Log.d(TAG, "Autocomplete success: ${response.autocompletePredictions.size} results for '$constraint'")
            response.autocompletePredictions
        } catch (e: Exception) {
            Log.e(TAG, "Autocomplete error for '$constraint': ${e.message}", e)
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                Toast.makeText(context, "Autocomplete error: Check Places API/Network", Toast.LENGTH_SHORT).show()
            }
            emptyList()
        }
    }

    /**
     * Helper to fetch LatLng for a selected prediction.
     */
    fun fetchPlace(prediction: AutocompletePrediction, callback: (Place) -> Unit) {
        val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)
        val request = FetchPlaceRequest.builder(prediction.placeId, placeFields).build()
        placesClient.fetchPlace(request).addOnSuccessListener { response ->
            callback(response.place)
        }
    }
}
