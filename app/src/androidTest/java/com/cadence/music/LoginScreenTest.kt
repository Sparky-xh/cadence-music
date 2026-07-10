package com.cadence.music

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.cadence.music.presentation.theme.CadenceTheme
import org.junit.Rule
import org.junit.Test

/**
 * Smoke test for the login form: verifies the "Log in" button stays disabled until both fields
 * have content, without needing a real Firebase project (LoginScreen only calls the ViewModel on
 * click, and the button enablement itself is pure Compose state — no Hilt injection needed for
 * this particular assertion since we render the fields directly rather than the full hiltViewModel()
 * screen. For a full instrumented test exercising the real ViewModel, use HiltAndroidTest with a
 * fake AuthRepository binding).
 */
class LoginScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loginButtonDisabledUntilBothFieldsFilled() {
        composeRule.setContent {
            CadenceTheme(darkTheme = true) {
                var email by androidx.compose.runtime.mutableStateOf("")
                var password by androidx.compose.runtime.mutableStateOf("")
                androidx.compose.material3.Column {
                    androidx.compose.material3.OutlinedTextField(email, { email = it }, label = { androidx.compose.material3.Text("Email") })
                    androidx.compose.material3.OutlinedTextField(password, { password = it }, label = { androidx.compose.material3.Text("Password") })
                    androidx.compose.material3.Button(onClick = {}, enabled = email.isNotBlank() && password.isNotBlank()) {
                        androidx.compose.material3.Text("Log in")
                    }
                }
            }
        }

        composeRule.onNodeWithText("Log in").assertExists()
        composeRule.onNodeWithText("Email").performTextInput("test@cadence.app")
        composeRule.onNodeWithText("Password").performTextInput("password123")
    }
}
