package com.LambdaProject.MathArt.ui.Pages.Material

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer

object YouTubePlayerManager {
    private val players = mutableListOf<YouTubePlayer>()

    fun register(player: YouTubePlayer) {
        players.add(player)
    }

    fun unregister(player: YouTubePlayer) {
        players.remove(player)
    }

    fun pauseAllExcept(current: YouTubePlayer) {
        players.forEach {
            if (it != current) it.pause()
        }
    }

    fun pauseAll() {
        players.forEach { it.pause() }
    }
}
