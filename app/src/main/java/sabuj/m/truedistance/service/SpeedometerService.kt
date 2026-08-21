package sabuj.m.truedistance.service

import android.app.Service
import android.content.Intent
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
 * §6.2 / §12 — Foreground service for live Speedometer trip tracking.
 * Calculates live speed, distance, elapsed time, average/max speed with spike filtering,
 * and maintains the GPS breadcrumb path.
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

        startForeground(
            NotificationHelper.SPEEDOMETER_NOTIFICATION_ID,
            NotificationHelper.buildSpeedometerNotification(
                this, "0.0 km/h", "0.00 km", "00:00:00", isPaused = false
            )
        )

        startLocationTracking()
        startTimerLoop()
    }

    private fun startLocationTracking() {
        trackingJob?.cancel()
        trackingJob = lifecycleScope.launch {
            val accuracyMode = settingsRepository.gpsAccuracyMode.first()
            val intervalSeconds = settingsRepository.updateFrequencySeconds.first()

            locationHelper.observeLocation(intervalSeconds, accuracyMode).collect { location ->
                val currentLatLng = LatLng(location.latitude, location.longitude)
                val now = System.currentTimeMillis()

                if (!isPaused) {
                    val filteredSpeed = speedFilter.filter(location.speed, now)
                    currentSpeedMps = filteredSpeed

                    if (filteredSpeed > maxSpeedMps) {
                        maxSpeedMps = filteredSpeed
                    }

                    if (lastLocation != null) {
                        val delta = DistanceCalculator.haversineMeters(
                            lastLocation!!.latitude, lastLocation!!.longitude,
                            currentLatLng.latitude, currentLatLng.longitude
                        )
                        // Ignore tiny GPS jitter (< 1.5m if speed is near zero)
                        if (delta > 1.5 || filteredSpeed > 0.5) {
                            distanceCoveredMeters += delta
                            pathPoints.add(currentLatLng)
                        }
                    } else {
                        pathPoints.add(currentLatLng)
                    }
                    lastLocation = currentLatLng
                }

                val avgSpeed = if (elapsedMillis > 1000) {
                    distanceCoveredMeters / (elapsedMillis / 1000.0)
                } else {
                    0.0
                }

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

    private fun pauseTrip() {
        isPaused = true
        currentSpeedMps = 0.0
        stateHolder.update { it.copy(isPaused = true, currentSpeedMps = 0.0) }
        updateNotification()
    }

    private fun resumeTrip() {
        isPaused = false
        lastTickTime = System.currentTimeMillis()
        stateHolder.update { it.copy(isPaused = false) }
        updateNotification()
    }

    private fun updateNotification() {
        val speedKmh = currentSpeedMps * 3.6
        val distKm = distanceCoveredMeters / 1000.0
        val speedText = String.format(Locale.US, "%.1f km/h", speedKmh)
        val distText = String.format(Locale.US, "%.2f km", distKm)
        val elapsedText = formatElapsed(elapsedMillis)

        val manager = getSystemService(android.app.NotificationManager::class.java)
        manager?.notify(
            NotificationHelper.SPEEDOMETER_NOTIFICATION_ID,
            NotificationHelper.buildSpeedometerNotification(
                this, speedText, distText, elapsedText, isPaused
            )
        )
    }

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

            tripRepository.saveTrip(trip)

            stateHolder.update {
                it.copy(
                    isTracking = false,
                    isPaused = false,
                    currentSpeedMps = 0.0
                )
            }

            stopForeground(STOP_FOREGROUND_REMOVE)
            val manager = getSystemService(android.app.NotificationManager::class.java)
            manager?.cancel(NotificationHelper.SPEEDOMETER_NOTIFICATION_ID)
            stopSelf()
        }
    }

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
