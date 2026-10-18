package com.LambdaProject.MathArt.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class NetworkObserver(context: Context) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val isConnected: Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
//                trySend(true)
                launch { send(true) }
            }

            override fun onLost(network: Network) {
//                trySend(false)
                launch { send(false) }
            }
        }
        connectivityManager.registerNetworkCallback(
            NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build(),
            callback
        )

        val initial = connectivityManager.activeNetwork?.let {
            connectivityManager.getNetworkCapabilities(it)
        }?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
        trySend(initial)

//        connectivityManager.registerDefaultNetworkCallback(callback)
//        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()
}