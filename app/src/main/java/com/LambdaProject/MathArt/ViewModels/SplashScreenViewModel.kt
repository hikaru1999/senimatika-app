package com.LambdaProject.MathArt.ViewModels

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.LambdaProject.MathArt.UserPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await

class SplashViewModel(context: Context) : ViewModel() {

    private val _splashState = MutableStateFlow<SplashState>(SplashState.Loading)
    val splashState: StateFlow<SplashState> = _splashState.asStateFlow()

    private val userPreferences = UserPreferences(context)
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var hasNavigated = false

    init {
        checkNavigation()
        registerNetworkCallback()
    }

    private fun registerNetworkCallback() {
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                checkNavigation()
                Log.d("NetworkCheck", "Internet Available: ${isInternetAvailable()}")
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                _splashState.value = SplashState.NoInternet
            }
        }

        connectivityManager.registerNetworkCallback(
            android.net.NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build(),
            networkCallback!!
        )
    }

    private fun isInternetAvailable(): Boolean {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun checkNavigation() {
        viewModelScope.launch {

            if (!isInternetAvailable()) {
                FirebaseAuth.getInstance().signOut()
                _splashState.value = SplashState.NoInternet
                return@launch
            }

            delay(500)

            val isIntroShown = userPreferences.isIntroShown.first()
            val user = FirebaseAuth.getInstance().currentUser

            delay(5000)

            if (!isIntroShown) {
                _splashState.value = SplashState.Intro
            } else if (user != null) {
                val uid = user.uid
                try {
                    val document = FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(uid)
                        .get()
                        .await()

                    val username = document.getString("username") ?: "User"
                    _splashState.value = SplashState.Dashboard(username)
                    hasNavigated = true
                } catch (e: Exception) {
                    _splashState.value = SplashState.Login("Gagal mengambil data")
                    hasNavigated = true
                }
            } else {
                _splashState.value = SplashState.Login()
                hasNavigated = true
            }
        }
    }
}
