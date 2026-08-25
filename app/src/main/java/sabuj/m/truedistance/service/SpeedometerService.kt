package sabuj.m.truedistance.service

import android.app.Service
import android.content.Intent
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import sabuj.m.truedistance.database.Trip
import sabuj.m.truedistance.repository.SettingsRepository
import sabuj.m.truedistance.repository.TripRepository
import sabuj.m.truedistance.utils.DistanceCalculator
import sabuj.m.truedistance.utils.LocationTrackingHelper
import sabuj.m.truedistance.utils.NotificationHelper
import sabuj.m.truedistance.utils.SpeedSpikeFilter
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

/**
 * §6.2 / §12 — SpeedometerService is the dedicated Android foreground service for live trip tracking.
 *
 * Responsibilities:
 * 1. Computes live speed, total distance covered, elapsed timer, and average/max speed.
 * 2. Applies spike filtering via SpeedSpikeFilter to eliminate GPS jump anomalies.
 * 3. Records path coordinates for live breadcrumb polyline rendering on Google Maps.
 * 4. Publishes real-time state to SpeedometerStateHolder for reactive UI updates.
 * 5. Maintains an interactive status bar foreground notification with live stats, Pause/Resume, and Stop actions.
 * 6. Serializes and persists completed trips with full polyline JSON to Room database.
 */
@AndroidEntryPoint
class SpeedometerService : LifecycleService() {

    @Inject lateinit var tripRepository: TripRepository
    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var stateHolder: SpeedometerStateHolder

    private lateinit var locationHelper: LocationTrackingHelper
    private val speedFilter = SpeedSpikeFilter()

    private var trackingJob: Job? = null
    private var timerJob: Job? = null

    private var tripId: String = UUID.randomUUID().toString()
    private var startedAt: Long = 0L
    private var isPaused: Boolean = false

    private var elapsedMillis: Long = 0L
    private var lastTickTime: Long = 0L

    private var distanceCoveredMeters: Double = 0.0
    private var maxSpeedMps: Double = 0.0
    private var currentSpeedMps: Double = 0.0
    private var lastLocation: LatLng? = null
    private val pathPoints = mutableListOf<LatLng>()

