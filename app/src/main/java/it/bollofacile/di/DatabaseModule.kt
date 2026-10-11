package it.bollofacile.di

import androidx.room.RoomDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import it.bollofacile.data.db.AppDatabase
import it.bollofacile.data.db.VeicoloDao
import it.bollofacile.data.db.PagamentoDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideVeiculoDao(database: AppDatabase): VeicoloDao = database.veicoloDao()

    @Provides
    @Singleton
    fun providePagamentoDao(database: AppDatabase): PagamentoDao = database.pagamentoDao()
}