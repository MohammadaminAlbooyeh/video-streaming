package it.bollofacile.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.bollofacile.data.repository.VeicoloRepository
import it.bollofacile.data.prefs.SettingsStore
import it.bollofacile.domain.ProssimeScadenzeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class HomeViewModel @Inject constructor(
    private val prossimeScadenzeUseCase: ProssimeScadenzeUseCase,
    private val veicoloRepository: VeicoloRepository,
    private val settingsStore: SettingsStore
) : ViewModel() {
    
    val scadenze = prossimeScadenzeUseCase().distinctUntilChanged()
    
    val veicoliCount = veicoloRepository.getAll()
        .map { it.size }
        .distinctUntilChanged()
    
    val isPro = settingsStore.isProEnabled.distinctUntilChanged()
}