package com.LambdaProject.MathArt.utils

fun extractVideoId(url: String): String {
    val regex = "^(?:https?:\\/\\/)?(?:www\\.|m\\.)?(?:youtube\\.com\\/watch\\?v=|youtu\\.be\\/|youtube\\.com\\/embed\\/|youtube\\.com\\/shorts\\/)([^#&?]*).*".toRegex()
    val matchResult = regex.find(url)
    return matchResult?.groupValues?.get(1) ?: url
}