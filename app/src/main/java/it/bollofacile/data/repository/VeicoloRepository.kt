package it.bollofacile.data.repository

import it.bollofacile.data.db.VeicoloEntity
import it.bollofacile.rules.model.Veicolo
import kotlinx.coroutines.flow.Flow

interface VeicoloRepository {
    suspend fun insert(veicolo: Veicolo): Long
    suspend fun update(veicolo: Veicolo)
    suspend fun delete(id: Long)
    fun getById(id: Long): Flow<Veicolo?>
    fun getAll(): Flow<List<Veicolo>>
    fun getByTarga(targa: String): Flow<Veicolo?>
    suspend fun count(): Int
}