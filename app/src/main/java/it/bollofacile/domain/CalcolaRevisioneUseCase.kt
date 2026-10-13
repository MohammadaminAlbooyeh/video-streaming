package it.bollofacile.domain

import it.bollofacile.rules.RevisioneRules
import it.bollofacile.rules.model.RevisioneResult
import it.bollofacile.rules.model.Veicolo
import javax.inject.Inject

class CalcolaRevisioneUseCase @Inject constructor() {
    operator fun invoke(veicolo: Veicolo, ultimaRevisione: java.time.LocalDate? = null): RevisioneResult {
        return RevisioneRules.calcolaProssimaRevisione(
            dataImmatricolazione = veicolo.dataImmatricolazione,
            ultimaRevisione = ultimaRevisione,
            isStorico = veicolo.isStorico,
            isElettrico = veicolo.alimentazione == it.bollofacile.rules.model.Alimentazione.ELETTRICA,
            regione = veicolo.regione
        )
    }
}