package com.strataguard.app.ui.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.strataguard.app.data.auth.AuthRepository
import com.strataguard.app.data.auth.AuthResult
import com.strataguard.app.ui.components.ErrorBanner
import com.strataguard.app.ui.components.StrataButton
import com.strataguard.app.ui.components.StrataTextField
import com.strataguard.app.ui.theme.Grey500
import com.strataguard.app.ui.theme.Navy800
import com.strataguard.app.ui.theme.SuccessGreen
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    authRepository: AuthRepository = koinInject(),
) {
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
        ) {
            Spacer(Modifier.height(16.dp))

            IconButton(onClick = onNavigateBack, enabled = !isLoading) {
                Text("←", style = MaterialTheme.typography.titleLarge, color = Navy800)
            }

            Spacer(Modifier.height(32.dp))

            Text(
                text = "✉",
                fontSize = 48.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Reset your password",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = Navy800,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Enter your email and we'll send you a link to reset your password.",
                style = MaterialTheme.typography.bodyMedium,
                color = Grey500,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(32.dp))

            if (successMessage.isNotEmpty()) {
                androidx.compose.material3.Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.CardDefaults.cardColors(
                        containerColor = com.strataguard.app.ui.theme.SuccessLight,
                    ),
                    shape = MaterialTheme.shapes.small,
                ) {
                    Text(
                        text = successMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SuccessGreen,
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            ErrorBanner(message = errorMessage)
            if (errorMessage.isNotEmpty()) Spacer(Modifier.height(16.dp))

            StrataTextField(
                value = email,
                onValueChange = {
                    email = it
                    errorMessage = ""
                    successMessage = ""
                },
                label = "Email address",
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done,
                enabled = !isLoading && successMessage.isEmpty(),
            )

            Spacer(Modifier.height(24.dp))

            StrataButton(
                text = "Send Reset Link",
                onClick = {
                    if (email.isBlank()) {
                        errorMessage = "Please enter your email address."
                        return@StrataButton
                    }
                    scope.launch {
                        isLoading = true
                        errorMessage = ""
                        when (val result = authRepository.sendPasswordReset(email)) {
                            is AuthResult.Success ->
                                successMessage = "Reset link sent! Check your inbox for ${email.trim()}."
                            is AuthResult.Error ->
                                errorMessage = result.message
                        }
                        isLoading = false
                    }
                },
                isLoading = isLoading,
                enabled = successMessage.isEmpty(),
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Back to Sign In",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = Navy800,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isLoading) { onNavigateBack() },
            )
        }
    }
}