package com.LambdaProject.MathArt.ui.Pages.Material

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
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

    AndroidView(
        modifier = modifier.clip(RoundedCornerShape(12.dp)),
        factory = { context ->
            YouTubePlayerView(context).apply {
                // MATIKAN inisialisasi otomatis untuk kustomisasi manual
                enableAutomaticInitialization = false
                lifecycleOwner.lifecycle.addObserver(this)

                // AKSES WEBVIEW INTERNAL
                // YouTubePlayerView membungkus WebView di index 0
                val internalWebView = this.getChildAt(0) as? WebView
                internalWebView?.settings?.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    // Trick 1: Samarkan User Agent sebagai Chrome Desktop/Mobile yang legal
                    userAgentString = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Mobile Safari/537.36"
                }

                // Trick 2: Set IFrame Options dengan Origin HTTPS (Bukan android-app://)
                val options = IFramePlayerOptions.Builder()
                    .controls(1)
                    // Origin harus diawali https:// agar YouTube memberikan izin video unlisted/public
                    .origin("https://$packageName")
                    .build()

                // Trick 3: Inisialisasi dengan opsi yang sudah dibuat
                initialize(object : AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        // Gunakan cue agar tidak langsung putar otomatis (hemat kuota user)
                        youTubePlayer.cueVideo(videoId, 0f)
                    }

                    override fun onError(youTubePlayer: YouTubePlayer, error: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerError) {
                        // Log jika masih ada error untuk debugging
                        android.util.Log.e("YouTubePlayer", "Error Code: $error")
                    }
                }, options)
            }
        },
        onRelease = {
            it.release()
        }
    )
}