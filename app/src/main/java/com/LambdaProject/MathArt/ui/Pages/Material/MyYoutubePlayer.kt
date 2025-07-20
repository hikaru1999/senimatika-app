package com.LambdaProject.MathArt.ui.Pages.Material

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

@Composable
fun MyYouTubePlayer(
    videoId: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        modifier = modifier,
        factory = {
            val view = YouTubePlayerView(context).apply {
                lifecycleOwner.lifecycle.addObserver(this)
            }

            view.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                lateinit var youTubePlayer: YouTubePlayer

                override fun onReady(player: YouTubePlayer) {
                    youTubePlayer = player
                    player.cueVideo(videoId, 0f)
                    YouTubePlayerManager.register(player)

                    player.addListener(object : AbstractYouTubePlayerListener() {
                        override fun onStateChange(
                            youTubePlayer: YouTubePlayer,
                            state: PlayerConstants.PlayerState
                        ) {
                            if (state == PlayerConstants.PlayerState.PLAYING) {
                                YouTubePlayerManager.pauseAllExcept(youTubePlayer)
                            }
                        }
                    })
                }
            })

            view
        },
        update = {}
    )
}
