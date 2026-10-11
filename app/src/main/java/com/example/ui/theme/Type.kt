package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

// DTDC Logo Matching Typography
// Rajdhani captures the bold, geometric, aerodynamic industrial sans-serif of the DTDC brand mark
val DtdcBrandFontFamily = FontFamily(
    Font(R.font.rajdhani, FontWeight.Normal),
    Font(R.font.rajdhani, FontWeight.Medium),
    Font(R.font.rajdhani_bold, FontWeight.SemiBold),
    Font(R.font.rajdhani_bold, FontWeight.Bold),
    Font(R.font.rajdhani_bold, FontWeight.ExtraBold),
    Font(R.font.rajdhani_bold, FontWeight.Black)
)

// Montserrat captures the clean corporate geometric sans-serif of the subtext
val DtdcCorporateFontFamily = FontFamily(
    Font(R.font.montserrat, FontWeight.Normal),
    Font(R.font.montserrat, FontWeight.Medium),
    Font(R.font.montserrat_bold, FontWeight.SemiBold),
    Font(R.font.montserrat_bold, FontWeight.Bold),
    Font(R.font.montserrat_bold, FontWeight.ExtraBold)
)

// Material 3 typography with DTDC corporate aesthetic
val Typography =
    Typography(
        displayLarge = TextStyle(
            fontFamily = DtdcBrandFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp
        ),
        titleLarge = TextStyle(
            fontFamily = DtdcBrandFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp
        ),
        titleMedium = TextStyle(
            fontFamily = DtdcBrandFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = DtdcCorporateFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            letterSpacing = 0.5.sp
        ),
        labelLarge = TextStyle(
            fontFamily = DtdcBrandFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    )
