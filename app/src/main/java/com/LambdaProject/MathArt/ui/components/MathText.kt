package com.LambdaProject.MathArt.ui.components

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
@Composable
fun MathText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: Int = 14,
    color: Color = Color.Black,
    textAlign: String = "center",
    fontWeight: FontWeight = FontWeight.Normal,
) {
    val density = LocalDensity.current
    var webViewHeight by remember { mutableStateOf(100.dp) }

    /* val escapedText = text.replace("'", "\\'").replace("\n", "<br>") */
    val escapedText = text
        .replace("&", "&amp;")    .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("'", "&#39;")
        .replace("\"", "&quot;")
        .replace("\n", "<br>")

    val textColor = String.format("#%06X", 0xFFFFFF and color.toArgb())
    val cssFontWeight = fontWeight.weight

    val baseUrl = "file:///android_asset/"
    val katexJs = "katex/katex.min.js"
    val katexCss = "katex/katex.min.css"
    val autoRenderJs = "katex/contrib/auto-render.min.js"

    val htmlData = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
            <link rel="stylesheet" href="$katexCss">
            <script defer src="$katexJs"></script>
            <script defer src="$autoRenderJs" onload="renderMath()"></script>
            <style>
                body {
                    background-color: transparent;
                    color: $textColor;
                    font-size: ${fontSize}px;
                    font-weight: $cssFontWeight;
                    margin: 0;
                    padding: 0;
                    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
                    overflow: visible;
                    text-align: $textAlign;
                    line-height: 1.6;
                }
                #math-wrapper {
                    font-weight: $cssFontWeight;
                    padding: 15px 10px;
                    display: block;
                    word-wrap: break-word;
                    width: 100%;
                    box-sizing: border-box;
                    min-height: 20px;
                }
                .katex-display {
                    margin: 0.8em 0 !important;
                }
                .katex {
                    font-weight: $cssFontWeight !important;
                    ${if (cssFontWeight >= 700) "text-shadow: 0.2px 0 0 $textColor, -0.1px 0 0 $textColor;" else ""}
                }.katex {
                    white-space: normal !important;
                }
            </style>
            <script>
                function updateHeight() {
                    var wrapper = document.getElementById('math-wrapper');
                    var height = Math.max(wrapper.scrollHeight, wrapper.offsetHeight, wrapper.getBoundingClientRect().height);
                    Android.updateHeight(height);
                }
                function observeChanges() {
                    const wrapper = document.getElementById('math-wrapper');
                    const observer = new MutationObserver(function() {
                        updateHeight();
                    });

                    observer.observe(wrapper, {
                        childList: true,
                        subtree: true,
                        characterData: true
                    });
                }
                function renderMath() {
                    if (typeof renderMathInElement === 'function') {
                        renderMathInElement(document.getElementById('math-wrapper'), {
                            delimiters: [
                                {left: '$$', right: '$$', display: true},
                                {left: '$', right: '$', display: false}
                            ],
                            throwOnError : false
                        });
                    }
                    observeChanges();
                    setTimeout(updateHeight, 100);
                    setTimeout(updateHeight, 500);
                    setTimeout(updateHeight, 1500);
                }
                window.onload = updateHeight;
            </script>
        </head>
        <body>
            <div id="math-wrapper">$escapedText</div>
        </body>
        </html>
    """.trimIndent()

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = webViewHeight),
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.allowFileAccess = true
                settings.domStorageEnabled = true
                setBackgroundColor(0)

                addJavascriptInterface(object : Any() {
                    @JavascriptInterface
                    fun updateHeight(height: Float) {
                        val heightInDp = (height / context.resources.displayMetrics.density).dp
                        webViewHeight = heightInDp + 8.dp
                    }
                }, "Android")
                webViewClient = WebViewClient()
                
                // Disable interaction to allow parent click events
                isClickable = false
                isFocusable = false
                isEnabled = false
                setOnTouchListener { _, _ -> true }
            }
        },
        update = { webView ->
            if (webView.tag != text) {
                webView.loadDataWithBaseURL(baseUrl, htmlData, "text/html", "utf-8", null)
                webView.tag = text
            }
        }
    )
}
