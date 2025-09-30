package com.sivakasi.papco.jobflow.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalRippleConfiguration
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RippleConfiguration
import androidx.compose.material.Surface
import androidx.compose.material.darkColors
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

private val segoeui = FontFamily(
    Font(R.font.segoeui)
)

private val screenTitle = TextStyle(
    fontFamily = segoeui,
    fontWeight = FontWeight.Normal,
    fontSize = 24.sp
)

private val appBarTitle=TextStyle(
    fontFamily = arialFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 18.sp
)

private val h6 = TextStyle(
    fontFamily = arialFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 16.sp
)

private val detailText = TextStyle(
    fontFamily = segoeui,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp
)

private val subDetailText = TextStyle(
    fontFamily = segoeui,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp
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


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun JobFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) darkColors else lightColors
    MaterialTheme(
        colors = colors,
        typography = MaterialTheme.typography.copy(
            h4= screenTitle,
            h5= appBarTitle,
            h6 = h6,
            body1 = detailText,
            body2 = subDetailText
        ),
        shapes = MaterialTheme.shapes.copy(
            medium = RoundedCornerShape(15.dp)
        )
    ) {

        val rippleColor = MaterialTheme.colors.primary
        val rippleConfiguration = remember(true) {
            RippleConfiguration(
                color = rippleColor,
                rippleAlpha = RippleAlpha(
                    0.6f, 0.6f, 0.6f, 0.6f
                )
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.surface)
                .windowInsetsPadding(WindowInsets.statusBars)
        ){
            CompositionLocalProvider(LocalRippleConfiguration provides rippleConfiguration, content = content)
        }

    }
}