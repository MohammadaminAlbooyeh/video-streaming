package it.bollofacile.ui.pro

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.bollofacile.billing.BillingManager
import it.bollofacile.data.prefs.SettingsStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProViewModel @Inject constructor(
    private val billingManager: BillingManager,
    private val settingsStore: SettingsStore
) : ViewModel() {

    val isPro: StateFlow<Boolean> = settingsStore.isProEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val proPrice: StateFlow<String?> = billingManager.proPrice

    init {
        billingManager.initialize()
        viewModelScope.launch {
            billingManager.isPro.collect { purchased ->
                if (purchased) settingsStore.setProEnabled(true)
            }
        }
    }

    fun buyPro(activity: Activity) {
        billingManager.launchPurchaseFlow(activity)
    }

    fun restorePurchases() {
        billingManager.restorePurchases()
    }
}
