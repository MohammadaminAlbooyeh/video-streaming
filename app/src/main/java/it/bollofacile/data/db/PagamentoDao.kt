package it.bollofacile.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PagamentoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pagamento: PagamentoEntity): Long

    @Query("SELECT * FROM pagamenti WHERE veicoloId = :veicoloId ORDER BY dataPagamento DESC")
    fun getByVeicoloId(veicoloId: Long): Flow<List<PagamentoEntity>>

    @Query("SELECT * FROM pagamenti WHERE veicoloId = :veicoloId AND tipo = :tipo ORDER BY dataPagamento DESC")
    fun getByVeicoloIdAndTipo(veicoloId: Long, tipo: PagamentoEntity.TipoPagamento): Flow<List<PagamentoEntity>>

    @Query("SELECT * FROM pagamenti WHERE dataScadenza BETWEEN :start AND :end ORDER BY dataScadenza")
    fun getScadenzeInRange(start: kotlinx.datetime.LocalDate, end: kotlinx.datetime.LocalDate): Flow<List<PagamentoEntity>>

    @Query("SELECT SUM(importo) FROM pagamenti WHERE veicoloId = :veicoloId AND tipo = :tipo AND strftime('%Y', dataPagamento) = :anno")
    suspend fun getTotaleAnnuale(veicoloId: Long, tipo: PagamentoEntity.TipoPagamento, anno: String): Double?

    @Query("DELETE FROM pagamenti WHERE id = :id")
    suspend fun delete(id: Long): Int
}