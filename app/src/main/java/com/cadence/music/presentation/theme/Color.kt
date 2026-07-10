package com.cadence.music.presentation.theme

import androidx.compose.ui.graphics.Color

/**
 * "Analog warmth" palette — deliberately not Spotify green/black, not Apple Music red/pink, not
 * YouTube Music red/white. The idea is a record-sleeve, editorial-zine feeling: warm espresso
 * darks, a copper/amber accent standing in for "vinyl gold," and a small set of muted genre
 * tints (sage, terracotta, dusty lilac...) used only for genre-shelf accents on Home, never for
 * primary UI chrome, so the app reads as one cohesive brand rather than a rainbow of tags.
 */

// Core neutrals
val Espresso900 = Color(0xFF1B1512) // dark theme background
val Espresso800 = Color(0xFF241D19) // dark theme surface
val Espresso700 = Color(0xFF352C26) // dark theme elevated surface / cards
val Cream50 = Color(0xFFF5EDE4)      // light theme background
val Cream100 = Color(0xFFEBE1D3)     // light theme surface
val Cream200 = Color(0xFFDCCEB9)     // light theme elevated surface / cards
val InkText = Color(0xFF241D19)      // primary text on light
val ParchmentText = Color(0xFFF3EADD) // primary text on dark

// Brand accent — "vinyl gold"
val Amber500 = Color(0xFFE8A33D)
val Amber300 = Color(0xFFF0C27A)
val Amber700 = Color(0xFFB97F26)

// Secondary accent for destructive/critical states (kept warm, not a cold system red)
val Rust500 = Color(0xFFC1543A)

// Genre tint palette (Home shelves + genre chips only)
val GenreIndie = Color(0xFFE8A33D)
val GenreLofi = Color(0xFF8AA6A3)
val GenreElectronic = Color(0xFFC46A57)
val GenreHipHop = Color(0xFFB98CCE)
val GenreAcoustic = Color(0xFFD9B36C)
val GenreRock = Color(0xFFA8524A)
val GenreJazz = Color(0xFF6E8B74)
val GenreClassical = Color(0xFF9C8ECF)
