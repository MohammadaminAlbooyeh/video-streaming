package it.bollofacile.data.repository

import it.bollofacile.data.db.AppDatabase
import it.bollofacile.data.db.VeicoloEntity
import it.bollofacile.rules.model.Veicolo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VeicoloRepositoryImpl @Inject constructor(
    private val database: AppDatabase
) : VeicoloRepository {

    override suspend fun insert(veicolo: Veicolo): Long {
        return database.veicoloDao().insert(veicolo.toEntity())
    }

    override suspend fun update(veicolo: Veicolo) {
        database.veicoloDao().update(veicolo.toEntity())
    }

    override suspend fun delete(id: Long) {
        database.veicoloDao().delete(id)
    }

    override fun getById(id: Long): Flow<Veicolo?> {
        return database.veicoloDao().getById(id).map { it?.toDomain() }
    }

    override fun getAll(): Flow<List<Veicolo>> {
        return database.veicoloDao().getAll().map { it.map { it.toDomain() } }
    }

    override fun getByTarga(targa: String): Flow<Veicolo?> {
        return database.veicoloDao().getByTarga(targa).map { it?.toDomain() }
    }

    override suspend fun count(): Int {
        return database.veicoloDao().count()
    }
}