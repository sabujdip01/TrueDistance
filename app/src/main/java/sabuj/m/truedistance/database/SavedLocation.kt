package sabuj.m.truedistance.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/** §8 Data Model — SavedLocation */
@Entity(tableName = "saved_locations")
data class SavedLocation(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val createdAt: Long = System.currentTimeMillis()
)
