package com.LambdaProject.MathArt.ui.components

import android.view.*
import android.annotation.SuppressLint
import android.webkit.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility", "RememberReturnType")
@Composable
fun MathText(
    text: String,
    onRenderComplete: () -> Unit = {},
    onImageClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    fontSize: Int = 14,
    color: Color = Color.Black,
    textAlign: String = "left",
    fontWeight: FontWeight = FontWeight.Normal,
) {
    val density = LocalDensity.current
    var isReady by remember(text) { mutableStateOf(false) }
//    var webViewHeight by remember { mutableStateOf(100.dp) }
    var webViewHeight by remember(text) { mutableStateOf(1.dp) }

    val textColor = String.format("#%06X", 0xFFFFFF and color.toArgb())
    val cssFontWeight = fontWeight.weight
    val baseUrl = "file:///android_asset/"

    val escapedText = text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("'", "&#39;")
        .replace("\"", "&quot;")

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
                    line-height: 1.5;
                }
                
                div, p, ul, li {
                    margin-top: 5px;
                    margin-bottom: 10px;
                    display: block;
                }
                
                .justify { text-align: justify !important; display: block; width: 100%; }
                .center  { text-align: center !important; display: block; width: 100%; }
                .right   { text-align: right !important; display: block; width: 100%; }
                .left    { text-align: left !important; display: block; width: 100%; }
                
                #math-wrapper {
                    font-weight: $cssFontWeight;
                    padding: 2px 10px;
                    display: block;
                    word-wrap: break-word;
                    width: 100%;
                    box-sizing: border-box;
                    min-height: 20px;
                }
                
                
                img {
                    max-width: 100%;
                    height: auto;
                    display: block;
                    margin: 10px auto;
                    border-radius: 10px;
                    cursor: pointer;
                    box-shadow: 0 4x 8px rgba(0,0,0,0,1);
                }
                ul {
                    padding-left: 20px;
                    margin: 5px 0;
                    text-align: left;
                    list-style-type: disc !important;
                }
                li { margin-bottom: 2px; display: list-item !important; }

                .katex-display {
                    margin: 0.5em 0 !important;
                }
                .katex {
                    font-weight: $cssFontWeight !important;
                    ${if (cssFontWeight >= 700) "text-shadow: 0.2px 0 0 $textColor, -0.1px 0 0 $textColor;" else ""}
                }.katex {
                    white-space: normal !important;
                }
            </style>
            <script>
                function parseContent(text) {
                    // Standarisasi baris baru
                    text = text.replace(/\r\n/g, '\n');
                    
                    // Bold & Italic
                    text = text.replace(/\*\*(.*?)\*\*/g, '<b>$1</b>');
                    text = text.replace(/(^|[^a-zA-Z0-9])_([^_]+)_(?![a-zA-Z0-9])/g, '$1<i>$2</i>');
                    
                    // ALignment Tags
                    text = text.replace(/\[justify\]([\s\S]*?)\[\/justify\]/g, function(m, p1) {
                        return '<div class="justify">' + p1.trim() + '</div>';
                    });
                    text = text.replace(/\[center\]([\s\S]*?)\[\/center\]/g, function(m, p1) {
                        return '<div class="center">' + p1.trim() + '</div>';
                    });
                    text = text.replace(/\[right\]([\s\S]*?)\[\/right\]/g, function(m, p1) {
                        return '<div class="right">' + p1.trim() + '</div>';
                    });
                    text = text.replace(/\[left\]([\s\S]*?)\[\/left\]/g, function(m, p1) {
                        return '<div class="left">' + p1.trim() + '</div>';
                    });
                    
                    // Numbered & Bullet List
//                    text = text.replace(/(^|\n|[ \t]*)(\d+)\.[ \t]+(.*?)(?=\n|$)/g, '$1<li class="num-item">$3</li>');
//                    text = text.replace(/(^|\n|[ \t]*)\*[ \t]+(.*?)(?=\n|$)/g, '$1<li class="bullet-item">$2</li>');

                    text = text.replace(/(?:^|\n)\*[ \t]+(.*?)(?=\n|$)/g, '\n<li class="bullet-item">$1</li>');
                    text = text.replace(/(^|\n)(\d+)\.[ \t]+(.*?)(?=\n|$)/g, '$1<li class="num-item">$3</li>');
                    
                    text = text.replace(/(?:<li class="bullet-item">[\s\S]*?<\/li>\s*)+/g, function(match) {
                        return '<ul>' + match.trim() + '</ul>';
                    });
    
                    text = text.replace(/(?:<li class="num-item">[\s\S]*?<\/li>\s*)+/g, function(match) {
                        return '<ol>' + match.trim() + '</ol>';
                    });
                    
                    // Images
                    text = text.replace(/!\[(.*?)\]\((.*?)\)/g, function(match, alt, src) {
                        return '<img src="' + src + '" alt="' + alt + '" ' +
                               'onclick="Android.openImage(\'' + src + '\')" ' +
                               'onload="updateHeight()" ' +
                               'onerror="this.style.display=\'none\'">';
                    });
                    
                    // Others
                    text = text.replace(/>\n/g, '>');
                    text = text.replace(/\n</g, '<');
                    text = text.replace(/\n/g, '<br>');
                    
                    return text;
                }
                
                function openImage(src) {
                    Android.openImage(src);
                }
                
                function updateHeight() {
                    window.requestAnimationFrame(function() {
                        var wrapper = document.getElementById('math-wrapper');
                        if (wrapper) {
                            // getBoundingClientRect lebih akurat untuk konten yang di-skew/render
                            var height = wrapper.getBoundingClientRect().height;
                            Android.updateHeight(height);
                        }
                    });
                }
                
                function renderMath() {
                    const wrapper = document.getElementById('math-wrapper');
                    if (!wrapper) return;
                    
                    wrapper.innerHTML = parseContent(wrapper.innerHTML);
                    
                    if (typeof renderMathInElement === 'function') {
                        renderMathInElement(wrapper, {
                            delimiters: [
                                {left: '$$', right: '$$', display: true},
                                {left: '$', right: '$', display: false}
                            ],
                            throwOnError : false
                        });
                    }
                    
                    if (window.ResizeObserver) {
                        new ResizeObserver(updateHeight).observe(wrapper);
                    }
                    
                    updateHeight();
                    setTimeout(updateHeight, 100);
                    setTimeout(updateHeight, 500);
                    setTimeout(updateHeight, 2500);
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
            .heightIn(webViewHeight)
            .graphicsLayer {
                alpha = if (isReady && webViewHeight > 10.dp) 1f else 0f
//                alpha = if (isReady) 1f else 0f
            },
        factory = { context ->
            WebView(context).apply {
                setLayerType(View.LAYER_TYPE_HARDWARE, null)

                settings.javaScriptEnabled = true
                settings.allowFileAccess = true
                settings.domStorageEnabled = true

                settings.cacheMode = android.webkit.WebSettings.LOAD_NO_CACHE

                setBackgroundColor(0)

                addJavascriptInterface(object : Any() {
                    @JavascriptInterface
                    fun updateHeight(height: Float) {
                        val heightInDp = (height / context.resources.displayMetrics.density).dp
                        if (heightInDp != webViewHeight) {
                            post {
                                webViewHeight = heightInDp + 4.dp
                                isReady = true
                                onRenderComplete()
                            }
                        }
//                        if (heightInDp > 1.dp) {
//                            webViewHeight = heightInDp + 12.dp
//                            isReady = true
//                            onRenderComplete()
//                        }
                    }

                    @JavascriptInterface
                    fun openImage(url: String) {
                        post { onImageClick(url) }
                    }
                }, "Android")
//                webViewClient = WebViewClient()

                isClickable = true
                isFocusable = true
                isEnabled = true

                setOnTouchListener { v, event ->
                    v.onTouchEvent(event)
                    false
                }

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        view?.loadUrl("javascript:updateHeight()")
                    }
                }
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
