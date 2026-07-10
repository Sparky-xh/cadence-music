# Cadence

A personal music ecosystem for discovery, lyrics, offline listening, and independent creators.
Kotlin + Jetpack Compose (Material 3), MVVM + Clean Architecture, Media3 ExoPlayer, Room, Firebase.

## What makes the UI different from mainstream streaming apps

- **Palette**: warm "espresso + vinyl gold" (`#1B1512` / `#E8A33D`) instead of Spotify green/black,
  Apple Music red/pink, or YouTube Music red/white. See `presentation/theme/Color.kt`.
- **Typography**: serif display face paired with a plain body sans — an editorial/record-sleeve
  feel rather than one geometric sans everywhere. See `presentation/theme/Type.kt`.
- **Player**: circular spinning "vinyl" art (`components/VinylArt.kt`) instead of a static square
  cover, a waveform-style seek bar (`components/WaveformSeekBar.kt`) instead of a bare line, and
  gesture zones (double-tap to seek, hold to 2x) instead of visible skip buttons for that motion.
- **Home**: horizontally-scrolling "shelves" (For You / Recently Played / Saved) rather than one
  algorithmic vertical feed.
- **Lyrics**: karaoke-style scale+fade on the current line, plus a from-scratch manual sync editor
  that lets anyone hand-time lyrics by tapping a flag during playback — most apps only let you
  *view* lyrics, not create synced ones.

## Project structure

```
app/src/main/java/com/cadence/music/
  di/            Hilt modules (Database, Network, Firebase, Player, Repository bindings)
  domain/        Pure Kotlin: models, repository interfaces, use cases — no Android imports
  data/
    local/       Room entities, DAOs, database
    remote/      Firebase data sources, LRCLIB lyrics API + LRC parser, DTOs
    mapper/      DTO/Entity <-> domain model conversions
    repository/  Repository implementations binding data sources to domain interfaces
  playback/      ExoPlayer/Media3 service, PlayerController facade, download manager setup
  presentation/  Compose screens + ViewModels, organized one folder per feature
```

## Building it

This project was generated in a sandbox with **no Android SDK, no emulator, and no network
access**, so it has not been compiled or run. Before opening it in Android Studio:

1. **Create a Firebase project** (console.firebase.google.com), enable Authentication
   (Email/Password, Google, Facebook providers), Firestore, and Storage. Download
   `google-services.json` into `app/`.
2. **Add a Facebook App ID** in `res/values/strings.xml` (`facebook_app_id`,
   `fb_login_protocol_scheme`) if you want Facebook/"Instagram" login to work — see the note in
   `presentation/auth/LoginScreen.kt` about why Instagram reuses the Facebook Login SDK.
3. **Wire Google Sign-In and Facebook Login in `MainActivity`**: both need an Activity host
   (Credential Manager's `GetGoogleIdOption`, and Facebook's `LoginManager`), which is why they
   are stubbed as TODO click handlers in `LoginScreen.kt` rather than implemented directly in a
   Composable.
4. **Open in Android Studio** (Otter/2025.1+ recommended), let Gradle sync pull dependencies —
   check `gradle/libs.versions.toml` against the latest stable versions first; it was written
   from training knowledge without live access to Maven Central, so a couple of patch versions
   may have moved on.
5. **Add real fonts (optional)**: drop Fraunces/Inter variable font files into `res/font/` and
   flip the two `TODO` lines in `presentation/theme/Type.kt` — it compiles today with system
   fonts as a safe fallback.
6. **Run on a device/emulator with API 26+.**

## Known gaps to close before shipping

- **Billing**: `PremiumViewModel.upgrade()` writes the subscription tier directly to Firestore.
  Replace with real Play Billing Library purchase flow + server-side receipt verification before
  this ever reaches production — a client that can grant itself Premium is not secure.
- **Search**: Firestore has no native full-text search; `FirestoreMusicDataSource` uses a
  prefix-range workaround. For real fuzzy/typo-tolerant search, mirror song writes into
  Algolia or Typesense and query that instead.
- **Duration probing on upload**: `CreatorRepositoryImpl.uploadSong` leaves `durationMs = 0`.
  Either probe the file client-side before upload (e.g. with `MediaMetadataRetriever`) or run a
  Cloud Function that processes the file server-side.
- **Lyrics provider**: wired to LRCLIB (free, no API key). Swap in Musixmatch/Genius in
  `data/remote/lyrics/` if you need a larger catalog — both require developer accounts and a
  different response shape than `LrcLibResponseDto`.
- **Equalizer**: spec calls for equalizer support; Media3 doesn't ship one, so plug in
  `android.media.audiofx.Equalizer` attached to the ExoPlayer's audio session ID
  (`exoPlayer.audioSessionId`) — a real UI for this was out of scope here but the attach point is
  ready in `playback/CadencePlaybackService`.
- **Instrumented/Hilt tests**: `LoginScreenTest` is a plain Compose smoke test. For tests that
  exercise real ViewModels end-to-end, add `@HiltAndroidTest` + a `TestRepositoryModule` that
  swaps in fakes — not included here to avoid bloating the sample further.

## Testing

- **Unit**: `app/src/test/.../LrcParserTest.kt` (pure logic, no mocks) and
  `SearchViewModelTest.kt` (MockK + Turbine + a coroutine `TestDispatcher`).
  Run with `./gradlew testDebugUnitTest`.
- **Instrumented**: `app/src/androidTest/.../LoginScreenTest.kt`, a Compose UI smoke test.
  Run with `./gradlew connectedDebugAndroidTest` against a device/emulator.
- **Manual QA pass** worth doing once it builds: sign-up → onboarding → Home recommendations
  populate → play a song → double-tap/hold gestures on the player → open Lyrics → add manual
  lyrics → sync a couple of lines → background the app and check the notification controls →
  download a song, then toggle airplane mode and replay it from Downloads → upgrade to Creator →
  upload a song → confirm it appears in Search and the Creator dashboard.

## Stage map (matches the brief's build order)

| Stage | Where it lives |
|---|---|
| 1. Project setup | `settings.gradle.kts`, `build.gradle.kts`, `gradle/libs.versions.toml`, `app/build.gradle.kts` |
| 2. Authentication | `data/remote/firebase/FirebaseAuthDataSource.kt`, `presentation/auth/` |
| 3. UI design | `presentation/theme/`, `presentation/components/`, `presentation/navigation/` |
| 4. Music playback | `playback/`, `presentation/player/` |
| 5. Lyrics system | `data/remote/lyrics/`, `presentation/lyrics/` |
| 6. Downloads | `playback/DownloadUtil.kt`, `data/repository/DownloadRepositoryImpl.kt`, `presentation/downloads/` |
| 7. Creator/premium | `presentation/creator/`, `presentation/premium/`, `data/repository/CreatorRepositoryImpl.kt` |
| 8. Final optimization | proguard rules are in place; still needed: real device profiling, baseline profiles, and the "known gaps" above |
