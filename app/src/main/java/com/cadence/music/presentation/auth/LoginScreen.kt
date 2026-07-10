package com.cadence.music.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Auth entry point. Google Sign-In here should be wired through Credential Manager
 * (androidx.credentials + GetGoogleIdOption) in MainActivity/a rememberLauncherForActivityResult
 * host, which then calls viewModel.onGoogleIdToken(...) — omitted from this Composable body since
 * Credential Manager needs an Activity context, not just a Composable. Facebook Login similarly
 * needs LoginManager wired in the hosting Activity, which then forwards the access token to
 * viewModel.onFacebookAccessToken(...).
 *
 * "Continue with Instagram": Instagram does not provide a general-purpose OAuth login button for
 * third-party consumer apps the way Google/Facebook do (its API surface today is scoped to
 * business/creator content permissions, not identity login). We keep the button because it is in
 * the spec, but it authenticates through the same Facebook Login SDK/flow under the hood — most
 * people with an Instagram account already have a linked Facebook identity. This is disclosed to
 * the user via the subtitle text rather than silently pretending it is a separate provider.
 */
@Composable
fun LoginScreen(
    onLoggedIn: (needsOnboarding: Boolean) -> Unit,
    onNavigateToSignup: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.loggedInUser) {
        uiState.loggedInUser?.let { onLoggedIn(!it.hasCompletedOnboarding) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(64.dp))
        Text("Cadence", style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.primary)
        Text("Music worth keeping", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(40.dp))

        OutlinedTextField(
            value = email, onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password, onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, contentDescription = null)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        uiState.errorMessage?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(20.dp))
        Button(
            onClick = { viewModel.onEmailLogin(email, password) },
            enabled = !uiState.isLoading && email.isNotBlank() && password.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            if (uiState.isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            else Text("Log in")
        }

        Spacer(Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(Modifier.weight(1f))
            Text("  or continue with  ", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            HorizontalDivider(Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))

        OutlinedButton(onClick = { /* wire to Credential Manager in the hosting Activity */ }, modifier = Modifier.fillMaxWidth()) {
            Text("Continue with Google")
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = { /* wire to Facebook LoginManager in the hosting Activity */ }, modifier = Modifier.fillMaxWidth()) {
            Text("Continue with Facebook")
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = { /* same Facebook Login flow — see file header note */ }, modifier = Modifier.fillMaxWidth()) {
            Text("Continue with Instagram")
        }
        Text(
            "Instagram sign-in uses your linked Facebook identity",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp)
        )

        Spacer(Modifier.height(32.dp))
        TextButton(onClick = onNavigateToSignup) { Text("New here? Create an account") }
        Spacer(Modifier.height(24.dp))
    }
}
