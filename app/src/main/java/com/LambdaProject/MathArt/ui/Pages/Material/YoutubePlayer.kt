package com.LambdaProject.MathArt.ui.Pages.Material

import android.annotation.SuppressLint
import android.webkit.WebView
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.LambdaProject.MathArt.utils.extractVideoId
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YouTubePlayer(
    youtubeUrl: String,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val videoId = remember(youtubeUrl) { extractVideoId(youtubeUrl) }
    val packageName = "com.LambdaProject.MathArt"

    var playerRef by remember { mutableStateOf<YouTubePlayer?>(null) }
    var playerViewRef by remember { mutableStateOf<YouTubePlayerView?>(null) }
    var lastCuedVideoId by remember { mutableStateOf<String?>(null) }

    DisposableEffect(lifecycleOwner, videoId) {
        onDispose {
            playerRef?.let { player ->
                try {
                    player.pause()
                    YouTubePlayerManager.unregister(player)
                } catch (_: Exception) {}
            }
            playerViewRef?.let { view ->
                try {
                    lifecycleOwner.lifecycle.removeObserver(view)
                } catch (_: Exception) {}
            }
        }
    }

    AndroidView(
        modifier = modifier.clip(RoundedCornerShape(12.dp)),
        factory = { context ->
            YouTubePlayerView(context).apply {
                playerViewRef = this
                enableAutomaticInitialization = false
                lifecycleOwner.lifecycle.addObserver(this)
                val internalWebView = this.getChildAt(0) as? WebView
                internalWebView?.settings?.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    userAgentString = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Mobile Safari/537.36"
                }

                val options = IFramePlayerOptions.Builder()
                    .controls(1)
                    .origin("https://$packageName")
                    .build()

                initialize(object : AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        playerRef = youTubePlayer
                        YouTubePlayerManager.register(youTubePlayer)
                        youTubePlayer.cueVideo(videoId, 0f)
                        lastCuedVideoId = videoId
                    }

                    override fun onError(youTubePlayer: YouTubePlayer, error: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerError) {
                        android.util.Log.e("YouTubePlayer", "Error Code: $error")
                    }
                }, options)
            }
        },
        update = {
            if (lastCuedVideoId != videoId) {
                playerRef?.cueVideo(videoId, 0f)
                lastCuedVideoId = videoId
            }
        },
        onRelease = { view ->
            playerRef?.let { player ->
                try {
                    player.pause()
                    YouTubePlayerManager.unregister(player)
                } catch (_: Exception) {}
            }
            try {
                lifecycleOwner.lifecycle.removeObserver(view)
            } catch (_: Exception) {}

            view.post {
                try {
                    view.release()
                } catch (e: Exception) {
                    android.util.Log.e("YouTubePlayer", "Error releasing YouTubePlayerView: ${e.message}")
                }
            }
        }
    )
}