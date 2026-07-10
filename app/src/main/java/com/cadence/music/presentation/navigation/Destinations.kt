package com.cadence.music.presentation.navigation

/** All navigation destinations in one place. Player/Lyrics take an optional songId argument;
 *  when absent they fall back to whatever PlayerController currently has queued. */
sealed class Destination(val route: String) {
    data object Splash : Destination("splash")
    data object Login : Destination("login")
    data object Signup : Destination("signup")
    data object Onboarding : Destination("onboarding")

    data object Home : Destination("home")
    data object Search : Destination("search")
    data object Library : Destination("library")
    data object Profile : Destination("profile")

    data object Player : Destination("player")
    data object Lyrics : Destination("lyrics")
    data object LyricsEditor : Destination("lyrics_editor")

    data object Downloads : Destination("downloads")
    data object Premium : Destination("premium")

    data object CreatorDashboard : Destination("creator_dashboard")
    data object UploadSong : Destination("upload_song")

    companion object {
        /** Bottom nav only shows these four — everything else (player, lyrics, premium, creator,
         *  onboarding, auth) is reached by pushing on top, matching a "focused task" feel rather
         *  than exposing every screen as a permanent tab. */
        val bottomNavItems = listOf(Home, Search, Library, Profile)
    }
}