    override fun onCreate() {
        super.onCreate()
        locationHelper = LocationTrackingHelper(this)
        NotificationHelper.createChannel(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        // Handle incoming action intents from UI and notification drawer buttons
        when (intent?.action) {
            ACTION_START -> {
                startTrip()
            }
            ACTION_PAUSE -> {
                pauseTrip()
            }
            ACTION_RESUME -> {
                resumeTrip()
            }
            ACTION_STOP -> {
                stopTrip()
                return Service.START_NOT_STICKY
            }
        }
        return START_STICKY
    }

    /**
     * Initializes and starts a new trip session:
     * - Resets all counters, filters, and timer states.
     * - Starts the foreground service with ongoing status bar notification.
     * - Launches location observation and timer coroutines.
     */
    private fun startTrip() {
        tripId = UUID.randomUUID().toString()
        startedAt = System.currentTimeMillis()
        lastTickTime = startedAt
        isPaused = false
        elapsedMillis = 0L
        distanceCoveredMeters = 0.0
        maxSpeedMps = 0.0
        currentSpeedMps = 0.0
        lastLocation = null
        pathPoints.clear()
        speedFilter.reset()

        stateHolder.reset()
        stateHolder.update {
            it.copy(
                isTracking = true,
                isPaused = false,
                startedAtMillis = startedAt,
                pathPoints = emptyList()
            )
        }

        // Elevate to foreground service with interactive status bar notification
        startForeground(
            NotificationHelper.SPEEDOMETER_NOTIFICATION_ID,
            NotificationHelper.buildSpeedometerNotification(
                this, "000 M/H", "000 M", "00:00:00", isPaused = false
            )
        )

        startLocationTracking()
        startTimerLoop()
    }

    /**
     * Subscribes to live GPS updates reacting dynamically to accuracy & interval preferences.
     * Filters speeds, accumulates distance, and updates polyline points.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun startLocationTracking() {
        trackingJob?.cancel()
        trackingJob = lifecycleScope.launch {
            combine(
                settingsRepository.gpsAccuracyMode,
                settingsRepository.updateFrequencySeconds
            ) { acc, freq -> Pair(acc, freq) }
                .flatMapLatest { (accuracyMode, intervalSeconds) ->
                    locationHelper.observeLocation(intervalSeconds, accuracyMode)
                }.collect { location ->
                val currentLatLng = LatLng(location.latitude, location.longitude)
                val now = System.currentTimeMillis()

                if (!isPaused) {
                    val isAccuracyAcceptable = !location.hasAccuracy() || location.accuracy <= 30.0f
                    val rawSpeed = if (location.hasSpeed() && isAccuracyAcceptable) location.speed else 0.0f
                    val filteredSpeed = speedFilter.filter(rawSpeed, now)
                    currentSpeedMps = filteredSpeed

                    if (filteredSpeed > maxSpeedMps) {
                        maxSpeedMps = filteredSpeed
                    }

                    if (lastLocation != null) {
                        val delta = DistanceCalculator.haversineMeters(
                            lastLocation!!.latitude, lastLocation!!.longitude,
                            currentLatLng.latitude, currentLatLng.longitude
                        )
                        val accuracy = if (location.hasAccuracy()) location.accuracy.toDouble() else 10.0
                        val minDistanceThreshold = kotlin.math.max(4.0, accuracy * 0.5)

                        // Only accumulate distance and polyline if user is genuinely moving
                        if (filteredSpeed > 0.5 && delta >= minDistanceThreshold && isAccuracyAcceptable) {
                            distanceCoveredMeters += delta
                            pathPoints.add(currentLatLng)
                            lastLocation = currentLatLng
                        }
                    } else {
                        if (isAccuracyAcceptable) {
                            pathPoints.add(currentLatLng)
                            lastLocation = currentLatLng
                        }
                    }
                }

                val avgSpeed = if (elapsedMillis > 1000) {
                    distanceCoveredMeters / (elapsedMillis / 1000.0)
                } else {
                    0.0
                }

                // Push updated metrics to UI state holder
                stateHolder.update {
                    it.copy(
                        currentSpeedMps = currentSpeedMps,
                        maxSpeedMps = maxSpeedMps,
                        averageSpeedMps = avgSpeed,
                        distanceCoveredMeters = distanceCoveredMeters,
                        currentLocation = currentLatLng,
                        pathPoints = pathPoints.toList(),
                        staleFix = false
                    )
                }
            }
        }
    }

    /**
     * Ticks every second to accumulate active elapsed time and recompute average speed.
     */
    private fun startTimerLoop() {
        timerJob?.cancel()
        timerJob = lifecycleScope.launch {
            while (true) {
                delay(1000)
                val now = System.currentTimeMillis()
                if (!isPaused) {
                    val delta = now - lastTickTime
                    elapsedMillis += delta

                    val avgSpeed = if (elapsedMillis > 1000) {
                        distanceCoveredMeters / (elapsedMillis / 1000.0)
                    } else {
                        0.0
                    }

                    stateHolder.update {
                        it.copy(
                            elapsedMillis = elapsedMillis,
                            averageSpeedMps = avgSpeed
                        )
                    }

                    updateNotification()
                }
                lastTickTime = now
            }
        }
    }

    /**
     * Freezes trip tracking and zeros out instantaneous speed while preserving distance and elapsed time.
     */
    private fun pauseTrip() {
        isPaused = true
        currentSpeedMps = 0.0
        stateHolder.update { it.copy(isPaused = true, currentSpeedMps = 0.0) }
        updateNotification()
    }

    /**
     * Resumes an active trip session.
     */
    private fun resumeTrip() {
        isPaused = false
        lastTickTime = System.currentTimeMillis()
        stateHolder.update { it.copy(isPaused = false) }
        updateNotification()
    }

    /**
     * Refreshes the foreground notification with formatted live speed, distance, and timer.
     */
    private fun updateNotification() {
        val speedKmh = currentSpeedMps * 3.6
        val speedFormatted = DistanceCalculator.formatSpeedString(speedKmh, sabuj.m.truedistance.database.UnitPreference.KM)
        val distFormatted = DistanceCalculator.format(distanceCoveredMeters, sabuj.m.truedistance.database.UnitPreference.KM, 2, true)
        val elapsedText = formatElapsed(elapsedMillis)

        val manager = getSystemService(android.app.NotificationManager::class.java)
        manager?.notify(
            NotificationHelper.SPEEDOMETER_NOTIFICATION_ID,
            NotificationHelper.buildSpeedometerNotification(
                this, speedFormatted, distFormatted, elapsedText, isPaused
            )
        )
    }

    /**
     * Finalizes the trip, serializes the breadcrumb polyline to JSON, saves to Room database,
     * removes the foreground notification, and stops the service.
     */
    private fun stopTrip() {
        lifecycleScope.launch {
            trackingJob?.cancel()
            timerJob?.cancel()

            val endedAt = System.currentTimeMillis()
            val avgSpeedKmh = if (elapsedMillis > 1000) {
                (distanceCoveredMeters / (elapsedMillis / 1000.0)) * 3.6
            } else {
                0.0
            }
            val maxSpeedKmh = maxSpeedMps * 3.6

            val startPoint = pathPoints.firstOrNull()
            val endPoint = pathPoints.lastOrNull()

            // Serialize recorded breadcrumb points to JSON array
            val pointsJson = JSONArray().apply {
                pathPoints.forEach { pt ->
                    put(JSONObject().apply {
                        put("lat", pt.latitude)
                        put("lng", pt.longitude)
                    })
                }
            }.toString()

            val trip = Trip(
                id = tripId,
                startedAt = startedAt,
                endedAt = endedAt,
                distanceMeters = distanceCoveredMeters,
                elapsedMillis = elapsedMillis,
                averageSpeedKmh = avgSpeedKmh,
                maxSpeedKmh = maxSpeedKmh,
                startLat = startPoint?.latitude ?: 0.0,
                startLng = startPoint?.longitude ?: 0.0,
                endLat = endPoint?.latitude,
                endLng = endPoint?.longitude,
                pathPointsJson = pointsJson
            )

            // Persist completed trip to Room database
            tripRepository.saveTrip(trip)

            val savedLoc = lastLocation
            stateHolder.reset()
            stateHolder.update {
                it.copy(
                    isTracking = false,
                    isPaused = false,
                    currentLocation = savedLoc,
                    currentSpeedMps = 0.0
                )
            }

            stopForeground(STOP_FOREGROUND_REMOVE)
            val manager = getSystemService(android.app.NotificationManager::class.java)
            manager?.cancel(NotificationHelper.SPEEDOMETER_NOTIFICATION_ID)
            stopSelf()
        }
    }

    /**
     * Helper to format milliseconds to HH:MM:SS string.
     */
    private fun formatElapsed(millis: Long): String {
        val totalSec = millis / 1000
        val hrs = totalSec / 3600
        val mins = (totalSec % 3600) / 60
        val secs = totalSec % 60
        return String.format(Locale.US, "%02d:%02d:%02d", hrs, mins, secs)
    }

    companion object {
        const val ACTION_START = "sabuj.m.truedistance.action.START_SPEEDOMETER"
        const val ACTION_PAUSE = "sabuj.m.truedistance.action.PAUSE_SPEEDOMETER"
        const val ACTION_RESUME = "sabuj.m.truedistance.action.RESUME_SPEEDOMETER"
        const val ACTION_STOP = "sabuj.m.truedistance.action.STOP_SPEEDOMETER"
    }
}
