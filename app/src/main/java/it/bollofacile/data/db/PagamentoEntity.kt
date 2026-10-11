package it.bollofacile.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate

@Entity(
    tableName = "pagamenti",
    foreignKeys = [
        ForeignKey(
            entity = VeicoloEntity::class,
            parentColumns = ["id"],
            childColumns = ["veicoloId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("veicoloId")]
)
data class PagamentoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val veicoloId: Long,
    val tipo: TipoPagamento,
    val importo: Double,
    val dataPagamento: LocalDate,
    val dataScadenza: LocalDate,
    val note: String = "",
    val metodoPagamento: String = "",
    val riferimento: String = "",
    val createdAt: LocalDate = LocalDate.now()
) {
    enum class TipoPagamento {
        BOLLO,
        SUPERBOLLO,
        REVISIONE,
        SANZIONE,
        ALTRO
    }
}