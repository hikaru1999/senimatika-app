package com.LambdaProject.MathArt

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont

val requinerFont = FontFamily(
    Font(R.font.requiner, FontWeight.Normal)
)

val vactoryFont = FontFamily(
    Font(R.font.vactory, FontWeight.Normal)
)

val helveticaFont = FontFamily(
    Font(R.font.helvetica, FontWeight.Normal),
    Font(R.font.helvetica_bold, FontWeight.Bold, FontStyle.Normal),
    Font(R.font.helvetica_light, FontWeight.Light),
    Font(R.font.helvetica_bold_rounded, FontWeight.Bold),
    Font(R.font.helvetica_oblique, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.helvetica_compressed, FontWeight.W200),
    Font(R.font.helvetica_bold_oblique, FontWeight.Bold, FontStyle.Italic)
)

val interFontFamily  = FontFamily(
    Font(
        googleFont = GoogleFont("Inter"),
        fontProvider = GoogleFont.Provider(
            providerAuthority = "com.google.android.gms.fonts",
            providerPackage = "com.google.android.gms",
            certificates = R.array.com_google_android_gms_fonts_certs
        ),
        weight = FontWeight.Normal
    )
)