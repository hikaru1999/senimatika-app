package com.LambdaProject.MathArt.utils

import androidx.annotation.DrawableRes
import com.LambdaProject.MathArt.R

object AvatarHelper {
    const val DEFAULT_AVATAR = "avatar_5"

    val AVATAR_LIST: List<String> = (1..32).map { "avatar_$it" }

    @DrawableRes
    fun getAvatarDrawableRes(avatarName: String?): Int {
        return when (avatarName) {
            "avatar_1" -> R.drawable.avatar_1
            "avatar_2" -> R.drawable.avatar_2
            "avatar_3" -> R.drawable.avatar_3
            "avatar_4" -> R.drawable.avatar_4
            "avatar_5" -> R.drawable.avatar_5
            "avatar_6" -> R.drawable.avatar_6
            "avatar_7" -> R.drawable.avatar_7
            "avatar_8" -> R.drawable.avatar_8
            "avatar_9" -> R.drawable.avatar_9
            "avatar_10" -> R.drawable.avatar_10
            "avatar_11" -> R.drawable.avatar_11
            "avatar_12" -> R.drawable.avatar_12
            "avatar_13" -> R.drawable.avatar_13
            "avatar_14" -> R.drawable.avatar_14
            "avatar_15" -> R.drawable.avatar_15
            "avatar_16" -> R.drawable.avatar_16
            "avatar_17" -> R.drawable.avatar_17
            "avatar_18" -> R.drawable.avatar_18
            "avatar_19" -> R.drawable.avatar_19
            "avatar_20" -> R.drawable.avatar_20
            "avatar_21" -> R.drawable.avatar_21
            "avatar_22" -> R.drawable.avatar_22
            "avatar_23" -> R.drawable.avatar_23
            "avatar_24" -> R.drawable.avatar_24
            "avatar_25" -> R.drawable.avatar_25
            "avatar_26" -> R.drawable.avatar_26
            "avatar_27" -> R.drawable.avatar_27
            "avatar_28" -> R.drawable.avatar_28
            "avatar_29" -> R.drawable.avatar_29
            "avatar_30" -> R.drawable.avatar_30
            "avatar_31" -> R.drawable.avatar_31
            "avatar_32" -> R.drawable.avatar_32
            else -> R.drawable.avatar_5
        }
    }
}
