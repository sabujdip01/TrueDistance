package sabuj.m.truedistance.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import sabuj.m.truedistance.database.DistanceSnapshot
import sabuj.m.truedistance.database.HistoryEntry
import sabuj.m.truedistance.repository.HistoryRepository
import sabuj.m.truedistance.repository.SettingsRepository
import sabuj.m.truedistance.utils.DistanceCalculator
import sabuj.m.truedistance.utils.LocationTrackingHelper
import sabuj.m.truedistance.utils.NotificationHelper
import java.util.UUID
import javax.inject.Inject

/**
 * §6.1.4 / §12 Tech Notes — foreground service owning live location tracking so it
 * survives the app being backgrounded. Controlled via explicit start/stop Intents
 * (not bound), state published through TrackingStateHolder for the UI to observe.
 */
@AndroidEntryPoint
class TrackingService : LifecycleService() {

    @Inject lateinit var historyRepository: HistoryRepository
    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var stateHolder: TrackingStateHolder

    private lateinit var locationHelper: LocationTrackingHelper
    private var trackingJob: Job? = null
    private var staleCheckJob: Job? = null

    private var historyEntryId: String? = null
    private var startedAt: Long = 0L
    private var lastFixAt: Long = 0L
    private var destinationName: String = ""
    private var savedLocationId: String? = null

    override fun onCreate() {
        super.onCreate()
        locationHelper = LocationTrackingHelper(this)
        NotificationHelper.createChannel(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        when (intent?.action) {
            ACTION_STOP -> {
                stopTracking()
                return Service.START_NOT_STICKY
            }
            else -> {
                val name = intent?.getStringExtra(EXTRA_DEST_NAME) ?: return START_NOT_STICKY
                val lat = intent.getDoubleExtra(EXTRA_DEST_LAT, 0.0)
                val lng = intent.getDoubleExtra(EXTRA_DEST_LNG, 0.0)
                savedLocationId = intent.getStringExtra(EXTRA_SAVED_LOCATION_ID)
                startTracking(name, lat, lng)
            }
        }
        return START_STICKY
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun startTracking(name: String, destLat: Double, destLng: Double) {
        destinationName = name
        startedAt = System.currentTimeMillis()
        val destination = LatLng(destLat, destLng)

        // Clear stale state from previous tracking session (markers, polyline data, etc.)
        stateHolder.reset()

        stateHolder.update {
            it.copy(destination = destination, destinationName = name, isTracking = true)
        }

        startForeground(
            NotificationHelper.NOTIFICATION_ID,
            NotificationHelper.buildNotification(this, name, "…", sticky = true)
        )

        trackingJob = lifecycleScope.launch {
            combine(
                settingsRepository.gpsAccuracyMode,
                settingsRepository.updateFrequencySeconds
            ) { acc, freq -> Pair(acc, freq) }
                .flatMapLatest { (accuracyMode, intervalSeconds) ->
                    locationHelper.observeLocation(intervalSeconds, accuracyMode)
                }.collect { location ->
                lastFixAt = System.currentTimeMillis()
                val distance = DistanceCalculator.haversineMeters(
                    location.latitude, location.longitude, destLat, destLng
                )
                val unit = settingsRepository.unit.first()
                val precision = settingsRepository.decimalPrecision.first()
                val autoMeters = settingsRepository.autoMetersUnder1km.first()
                val formatted = DistanceCalculator.format(distance, unit, precision, autoMeters)

                stateHolder.update {
                    it.copy(
                        currentLocation = LatLng(location.latitude, location.longitude),
                        distanceMeters = distance,
                        formattedDistance = formatted,
                        staleFix = false
                    )
                }

                updateNotification(formatted)
                persistSnapshot(distance)

                // Auto-stop when within ~10 meters of the destination
                if (DistanceCalculator.isDestinationReached(distance)) {
                    stateHolder.update { it.copy(destinationReached = true) }
                    stopTracking()
                    return@collect
                }
            }
        }

        staleCheckJob = lifecycleScope.launch {
            while (true) {
                kotlinx.coroutines.delay(5_000)
                val stale = lastFixAt != 0L && System.currentTimeMillis() - lastFixAt > 20_000
                stateHolder.update { it.copy(staleFix = stale) }
            }
        }
    }

    /**
     * Records a raw (timestamp, distanceMeters) sample for the current session.
     * Every GPS fix is persisted — the display layer (DistanceSnapshotFormatter)
     * selects which samples to show post-hoc using time-based tiers.
     */
    private suspend fun persistSnapshot(distance: Double) {
        val id = historyEntryId
        if (id == null) {
            val newId = UUID.randomUUID().toString()
            historyEntryId = newId
            historyRepository.startSession(
                HistoryEntry(
                    id = newId,
                    destinationName = destinationName,
                    destinationLat = stateHolder.state.value.destination?.latitude ?: 0.0,
                    destinationLng = stateHolder.state.value.destination?.longitude ?: 0.0,
                    initialDistanceMeters = distance,
                    startedAt = startedAt,
                    savedLocationId = savedLocationId
                )
            )
        }
        // Record every sample with its real timestamp — no percentage computation here
        recordSnapshot(id ?: historyEntryId!!, distance)
    }

    private suspend fun recordSnapshot(entryId: String, distance: Double) {
        historyRepository.recordSnapshot(
            DistanceSnapshot(
                historyEntryId = entryId,
                timestamp = System.currentTimeMillis(),
                elapsedPercent = 0,  // Legacy field — unused; display uses timestamps now
                distanceMeters = distance
            )
        )
    }

    private fun updateNotification(formattedDistance: String) {
        val manager = getSystemService(android.app.NotificationManager::class.java)
        manager?.notify(
            NotificationHelper.NOTIFICATION_ID,
            NotificationHelper.buildNotification(this, destinationName, formattedDistance, sticky = true)
        )
    }

    private fun stopTracking() {
        lifecycleScope.launch {
            val id = historyEntryId
            if (id != null) {
                val entry = historyRepository.getById(id)
                if (entry != null) {
                    historyRepository.updateSession(
                        entry.copy(
                            finalDistanceMeters = stateHolder.state.value.distanceMeters,
                            endedAt = System.currentTimeMillis()
                        )
                    )
                }
                recordSnapshot(id, stateHolder.state.value.distanceMeters)
            }

            trackingJob?.cancel()
            staleCheckJob?.cancel()
            historyEntryId = null
            stateHolder.update { it.copy(isTracking = false) }

            stopForeground(STOP_FOREGROUND_REMOVE)
            // Explicitly cancel the notification in case updateNotification() re-posted it
            val manager = getSystemService(android.app.NotificationManager::class.java)
            manager?.cancel(NotificationHelper.NOTIFICATION_ID)
            stopSelf()
        }
    }

    companion object {
        const val ACTION_STOP = "sabuj.m.truedistance.action.STOP_TRACKING"
        const val EXTRA_DEST_NAME = "extra_dest_name"
        const val EXTRA_DEST_LAT = "extra_dest_lat"
        const val EXTRA_DEST_LNG = "extra_dest_lng"
        const val EXTRA_SAVED_LOCATION_ID = "extra_saved_location_id"
    }
}
