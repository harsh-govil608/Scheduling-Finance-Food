package com.lifeos.expensecapture.family.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

/**
 * Family module's identity gate (2026-08) - see FamilyAuthRepository's kdoc for why this exists
 * only for this module. Not shown at app launch; only reached when the founder (or a family
 * member) opens the new Family pillar - everything else in the app still needs no account at all.
 */
@Composable
fun FamilySignInScreen(viewModel: FamilyAppViewModel) {
    var isSignUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    val error by viewModel.authError.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            if (isSignUp) "Create your family account" else "Sign in to Family",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            "Family sharing needs an account so other members' phones can see the same data - " +
                "everything else in this app still works with no account at all.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )

        if (isSignUp) {
            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("Your name") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
        }
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )

        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 8.dp))
        }

        Button(
            onClick = {
                if (isSignUp) viewModel.signUp(email.trim(), password, displayName.trim())
                else viewModel.signIn(email.trim(), password)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isSignUp) "Create account" else "Sign in")
        }
        TextButton(onClick = { isSignUp = !isSignUp }, modifier = Modifier.fillMaxWidth()) {
            Text(if (isSignUp) "Already have an account? Sign in" else "New to Family? Create an account")
        }
    }
}
