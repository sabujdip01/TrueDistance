package sabuj.m.truedistance.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import sabuj.m.truedistance.database.DistanceSnapshotDao
import sabuj.m.truedistance.database.HistoryEntryDao
import sabuj.m.truedistance.database.SavedLocationDao
import sabuj.m.truedistance.database.TrueDistanceDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TrueDistanceDatabase =
        Room.databaseBuilder(
            context,
            TrueDistanceDatabase::class.java,
            "true_distance.db"
        ).build()

    @Provides
    fun provideSavedLocationDao(db: TrueDistanceDatabase): SavedLocationDao =
        db.savedLocationDao()

    @Provides
    fun provideHistoryEntryDao(db: TrueDistanceDatabase): HistoryEntryDao =
        db.historyEntryDao()

    @Provides
    fun provideDistanceSnapshotDao(db: TrueDistanceDatabase): DistanceSnapshotDao =
        db.distanceSnapshotDao()
}
