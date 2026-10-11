package it.bollofacile.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import it.bollofacile.data.db.converters.Converters
import it.bollofacile.rules.model.Veicolo

@Database(
    entities = [VeicoloEntity::class, PagamentoEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun veicoloDao(): VeicoloDao
    abstract fun pagamentoDao(): PagamentoDao
}