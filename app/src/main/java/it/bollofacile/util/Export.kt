package it.bollofacile.util

import android.content.Context
import it.bollofacile.data.db.PagamentoEntity
import it.bollofacile.rules.model.Veicolo
import java.io.File

object Export {

    fun buildCsv(veicoli: List<Veicolo>, pagamenti: List<PagamentoEntity>): String = buildString {
        appendLine("VEICOLI")
        appendLine("id;targa;marca;modello;kw;cv;classeEuro;alimentazione;dataImmatricolazione;regione;storico;note")
        veicoli.forEach { v ->
            appendLine(
                listOf(
                    v.id, v.targa.orEmpty(), v.marca, v.modello, v.kw, v.cv,
                    v.classeEuro.name, v.alimentazione.name, v.dataImmatricolazione,
                    v.regione.name, v.isStorico, v.note
                ).joinToString(";") { it.toString().replace(";", ",") }
            )
        }
        appendLine()
        appendLine("PAGAMENTI")
        appendLine("id;veicoloId;tipo;importo;dataPagamento;dataScadenza;metodo;riferimento;note")
        pagamenti.forEach { p ->
            appendLine(
                listOf(
                    p.id, p.veicoloId, p.tipo.name, p.importo,
                    p.dataPagamento, p.dataScadenza, p.metodoPagamento, p.riferimento, p.note
                ).joinToString(";") { it.toString().replace(";", ",") }
            )
        }
    }

    fun writeCsvFile(context: Context, csv: String): File {
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, "bollofacile_export.csv")
        file.writeText(csv)
        return file
    }
}
