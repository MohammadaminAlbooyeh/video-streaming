package it.bollofacile.domain

import it.bollofacile.util.daysUntil
import it.bollofacile.util.maxDaysInMonth
import it.bollofacile.data.repository.VeicoloRepository
import it.bollofacile.rules.BolloRules2026
import it.bollofacile.rules.RevisioneRules
import it.bollofacile.rules.model.BolloResult
import it.bollofacile.rules.model.RevisioneResult
import it.bollofacile.rules.model.Veicolo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

data class ScadenzaVeicolo(
    val veicolo: Veicolo,
    val bolloResult: BolloResult,
    val revisioneResult: RevisioneResult,
    val giorniBollo: Int,
    val giorniRevisione: Int,
    val isBolloScaduto: Boolean,
    val isRevisioneScaduta: Boolean
)

class ProssimeScadenzeUseCase @Inject constructor(
    private val veicoloRepository: VeicoloRepository,
    private val calcolaBolloUseCase: CalcolaBolloUseCase,
    private val calcolaRevisioneUseCase: CalcolaRevisioneUseCase
) {
    
    operator fun invoke(): Flow<List<ScadenzaVeicolo>> {
        return veicoloRepository.getAll().map { veicoli ->
            veicoli.map { veicolo ->
                val bolloResult = calcolaBolloUseCase(veicolo)
                val revisioneResult = calcolaRevisioneUseCase(veicolo)
                
                val giorniBollo = LocalDate.now().daysUntil(bolloResult.scadenza).toInt()
                val giorniRevisione = LocalDate.now().daysUntil(revisioneResult.prossimaScadenza).toInt()
                
                ScadenzaVeicolo(
                    veicolo = veicolo,
                    bolloResult = bolloResult,
                    revisioneResult = revisioneResult,
                    giorniBollo = giorniBollo,
                    giorniRevisione = giorniRevisione,
                    isBolloScaduto = giorniBollo < 0,
                    isRevisioneScaduta = giorniRevisione < 0
                )
            }.sortedBy { it.giorniBollo.coerceAtLeast(0) }
        }
    }
    
    fun getUrgenti(giorni: Int = 30): Flow<List<ScadenzaVeicolo>> {
        return invoke().map { it.filter { 
            it.giorniBollo <= giorni && it.giorniBollo >= 0 || 
            it.giorniRevisione <= giorni && it.giorniRevisione >= 0 
        }}
    }
}