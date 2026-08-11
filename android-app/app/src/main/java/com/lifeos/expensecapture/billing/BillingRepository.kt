package com.lifeos.expensecapture.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.lifeos.expensecapture.App
import com.lifeos.expensecapture.logging.AppLogger
import com.lifeos.expensecapture.util.Prefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/** Product IDs must be created in Play Console (Monetize > Products > Subscriptions) with these
 * exact strings before queryProducts() will return anything - this is a naming contract between
 * this code and the Play Console listing, not something the app can create on its own. Final
 * pricing/naming is your call; these are placeholders matching the free/premium split discussed
 * (unlimited AI financial Q&A + full Family/Smart Split behind the paid tier). */
object BillingProducts {
    const val PREMIUM_MONTHLY = "premium_monthly"
    const val PREMIUM_YEARLY = "premium_yearly"
    val ALL = listOf(PREMIUM_MONTHLY, PREMIUM_YEARLY)
}

/**
 * Monetization scaffolding (2026-08-12, real founder request - "implementation to be paid").
 * Wraps Play Billing's callback-based API in suspend functions, matching the coroutine style
 * every other repository in this app already uses.
 *
 * IMPORTANT - this cannot be tested end to end yet: Play Billing only works when the app is
 * installed via Google Play (even the internal testing track is enough) - it does not function
 * at all on an app installed from a sideloaded APK, which is everything distributed so far via
 * distribution/app-latest.apk. connect() will succeed and queryProducts() will simply return an
 * empty list until (a) a Play Console developer account exists, (b) this app has at least a
 * draft listing there, and (c) the products in BillingProducts are actually created under
 * Monetize > Products > Subscriptions with real pricing. None of that is code - it's account
 * setup only the founder can do (identity verification + a one-time Play Console fee).
 *
 * Entitlement (`isPremium`) is cached locally in Prefs for fast, offline reads (e.g. gating a
 * screen before a network round-trip could ever complete), but the source of truth is always
 * Play itself - restorePurchases() re-syncs from Play on every app start (see App.onCreate) so a
 * lapsed/refunded/cancelled subscription doesn't leave someone locally "premium" forever. A
 * locally-cached flag with no re-verification would be trivially wrong the moment a subscription
 * expires.
 */
class BillingRepository(context: Context) {
    private val appContext = context.applicationContext

    private val _isPremium = MutableStateFlow(Prefs.isPremium(appContext))
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val purchasesUpdatedListener = PurchasesUpdatedListener { result, purchases ->
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            applicationScopeHandlePurchases(purchases)
        } else if (result.responseCode != BillingClient.BillingResponseCode.USER_CANCELED) {
            AppLogger.w("BillingRepository", "purchase flow failed: ${result.debugMessage}")
        }
    }

    private val billingClient = BillingClient.newBuilder(appContext)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    /** Fire-and-forget from the PurchasesUpdatedListener callback, which isn't itself a suspend
     * context - launched on App's own supervisor scope (same pattern App.onCreate uses for its
     * startup work) so one failed acknowledgement can't take down anything else. */
    private fun applicationScopeHandlePurchases(purchases: List<Purchase>) {
        (appContext as App).applicationScope.launch {
            purchases.forEach { handlePurchase(it) }
        }
    }

    suspend fun connect(): Boolean = suspendCoroutine { cont ->
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                cont.resume(result.responseCode == BillingClient.BillingResponseCode.OK)
            }

            override fun onBillingServiceDisconnected() {
                // Deliberately not retried automatically here - the next screen/action that
                // needs billing calls connect() again, same lazy-reconnect pattern as this
                // app's other on-demand connections (e.g. FamilyAuthRepository).
            }
        })
    }

    /** Re-syncs local entitlement from Play - the actual source of truth. Called on every app
     * start (see App.onCreate) and after a purchase completes, never trusted as a one-time check. */
    suspend fun restorePurchases() {
        val result = billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        )
        val activeSub = result.purchasesList.any { it.products.any { p -> p in BillingProducts.ALL } && it.isAcknowledged }
        setPremium(activeSub)
        result.purchasesList.forEach { handlePurchase(it) }
    }

    suspend fun queryProducts(): List<ProductDetails> {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                BillingProducts.ALL.map {
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(it)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                }
            )
            .build()
        return try {
            billingClient.queryProductDetails(params).productDetailsList ?: emptyList()
        } catch (e: Exception) {
            AppLogger.e("BillingRepository", "queryProducts failed", e)
            emptyList()
        }
    }

    /** `offerToken` comes from the specific ProductDetails.SubscriptionOfferDetails the paywall
     * screen showed the user - see PaywallScreen for how it's picked. */
    fun launchPurchaseFlow(activity: Activity, productDetails: ProductDetails, offerToken: String) {
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .setOfferToken(offerToken)
                        .build()
                )
            )
            .build()
        billingClient.launchBillingFlow(activity, params)
    }

    private suspend fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return
        if (purchase.products.none { it in BillingProducts.ALL }) return

        setPremium(true)

        // Play auto-refunds an unacknowledged purchase within 3 days - this has to happen for
        // every real purchase, not just the first time it's seen, since a purchase can arrive
        // here again (e.g. via restorePurchases after a reinstall) before ever being acknowledged.
        if (!purchase.isAcknowledged) {
            try {
                val params = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
                billingClient.acknowledgePurchase(params)
            } catch (e: Exception) {
                AppLogger.e("BillingRepository", "acknowledgePurchase failed", e)
            }
        }
    }

    private fun setPremium(value: Boolean) {
        Prefs.setPremium(appContext, value)
        _isPremium.value = value
    }

    fun disconnect() {
        billingClient.endConnection()
    }
}
