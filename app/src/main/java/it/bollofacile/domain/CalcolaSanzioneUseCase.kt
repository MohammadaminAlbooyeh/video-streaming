package it.bollofacile.domain

import it.bollofacile.rules.SanzioniRules
import it.bollofacile.rules.model.SanzioneResult
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class CalcolaSanzioneUseCase @Inject constructor() {
    operator fun invoke(
        importoBollo: Double,
        dataScadenza: LocalDate,
        dataPagamento: LocalDate = LocalDate.now()
    ): SanzioneResult {
        return SanzioniRules.calcolaSanzione(importoBollo, dataScadenza, dataPagamento)
    }
    
    fun getProssimeScadenzeRavvedimento(dataScadenza: LocalDate): List<SanzioniRules.ScadenzaRavvedimento> {
        return SanzioniRules.getProssimeScadenzeRavvedimento(dataScadenza)
    }
}