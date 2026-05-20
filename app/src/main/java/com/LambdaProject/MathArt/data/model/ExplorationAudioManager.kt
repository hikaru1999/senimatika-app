package com.LambdaProject.MathArt.data.model

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import androidx.activity.result.launch
import com.LambdaProject.MathArt.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class ExplorationAudioManager(private val context: Context) {
    private var soundPool: SoundPool
    private val sounds = mutableMapOf<String, Int>()
    private var bgmPlayer: MediaPlayer? = null
    private var intensePlayer: MediaPlayer? = null
    private var currentAmbientResId: Int? = null
    private var currentVolume = 0.6f
    private var fadeJob: Job? = null

    init {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(attrs)
            .build()

        loadSound("coin", R.raw.sfx_coin)
        loadSound("powerup", R.raw.sfx_powerup)
        loadSound("scroll", R.raw.sfx_scroll)
        loadSound("quizVictory", R.raw.sfx_quiz_victory)
        loadSound("quizFailed", R.raw.sfx_battle_failed)
        loadSound("success", R.raw.sfx_extraction_success)
        loadSound("gameover", R.raw.sfx_fail)
        loadSound("attack", R.raw.sfx_attack)
        loadSound("sfx_freeze", R.raw.sfx_powerup_used_ice)
        loadSound("sfx_shield", R.raw.sfx_powerup_used_shield)
        loadSound("sfx_magic", R.raw.sfx_powerup_used_magic)
        loadSound("sfx_healing", R.raw.sfx_powerup_used_health)
        loadSound("move", R.raw.sfx_move)
        loadSound("block", R.raw.sfx_block)
        loadSound("zoom_out", R.raw.sfx_zoom_used)
        loadSound("unlocked", R.raw.sfx_unlocked)
        loadSound("wear", R.raw.sfx_wear)
        loadSound("drink", R.raw.sfx_heal)
        loadSound("light_on", R.raw.sfx_light_on)
    }

    private fun loadSound(key: String, resId: Int) {
        sounds[key] = soundPool.load(context, resId, 1)
    }

    fun playSfx(key: String) {
        sounds[key]?.let { id ->
            soundPool.play(id, 1f, 1f, 0, 0, 1f)
        }
    }

    fun playBGM(resId: Int, isAmbient: Boolean = false) {
        if (isAmbient) currentAmbientResId = resId
        fadeJob?.cancel()
        stopBGM()

        bgmPlayer = MediaPlayer.create(context, resId).apply {
            isLooping = true
            val vol = if (isAmbient) 0.25f else 0.6f
            currentVolume = vol
            setVolume(vol, vol)
            start()
        }
    }

    fun playIntenseWarning(resId: Int) {
        if (intensePlayer != null) {
            if (intensePlayer?.isPlaying == true) return
        }

        try {
            intensePlayer = MediaPlayer.create(context, resId).apply {
                isLooping = true
                setVolume(0.3f, 0.3f)
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun pauseBGM() {
        bgmPlayer?.let {
            if (it.isPlaying) it.pause()
        }
    }

    fun resumeAmbient() {
        fadeJob?.cancel()
        bgmPlayer?.let {
            if (!it.isPlaying) {
                it.setVolume(0.25f, 0.25f) // Pastikan volume kembali ke mode ambient
                it.start()
            }
        } ?: run {
            currentAmbientResId?.let { playBGM(it, isAmbient = true) }
        }
    }

    fun stopIntenseWarning() {
        intensePlayer?.let {
            if (it.isPlaying) {
                it.stop()
                it.release()
            }
        }
        intensePlayer = null
    }

    fun forceStopAll() {
        fadeJob?.cancel()
        fadeJob = null

        stopBGM()

        stopIntenseWarning()
        currentAmbientResId = null
        currentVolume = 0.6f
    }

    fun stopBGMWithFade(duration: Long = 2000, onComplete: () -> Unit = {}) {
        val player = bgmPlayer ?: run { onComplete(); return }
        if (!player.isPlaying) {
            onComplete()
            return
        }

        val startVolume = currentVolume
        val steps = 20
        val interval = duration / steps
        val deltaVolume = startVolume / steps

        fadeJob?.cancel()
        fadeJob = CoroutineScope(Dispatchers.Main).launch {
            var vol = startVolume
            try {
                for (i in 1..steps) {
                    vol -= deltaVolume
                    val targetVol = vol.coerceAtLeast(0f)
                    player.setVolume(targetVol, targetVol)
                    delay(interval)
                }
                stopBGM()
                onComplete()
            } catch (e: CancellationException) {
                // Ignore
            } finally {
                fadeJob = null
            }
        }
    }

    fun stopBGM() {
        fadeJob?.cancel()
        bgmPlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
        }
        bgmPlayer = null
        stopIntenseWarning()
    }
}