package it.bollofacile.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.bollofacile.data.prefs.SettingsStore
import it.bollofacile.data.repository.VeicoloRepository
import it.bollofacile.domain.ProssimeScadenzeUseCase
import it.bollofacile.domain.ScadenzaVeicolo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    prossimeScadenzeUseCase: ProssimeScadenzeUseCase,
    veicoloRepository: VeicoloRepository,
    settingsStore: SettingsStore
) : ViewModel() {

    val scadenze: StateFlow<List<ScadenzaVeicolo>> = prossimeScadenzeUseCase()
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val veicoliCount: StateFlow<Int> = veicoloRepository.getAll()
        .map { it.size }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val isPro: StateFlow<Boolean> = settingsStore.isProEnabled
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
}
