package com.LambdaProject.MathArt.ui.Pages.Material

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import java.util.Collections

object YouTubePlayerManager {
    private val players = Collections.synchronizedList(mutableListOf<YouTubePlayer>())

    fun register(player: YouTubePlayer) {
        synchronized(players) {
            if (!players.contains(player)) {
                players.add(player)
            }
        }
    }

    fun unregister(player: YouTubePlayer) {
        synchronized(players) {
            players.remove(player)
        }
    }

    fun pauseAllExcept(current: YouTubePlayer) {
        val copy = synchronized(players) { players.toList() }
        copy.forEach {
            if (it != current) {
                try {
                    it.pause()
                } catch (_: Exception) {}
            }
        }
    }

    fun pauseAll() {
        val copy = synchronized(players) { players.toList() }
        copy.forEach {
            try {
                it.pause()
            } catch (_: Exception) {}
        }
    }

    fun clear() {
        pauseAll()
        synchronized(players) {
            players.clear()
        }
    }
}

