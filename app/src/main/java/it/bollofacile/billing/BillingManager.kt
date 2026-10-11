package it.bollofacile.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BillingManager(
    private val context: Context
) {

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases()
        .build()

    private val _isPro = MutableStateFlow(false)
    val isPro = _isPro.asStateFlow()

    private val _purchaseFlow = Channel<Purchase>()
    val purchaseFlow = _purchaseFlow.receiveAsFlow()

    private val skuPro = "pro_lifetime"
    private var billingReady = false

    interface BillingCallback {
        fun onBillingSetupFinished(billingResult: BillingResult)
        fun onPurchaseSuccess(purchase: Purchase)
        fun onPurchaseError(billingResult: BillingResult)
    }

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Utente ha annullato
        } else {
            // Errore
        }
    }

    fun initialize(callback: BillingCallback) {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    billingReady = true
                    queryPurchases()
                }
                callback.onBillingSetupFinished(billingResult)
            }

            override fun onBillingServiceDisconnected() {
                billingReady = false
            }
        })
    }

    fun launchPurchaseFlow(activity: Activity) {
        if (!billingReady) return

        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(getProductDetails())
                        .build()
                )
            )
            .build()

        billingClient.launchBillingFlow(activity, params)
    }

    private fun getProductDetails(): ProductDetails {
        // In produzione, queryProductDetailsAsync per ottenere ProductDetails
        // Qui placeholder
        return ProductDetails.newBuilder().build()
    }

    private fun queryPurchases() {
        CoroutineScope(Dispatchers.IO).launch {
            val result = billingClient.queryPurchases(
                QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            )
            
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                for (purchase in result.purchasesList) {
                    if (purchase.skus.contains(skuPro) && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        _isPro.value = true
                        _purchaseFlow.trySend(purchase)
                    }
                }
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.skus.contains(skuPro) && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            _isPro.value = true
            _purchaseFlow.trySend(purchase)
            
            // Acknowledge purchase
            if (!purchase.isAcknowledged) {
                val params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient.acknowledgePurchase(params) { billingResult ->
                    // Acknowledged
                }
            }
        }
    }

    fun restorePurchases() {
        queryPurchases()
    }

    fun release() {
        billingClient.endConnection()
    }
}