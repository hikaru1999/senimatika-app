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
            .setMaxStreams(5) // Max 5 suara bersamaan
            .setAudioAttributes(attrs)
            .build()

        // Load semua SFX ke memory (Pre-load)
        /* loadSound("click", R.raw.sfx_button_click)
        loadSound("walk", R.raw.sfx_walk) */
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
    }

    private fun loadSound(key: String, resId: Int) {
        sounds[key] = soundPool.load(context, resId, 1)
    }

    fun playSfx(key: String) {
        sounds[key]?.let { id ->
            soundPool.play(id, 1f, 1f, 0, 0, 1f)
        }
    }

    // Untuk Musik Latar (Looping)
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

    /* fun playIntenseWarning(resId: Int) {
        if (intensePlayer?.isPlaying == true) return // Jangan putar jika sudah jalan

        intensePlayer = MediaPlayer.create(context, resId).apply {
            isLooping = true
            setVolume(0f, 0f) // Mulai dari sunyi
            start()
        }

        // Fade in ke volume 0.25f (lebih kecil dari BGM 0.6f)
        CoroutineScope(Dispatchers.Main).launch {
            var vol = 0f
            while (vol < 0.25f) {
                vol += 0.02f
                intensePlayer?.setVolume(vol, vol)
                delay(100)
            }
        }
    } */

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

    /* fun stopBGMWithFade(duration: Long = 2000, onComplete: () -> Unit = {}) {
        bgmPlayer?.let { player ->
            if (!player.isPlaying) return

            val maxVolume = 0.6f
            val steps = 20
            val interval = duration / steps
            val deltaVolume = maxVolume / steps

            CoroutineScope(Dispatchers.Main).launch {
                var currentVol = maxVolume
                for (i in 0 until steps) {
                    currentVol -= deltaVolume
                    if (currentVol < 0) currentVol = 0f
                    player.setVolume(currentVol, currentVol)
                    delay(interval)
                }
                stopBGM() // Benar-benar release setelah silent
            }
        }
    } */

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

        // Simpan coroutine ke dalam fadeJob
        fadeJob?.cancel() // Batalkan jika ada fade lain
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
                // Jika dicancel (karena resumeAmbient dipanggil),
                // jangan panggil stopBGM()!
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