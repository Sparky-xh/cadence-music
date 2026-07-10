package com.cadence.music.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary = Amber500,
    onPrimary = Espresso900,
    primaryContainer = Amber700,
    onPrimaryContainer = ParchmentText,
    secondary = GenreLofi,
    onSecondary = Espresso900,
    background = Espresso900,
    onBackground = ParchmentText,
    surface = Espresso800,
    onSurface = ParchmentText,
    surfaceVariant = Espresso700,
    onSurfaceVariant = Cream100,
    error = Rust500,
    onError = Espresso900
)

private val LightColors = lightColorScheme(
    primary = Amber700,
    onPrimary = Cream50,
    primaryContainer = Amber300,
    onPrimaryContainer = InkText,
    secondary = GenreJazz,
    onSecondary = Cream50,
    background = Cream50,
    onBackground = InkText,
    surface = Cream100,
    onSurface = InkText,
    surfaceVariant = Cream200,
    onSurfaceVariant = Espresso700,
    error = Rust500,
    onError = Cream50
)

@Composable
fun CadenceTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = CadenceTypography,
        shapes = CadenceShapes,
        content = content
    )
}
