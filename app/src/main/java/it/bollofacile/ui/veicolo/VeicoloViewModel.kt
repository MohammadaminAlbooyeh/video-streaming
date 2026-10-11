package it.bollofacile.ui.veicolo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.bollofacile.data.repository.VeicoloRepository
import it.bollofacile.domain.CalcolaBolloUseCase
import it.bollofacile.domain.CalcolaRevisioneUseCase
import it.bollofacile.rules.model.Veicolo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

class VeicoloViewModel @Inject constructor(
    private val veicoloRepository: VeicoloRepository,
    private val calcolaBolloUseCase: CalcolaBolloUseCase,
    private val calcolaRevisioneUseCase: CalcolaRevisioneUseCase
) : ViewModel() {
    
    fun getVeiculo(id: Long): Flow<Veicolo?> {
        return veicoloRepository.getById(id).distinctUntilChanged()
    }
    
    fun insertVeiculo(veicolo: Veicolo) {
        viewModelScope.launch {
            veicoloRepository.insert(veicolo)
        }
    }
    
    fun updateVeiculo(veicolo: Veicolo) {
        viewModelScope.launch {
            veicoloRepository.update(veicolo)
        }
    }
    
    fun deleteVeiculo(id: Long) {
        viewModelScope.launch {
            veicoloRepository.delete(id)
        }
    }
    
    fun calcolaBollo(veicolo: Veicolo) = calcolaBolloUseCase(veicolo)
    fun calcolaRevisione(veicolo: Veicolo) = calcolaRevisioneUseCase(veicolo)
}