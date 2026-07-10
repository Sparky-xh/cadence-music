package com.cadence.music

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.cadence.music.presentation.navigation.CadenceNavGraph
import com.cadence.music.presentation.theme.CadenceTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single-activity host. Everything below this is Compose + Navigation-Compose; there is no
 * second Activity in the app (including the player, which is a navigation destination rather
 * than its own Activity so shared-element/gesture transitions stay smooth).
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
        )

        setContent {
            val isDark = isSystemInDarkTheme()
            CadenceTheme(darkTheme = isDark) {
                CadenceNavGraph()
            }
        }
    }
}
