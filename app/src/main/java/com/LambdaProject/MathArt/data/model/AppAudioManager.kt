package com.LambdaProject.MathArt.data.model

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.util.Log
import com.LambdaProject.MathArt.R
import kotlinx.coroutines.*

object AppAudioManager {
    private var soundPool: SoundPool? = null
    private val sounds = mutableMapOf<String, Int>()
    private var bgmPlayer: MediaPlayer? = null
    private var intensePlayer: MediaPlayer? = null
    private var currentVolume = 0.6f
    private var fadeJob: Job? = null
    private var lastAmbientResId: Int? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun initialize(context: Context) {
        if (soundPool != null) return

        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(10)
            .setAudioAttributes(attrs)
            .build()

        load(context, "coin", R.raw.sfx_coin)
        load(context, "powerup", R.raw.sfx_powerup)
        load(context, "scroll", R.raw.sfx_scroll)
        load(context, "move", R.raw.sfx_move)
        load(context, "block", R.raw.sfx_block)
        load(context, "attack", R.raw.sfx_attack)
        load(context, "unlocked", R.raw.sfx_unlocked)
        load(context, "drink", R.raw.sfx_heal)
        load(context, "sfx_freeze", R.raw.sfx_powerup_used_ice)
        load(context, "sfx_shield", R.raw.sfx_powerup_used_shield)
        load(context, "sfx_magic", R.raw.sfx_powerup_used_magic)
        load(context, "sfx_healing", R.raw.sfx_powerup_used_health)
        load(context, "zoom_out", R.raw.sfx_zoom_used)
        load(context, "wear", R.raw.sfx_wear)
        load(context, "light_on", R.raw.sfx_light_on)

        load(context, "score_good", R.raw.correct_1)
        load(context, "score_cool", R.raw.correct_2)
        load(context, "score_awesome", R.raw.correct_3)
        load(context, "score_ups", R.raw.fail_1)
        load(context, "score_fail", R.raw.fail_2)

        load(context, "quizVictory", R.raw.sfx_quiz_victory)
        load(context, "quizFailed", R.raw.sfx_battle_failed)

        load(context, "victory", R.raw.sfx_extraction_success)
        load(context, "gameover", R.raw.sfx_fail)
    }

    private fun load(context: Context, key: String, resId: Int) {
        soundPool?.let {
            sounds[key] = it.load(context, resId, 1)
        }
    }

    fun playSfx(key: String) {
        sounds[key]?.let { id ->
            soundPool?.play(id, 1f, 1f, 0, 0, 1f)
        }
    }

    fun playScoreSound(type: ScoreType) {
        val key = when (type) {
            ScoreType.GOOD -> "score_good"
            ScoreType.COOL -> "score_cool"
            ScoreType.AWESOME -> "score_awesome"
            ScoreType.UPS -> "score_ups"
            ScoreType.NOT_FOCUSED, ScoreType.TIME_OUT -> "score_fail"
        }
        playSfx(key)
    }

    fun playBGM(context: Context, resId: Int, isAmbient: Boolean = false) {
        if (bgmPlayer?.isPlaying == true && lastAmbientResId == resId) return

        if (isAmbient) lastAmbientResId = resId

        fadeJob?.cancel()
        stopBGM()

        try {
            bgmPlayer = MediaPlayer.create(context, resId).apply {
                isLooping = true
                val vol = if (isAmbient) 0.25f else 0.6f
                setVolume(vol, vol)
                start()
            }
        } catch (e: Exception) {
            Log.e("AppAudioManager", "Gagal memulai BGM: ${e.message}")
        }
    }

    fun playIntenseWarning(context: Context, resId: Int) {
        if (intensePlayer?.isPlaying == true) return
        intensePlayer?.release()
        intensePlayer = MediaPlayer.create(context, resId).apply {
            isLooping = true
            setVolume(0.3f, 0.3f)
            start()
        }
    }

    fun stopIntenseWarning() {
        intensePlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
        }
        intensePlayer = null
    }

    fun stopBGMWithFade(duration: Long = 2000) {
        val player = bgmPlayer ?: return
        if (!player.isPlaying) return

        fadeJob = scope.launch {
            val startVol = currentVolume
            val steps = 20
            val interval = duration / steps
            for (i in 1..steps) {
                val vol = startVol * (steps - i) / steps
                player.setVolume(vol, vol)
                delay(interval)
            }
            stopBGM()
        }
    }

    fun stopBGM() {
        fadeJob?.cancel()
        bgmPlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
        }
        bgmPlayer = null
    }

    fun forceStopAll() {
        stopBGM()
        stopIntenseWarning()
        fadeJob?.cancel()
    }

    fun resumeAmbient(context: Context) {
        lastAmbientResId?.let {
            playBGM(context, it, isAmbient = true)
        }
    }
}