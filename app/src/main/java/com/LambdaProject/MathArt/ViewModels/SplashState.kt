package com.LambdaProject.MathArt.ViewModels

sealed class SplashState {
    object Loading : SplashState()
    object Intro : SplashState()
    data class Login(val message: String? = null) : SplashState()
    data class Dashboard(val username: String) : SplashState()
    object NoInternet : SplashState()
}