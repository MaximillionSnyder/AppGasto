package com.example.appgasto.billing

import android.app.Activity
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.example.appgasto.data.repository.PreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

data class BillingState(
    val isPro: Boolean = false,
    val priceText: String? = null,
    val connecting: Boolean = false,
    val purchaseInProgress: Boolean = false,
    val purchaseError: Boolean = false,
    val userCanceled: Boolean = false
)

@Singleton
class BillingManager @Inject constructor(
    private val billingClient: BillingClient,
    private val preferencesRepository: PreferencesRepository
) : PurchasesUpdatedListener {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _state = MutableStateFlow(BillingState())
    val state: StateFlow<BillingState> = _state.asStateFlow()

    @Volatile
    private var connected = false

    private var productDetails: ProductDetails? = null

    fun connectAndQuery() {
        if (connected || billingClient.isReady) {
            connected = billingClient.isReady
            if (connected) {
                scope.launch {
                    queryPro()
                    loadPrice()
                }
                return
            }
        }
        _state.value = _state.value.copy(connecting = true)
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                connected = result.responseCode == BillingClient.BillingResponseCode.OK
                if (connected) {
                    scope.launch {
                        queryPro()
                        loadPrice()
                        _state.value = _state.value.copy(connecting = false)
                    }
                } else {
                    _state.value = _state.value.copy(connecting = false)
                }
            }

            override fun onBillingServiceDisconnected() {
                connected = false
            }
        })
    }

    fun requery() = connectAndQuery()

    fun restore() = connectAndQuery()

    private suspend fun queryPro() {
        val (billingResult, purchases) = suspendCancellableCoroutine { cont ->
            billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            ) { result, list ->
                if (cont.isActive) cont.resume(result to list)
            }
        }
        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) return
        val proPurchase = purchases.firstOrNull {
            it.products.contains(BillingConfig.PRODUCT_ID) &&
                it.purchaseState == Purchase.PurchaseState.PURCHASED
        }
        if (proPurchase != null) {
            if (!proPurchase.isAcknowledged) {
                acknowledge(proPurchase.purchaseToken)
            }
            preferencesRepository.setPro(true)
            _state.value = _state.value.copy(isPro = true)
        } else {
            preferencesRepository.setPro(false)
            _state.value = _state.value.copy(isPro = false)
        }
    }

    private suspend fun loadPrice() {
        if (productDetails != null) return
        val (billingResult, result) = suspendCancellableCoroutine { cont ->
            billingClient.queryProductDetailsAsync(
                QueryProductDetailsParams.newBuilder()
                    .setProductList(
                        listOf(
                            QueryProductDetailsParams.Product.newBuilder()
                                .setProductId(BillingConfig.PRODUCT_ID)
                                .setProductType(BillingClient.ProductType.INAPP)
                                .build()
                        )
                    )
                    .build()
            ) { res, detailsResult ->
                if (cont.isActive) cont.resume(res to detailsResult)
            }
        }
        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) return
        val details = result.productDetailsList.firstOrNull { it.productId == BillingConfig.PRODUCT_ID }
        productDetails = details
        val price = details?.oneTimePurchaseOfferDetails?.formattedPrice
        if (price != null) {
            _state.value = _state.value.copy(priceText = price)
        }
    }

    fun launchProPurchase(activity: Activity) {
        val details = productDetails
        if (connected && details != null) {
            launchFlow(activity, details)
        } else {
            _state.value = _state.value.copy(purchaseInProgress = false)
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    connected = result.responseCode == BillingClient.BillingResponseCode.OK
                    if (!connected) {
                        _state.value = _state.value.copy(purchaseError = true)
                        return
                    }
                    scope.launch {
                        loadPrice()
                        productDetails?.let { launchFlow(activity, it) }
                            ?: run { _state.value = _state.value.copy(purchaseError = true) }
                    }
                }

                override fun onBillingServiceDisconnected() {
                    connected = false
                }
            })
        }
    }

    private fun launchFlow(activity: Activity, details: ProductDetails) {
        _state.value = _state.value.copy(
            purchaseInProgress = true,
            purchaseError = false,
            userCanceled = false
        )
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(details)
                        .build()
                )
            )
            .build()
        scope.launch {
            val result = withContext(Dispatchers.Main) {
                billingClient.launchBillingFlow(activity, flowParams)
            }
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                _state.value = _state.value.copy(purchaseInProgress = false, purchaseError = true)
            }
        }
    }

    override fun onPurchasesUpdated(
        result: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                val proPurchase = purchases?.firstOrNull {
                    it.products.contains(BillingConfig.PRODUCT_ID)
                }
                if (proPurchase != null) {
                    handleNewPurchase(proPurchase)
                } else {
                    _state.value = _state.value.copy(purchaseInProgress = false)
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _state.value = _state.value.copy(purchaseInProgress = false, userCanceled = true)
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                _state.value = _state.value.copy(purchaseInProgress = false)
                restore()
            }
            else -> {
                _state.value = _state.value.copy(purchaseInProgress = false, purchaseError = true)
            }
        }
    }

    private fun handleNewPurchase(purchase: Purchase) {
        scope.launch {
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                if (!purchase.isAcknowledged) {
                    acknowledge(purchase.purchaseToken)
                }
                preferencesRepository.setPro(true)
                _state.value = _state.value.copy(
                    isPro = true,
                    purchaseInProgress = false,
                    purchaseError = false
                )
            } else {
                _state.value = _state.value.copy(purchaseInProgress = false)
            }
        }
    }

    private suspend fun acknowledge(purchaseToken: String) =
        suspendCancellableCoroutine<Unit> { cont ->
            billingClient.acknowledgePurchase(
                AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchaseToken)
                    .build()
            ) { result ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK && cont.isActive) {
                    cont.resume(Unit)
                }
            }
        }
}
