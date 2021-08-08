package com.sivakasi.papco.jobflow.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val pink = Color(0xff9c27b0)
private val seaGreen = Color(0xff03dac5)
private val surface = Color(0xff4b4a4a)
private val borderGrey = Color(0xff707070)
private val white = Color(0xffffffff)
private val black = Color(0xff000000)
private val red = Color(0xffc90b0b)

private val darkColors = darkColors(
    primary = pink,
    primaryVariant = pink,
    onPrimary=white,
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

@Composable
fun JobFlowTheme(
    darkTheme:Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
){
    val colors = if (darkTheme) darkColors else lightColors
    MaterialTheme(
        colors = colors,
        content = content
    )
}