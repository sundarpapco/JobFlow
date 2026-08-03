package com.sivakasi.papco.jobflow.ui

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

/*
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




@Composable
fun JobFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) darkColors else lightColors
    androidx.compose.material.MaterialTheme(
        colors = colors,
        typography = androidx.compose.material.MaterialTheme.typography.copy(
            h4= screenTitle,
            h5= appBarTitle,
            h6 = h6,
            body1 = detailText,
            body2 = subDetailText
        ),
        shapes = androidx.compose.material.MaterialTheme.shapes.copy(
            medium = RoundedCornerShape(15.dp)
        )
    ) {

        val rippleColor = MaterialTheme.colorScheme.primary
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
                .background(MaterialTheme.colorScheme.surface)
                .windowInsetsPadding(WindowInsets.statusBars)
        ){
            CompositionLocalProvider(LocalRippleConfiguration provides rippleConfiguration, content = content)

        }

    }
}*/
