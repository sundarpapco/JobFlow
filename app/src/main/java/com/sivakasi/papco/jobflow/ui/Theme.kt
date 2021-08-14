package com.sivakasi.papco.jobflow.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.sivakasi.papco.jobflow.R

val pink = Color(0xff9c27b0)
private val seaGreen = Color(0xff03dac5)
private val surface = Color(0xff1f1f1f)
private val borderGrey = Color(0xff707070)
private val white = Color(0xffffffff)
private val black = Color(0xff000000)
private val red = Color(0xffc90b0b)

private val arialFamily = FontFamily(
    Font(R.font.arial)
)

private val h6 = TextStyle(
    fontFamily = arialFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 16.sp
)


private val darkColors = darkColors(
    primary = pink,
    primaryVariant = pink,
    onPrimary = white,
    secondary = seaGreen,
    secondaryVariant = borderGrey,
    onSecondary = black,
    surface = surface,
    onSurface = white,
    background = black,
    onBackground = white,
    error = red,
    onError = white
)

private val lightColors = darkColors.copy(
    isLight = true
)

private val jobFlowRippleTheme = object : RippleTheme {

    @Composable
    override fun defaultColor(): Color {
        return MaterialTheme.colors.primary
    }

    @Composable
    override fun rippleAlpha(): RippleAlpha {
        return RippleAlpha(
            0.3f, 0.3f, 0.3f, 0.3f
        )
    }
}


@Composable
fun JobFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) darkColors else lightColors
    MaterialTheme(
        colors = colors,
        typography = MaterialTheme.typography.copy(h6 = h6),
    ) {
        CompositionLocalProvider(LocalRippleTheme provides jobFlowRippleTheme, content = content)
    }
}