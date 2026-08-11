package com.lifeos.expensecapture.ui.paywall

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.android.billingclient.api.ProductDetails
import com.lifeos.expensecapture.App
import com.lifeos.expensecapture.billing.BillingProducts
import com.lifeos.expensecapture.ui.common.cardSurfaceColor
import com.lifeos.expensecapture.util.Prefs
import kotlinx.coroutines.launch

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/**
 * Monetization scaffolding (2026-08-12) - see BillingRepository's own kdoc first. This screen is
 * fully wired up and will work exactly as written, but every card below will show "not available
 * yet" until (a) a Play Console developer account and app listing exist, and (b) the two products
 * in BillingProducts are actually created there with real pricing. That's account setup, not
 * something this screen or any other code can complete on its own.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallScreen(app: App, onBack: () -> Unit) {
    val context = LocalContext.current
    val activity = context.findActivity()
    val coroutineScope = rememberCoroutineScope()
    val isPremium by app.billingRepository.isPremium.collectAsState()

    var products by remember { mutableStateOf<List<ProductDetails>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        products = app.billingRepository.queryProducts()
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Premium") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text(
                            if (isPremium) "You're on Premium" else "What Premium unlocks",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Unlimited AI financial questions (free plan: ${Prefs.FREE_AI_QUESTIONS_PER_MONTH}/month) " +
                                "and full Family Sharing / Smart Split, with no limits on members or splits.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (isPremium) {
                item {
                    Text(
                        "Thanks for supporting the app - manage or cancel your subscription any time from " +
                            "the Play Store's Subscriptions page.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else if (isLoading) {
                item {
                    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                        CircularProgressIndicator()
                    }
                }
            } else if (products.isEmpty()) {
                item {
                    Text(
                        "Premium isn't available yet on this build - check back soon.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(products, key = { it.productId }) { product ->
                    ProductCard(product = product) { offerToken ->
                        if (activity != null) {
                            app.billingRepository.launchPurchaseFlow(activity, product, offerToken)
                        }
                    }
                }
            }

            statusMessage?.let { message ->
                item { Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            }

            if (!isPremium) {
                item {
                    TextButton(onClick = {
                        coroutineScope.launch {
                            app.billingRepository.restorePurchases()
                            statusMessage = if (app.billingRepository.isPremium.value) {
                                "Restored - you're on Premium."
                            } else {
                                "No active purchase found to restore."
                            }
                        }
                    }) { Text("Restore purchases") }
                }
            }
        }
    }
}

@Composable
private fun ProductCard(product: ProductDetails, onSubscribe: (offerToken: String) -> Unit) {
    val offer = product.subscriptionOfferDetails?.firstOrNull() ?: return
    val pricingPhase = offer.pricingPhases.pricingPhaseList.firstOrNull()
    val label = if (product.productId == BillingProducts.PREMIUM_YEARLY) "Yearly" else "Monthly"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.titleMedium)
            Text(
                pricingPhase?.formattedPrice ?: "",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            Button(onClick = { onSubscribe(offer.offerToken) }, modifier = Modifier.fillMaxWidth()) {
                Text("Subscribe")
            }
        }
    }
}
