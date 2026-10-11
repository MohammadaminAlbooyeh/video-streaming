package it.bollofacile.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VeicoloDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(veicolo: VeicoloEntity): Long

    @Update
    suspend fun update(veicolo: VeicoloEntity): Int

    @Query("DELETE FROM veicoli WHERE id = :id")
    suspend fun delete(id: Long): Int

    @Query("SELECT * FROM veicoli WHERE id = :id")
    fun getById(id: Long): Flow<VeicoloEntity?>

    @Query("SELECT * FROM veicoli ORDER BY createdAt DESC")
    fun getAll(): Flow<List<VeicoloEntity>>

    @Query("SELECT * FROM veicoli WHERE targa = :targa")
    fun getByTarga(targa: String): Flow<VeicoloEntity?>

    @Query("SELECT COUNT(*) FROM veicoli")
    suspend fun count(): Int
}