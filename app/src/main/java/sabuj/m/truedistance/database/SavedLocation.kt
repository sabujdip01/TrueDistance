package sabuj.m.truedistance.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * §8 Data Model — Room Entity representing a user-bookmarked location.
 *
 * @property id Unique UUID string primary key.
 * @property name User-assigned or place-derived friendly name.
 * @property address Formatted street address or reverse-geocoded string.
 * @property latitude Geographic latitude in decimal degrees.
 * @property longitude Geographic longitude in decimal degrees.
 * @property createdAt Timestamp when bookmark was created.
 */
@Entity(tableName = "saved_locations")
data class SavedLocation(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val createdAt: Long = System.currentTimeMillis()
)
