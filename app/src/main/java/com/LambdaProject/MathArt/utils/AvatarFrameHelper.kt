package com.LambdaProject.MathArt.utils

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.LambdaProject.MathArt.R

/**
 * Konfigurasi kalibrasi posisi dan skala bingkai avatar.
 * Setiap bingkai memiliki ornamen mahkota, sayap, atau batu permata yang berbeda
 * sehingga titik tengah (inner circle) disesuaikan presisi melalui offsetXPercent dan offsetYPercent.
 */
data class AvatarFrameConfig(
    val id: String,
    val name: String,
    val description: String,
    @DrawableRes val drawableRes: Int,
    val requiredLevel: Int,
    val scaleMultiplier: Float = 1.35f,
    val offsetXPercent: Float = 0f, // Persentase offset horizontal terhadap avatarSize
    val offsetYPercent: Float = 0f  // Persentase offset vertikal terhadap avatarSize
) {
    fun getOffsetX(avatarSize: Dp): Dp = avatarSize * offsetXPercent
    fun getOffsetY(avatarSize: Dp): Dp = avatarSize * offsetYPercent
}

object AvatarFrameHelper {
    val FRAME_LIST = listOf(
        AvatarFrameConfig(
            id = "frame_1",
            name = "Bronze Rune",
            description = "Bingkai Perunggu Klasik bertatahkan Permata Safir",
            drawableRes = R.drawable.frame_1,
            requiredLevel = 3,
            scaleMultiplier = 1.45f,
            offsetXPercent = 0.020f,
            offsetYPercent = 0.002f // Disesuaikan sedikit ke bawah agar lubang pas di tengah
        ),
        AvatarFrameConfig(
            id = "frame_2",
            name = "Celestial Ring",
            description = "Lingkaran Kosmis Astral dengan Aura Cahaya Cyan",
            drawableRes = R.drawable.frame_2,
            requiredLevel = 6,
            scaleMultiplier = 1.5f,
            offsetXPercent = 0.010f,
            offsetYPercent = -0.07f // Disesuaikan ke atas agar ornamen pas mengitari avatar
        ),
        AvatarFrameConfig(
            id = "frame_3",
            name = "Sun Crown",
            description = "Mahkota Surya Emas bercahaya dengan Sayap Suci",
            drawableRes = R.drawable.frame_3,
            requiredLevel = 10,
            scaleMultiplier = 1.5f,
            offsetXPercent = 0.005f,
            offsetYPercent = -0.1f // Geser ke atas agar mahkota berada di puncak kepala & lubang melingkari avatar
        ),
        AvatarFrameConfig(
            id = "frame_4",
            name = "Mystic Blossom",
            description = "Sayap Mahkota Kristal Mistik Bertabur Kelopak Bunga",
            drawableRes = R.drawable.frame_4,
            requiredLevel = 15,
            scaleMultiplier = 1.6f,
            offsetXPercent = -0.01f,
            offsetYPercent = -0.01f // Geser ke atas agar sayap kristal bertumpu di bawah avatar dengan sempurna
        )
    )

    fun getFrameConfig(frameId: String?): AvatarFrameConfig? {
        if (frameId.isNullOrBlank() || frameId == "none") return null
        return FRAME_LIST.find { it.id == frameId }
    }
}

/**
 * Komponen Avatar Pengguna dengan Bingkai Terkalibrasi (menggunakan DrawableRes)
 */
@Composable
fun UserAvatarWithFrame(
    @DrawableRes avatarRes: Int,
    frameId: String?,
    avatarSize: Dp = 56.dp,
    modifier: Modifier = Modifier
) {
    val frameConfig = AvatarFrameHelper.getFrameConfig(frameId)

    Box(
        modifier = modifier.size(avatarSize),
        contentAlignment = Alignment.Center
    ) {
        // 1. Avatar Foto Pengguna (Lingkaran Tengah)
        Image(
            painter = painterResource(id = avatarRes),
            contentDescription = "Avatar",
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        // 2. Bingkai Terkalibrasi di atas Avatar
        if (frameConfig != null) {
            Image(
                painter = painterResource(id = frameConfig.drawableRes),
                contentDescription = frameConfig.name,
                modifier = Modifier
                    .requiredSize(avatarSize * frameConfig.scaleMultiplier)
                    .offset(
                        x = frameConfig.getOffsetX(avatarSize),
                        y = frameConfig.getOffsetY(avatarSize)
                    )
            )
        }
    }
}

/**
 * Komponen Avatar Pengguna dengan Bingkai Terkalibrasi (menggunakan avatarName)
 */
@Composable
fun UserAvatarWithFrame(
    avatarName: String?,
    frameId: String?,
    avatarSize: Dp = 56.dp,
    modifier: Modifier = Modifier
) {
    val avatarRes = AvatarHelper.getAvatarDrawableRes(avatarName)
    UserAvatarWithFrame(
        avatarRes = avatarRes,
        frameId = frameId,
        avatarSize = avatarSize,
        modifier = modifier
    )
}
