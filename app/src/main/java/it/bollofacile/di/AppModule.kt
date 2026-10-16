package it.bollofacile.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import it.bollofacile.data.db.AppDatabase
import it.bollofacile.data.prefs.SettingsStore
import it.bollofacile.data.repository.VeicoloRepository
import it.bollofacile.data.repository.VeicoloRepositoryImpl
import it.bollofacile.domain.CalcolaBolloUseCase
import it.bollofacile.domain.CalcolaRevisioneUseCase
import it.bollofacile.domain.CalcolaSanzioneUseCase
import it.bollofacile.domain.ProssimeScadenzeUseCase
import it.bollofacile.reminder.ReminderScheduler
import it.bollofacile.ads.ConsentManager
import it.bollofacile.ads.AdManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@dagger.hilt.android.qualifiers.ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideVeiculoRepository(impl: VeicoloRepositoryImpl): VeicoloRepository = impl

    @Provides
    @Singleton
    fun provideCalcolaBolloUseCase(): CalcolaBolloUseCase = CalcolaBolloUseCase()

    @Provides
    @Singleton
    fun provideCalcolaRevisioneUseCase(): CalcolaRevisioneUseCase = CalcolaRevisioneUseCase()

    @Provides
    @Singleton
    fun provideCalcolaSanzioneUseCase(): CalcolaSanzioneUseCase = CalcolaSanzioneUseCase()

    @Provides
    @Singleton
    fun provideSettingsStore(@dagger.hilt.android.qualifiers.ApplicationContext context: Context): SettingsStore {
        return SettingsStore(context)
    }

    @Provides
    @Singleton
    fun provideReminderScheduler(
        @dagger.hilt.android.qualifiers.ApplicationContext context: Context,
        settingsStore: SettingsStore,
        scadenzeUseCase: ProssimeScadenzeUseCase
    ): ReminderScheduler {
        return ReminderScheduler(context, settingsStore, scadenzeUseCase)
    }

    @Provides
    @Singleton
    fun provideConsentManager(@dagger.hilt.android.qualifiers.ApplicationContext context: Context): ConsentManager {
        return ConsentManager(context)
    }

    @Provides
    @Singleton
    fun provideAdManager(
        @dagger.hilt.android.qualifiers.ApplicationContext context: Context,
        settingsStore: SettingsStore
    ): AdManager {
        return AdManager(context, settingsStore)
    }
}