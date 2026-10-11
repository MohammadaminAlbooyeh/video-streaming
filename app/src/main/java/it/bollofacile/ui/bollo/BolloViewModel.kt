package it.bollofacile.ui.bollo

import androidx.lifecycle.ViewModel
import it.bollofacile.domain.CalcolaBolloUseCase
import it.bollofacile.domain.CalcolaRevisioneUseCase
import it.bollofacile.rules.model.Veicolo
import javax.inject.Inject

class BolloViewModel @Inject constructor(
    private val calcolaBolloUseCase: CalcolaBolloUseCase,
    private val calcolaRevisioneUseCase: CalcolaRevisioneUseCase
) : ViewModel() {
    
    fun calcolaBollo(veicolo: Veicolo) = calcolaBolloUseCase(veicolo)
    fun calcolaRevisione(veicolo: Veicolo) = calcolaRevisioneUseCase(veicolo)
}