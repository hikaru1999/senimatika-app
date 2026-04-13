package com.LambdaProject.MathArt.utils

import android.content.Context

fun String.toDrawableResId(context: Context): Int {
    if (this.isEmpty()) return 0

    return context.resources.getIdentifier(
        this,
        "drawable",
        context.packageName
    )
}