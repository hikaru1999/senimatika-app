package com.LambdaProject.MathArt.data.model

import android.content.Context
import android.media.MediaPlayer
import com.LambdaProject.MathArt.R

object ScoreSoundManager {
    private var mediaPlayer: MediaPlayer? = null

    fun playSound(context: Context, type: ScoreType) {
        val soundResId = when (type) {
            ScoreType.GOOD -> R.raw.correct_1
            ScoreType.COOL -> R.raw.correct_2
            ScoreType.AWESOME -> R.raw.correct_3
            ScoreType.UPS -> R.raw.fail_1
            ScoreType.NOT_FOCUSED, ScoreType.TIME_OUT -> R.raw.fail_2
        }

        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(context, soundResId)
        mediaPlayer?.start()
    }
}