package com.lifeos.expensecapture.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.core.content.getSystemService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Offline Mode PRD, Phase 3 Doc 46: "the offline-status indication to the user." Every action
 * in this app is already fully available offline by construction (100% local Room DB, SMS
 * parsing is on-device) - so the capability matrix this PRD calls for is trivial (everything =
 * fully available), and reconciliation-on-reconnect is structurally not applicable (there's no
 * backend to reconcile against). What's left, and what this provides, is the one genuinely
 * required piece: telling the user they're offline at all, since "nothing breaks" isn't the
 * same as "the user knows why nothing needed the network." See day-2.md.
 */
object ConnectivityObserver {

    fun isOnlineFlow(context: Context): Flow<Boolean> = callbackFlow {
        val connectivityManager = context.getSystemService<ConnectivityManager>()
        if (connectivityManager == null) {
            trySend(true)
            awaitClose { }
            return@callbackFlow
        }

        fun currentlyOnline(): Boolean {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }

        trySend(currentlyOnline())

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { trySend(currentlyOnline()) }
            override fun onLost(network: Network) { trySend(currentlyOnline()) }
            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                trySend(currentlyOnline())
            }
        }
        connectivityManager.registerDefaultNetworkCallback(callback)
        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()
}
