package it.bollofacile.domain

import it.bollofacile.rules.BolloRules2026
import it.bollofacile.rules.model.BolloResult
import it.bollofacile.rules.model.Veicolo
import javax.inject.Inject

class CalcolaBolloUseCase @Inject constructor() {
    operator fun invoke(veicolo: Veicolo): BolloResult {
        return BolloRules2026.calcolaBollo(veicolo)
    }
}