package com.LambdaProject.MathArt.utils

import android.content.Context
import com.LambdaProject.MathArt.R

fun String.toDrawableResId(context: Context): Int {
    if (this.isEmpty()) return 0

    return context.resources.getIdentifier(
        this,
        "drawable",
        context.packageName
    )
}

fun getLandmarkResourceId(variant: String?): Int {
    return when (variant) {
        "obj_landmark_tugu" -> R.drawable.obj_landmark_tugu
        "obj_landmark_aceh" -> R.drawable.obj_landmark_aceh
        "obj_landmark_sumbar" -> R.drawable.obj_landmark_sumbar
        "obj_landmark_bandung" -> R.drawable.obj_landmark_bandung
        "obj_landmark_jatim" -> R.drawable.obj_landmark_jatim
        else -> R.drawable.obj_landmark_tugu
    }
}