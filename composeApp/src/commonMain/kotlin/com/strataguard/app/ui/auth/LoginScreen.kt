package com.strataguard.app.ui.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.strataguard.app.ui.components.ErrorBanner
import com.strataguard.app.ui.components.StrataButton
import com.strataguard.app.ui.components.StrataLogo
import com.strataguard.app.ui.components.StrataOutlinedButton
import com.strataguard.app.ui.components.StrataPasswordField
import com.strataguard.app.ui.components.StrataTextField
import com.strataguard.app.ui.theme.Grey500
import com.strataguard.app.ui.theme.Navy800
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) onLoginSuccess()
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(72.dp))

            StrataLogo()

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Know your building. Protect your rights.",
                style = MaterialTheme.typography.bodyMedium,
                color = Grey500,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(48.dp))

            ErrorBanner(message = state.errorMessage)

            if (state.errorMessage.isNotEmpty()) Spacer(Modifier.height(16.dp))

            StrataTextField(
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                label = "Email address",
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
                enabled = !state.isLoading,
            )

            Spacer(Modifier.height(12.dp))

            StrataPasswordField(
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                label = "Password",
                imeAction = ImeAction.Done,
                enabled = !state.isLoading,
            )

            Spacer(Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text(
                    text = "Forgot password?",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = Navy800,
                    modifier = Modifier.clickable(enabled = !state.isLoading) { onNavigateToForgotPassword() },
                )
            }

            Spacer(Modifier.height(24.dp))

            StrataButton(
                text = "Sign In",
                onClick = viewModel::signIn,
                isLoading = state.isLoading,
            )

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline)
                Text("or", style = MaterialTheme.typography.bodySmall, color = Grey500)
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline)
            }

            Spacer(Modifier.height(24.dp))

            StrataOutlinedButton(
                text = "Create a free account",
                onClick = onNavigateToRegister,
                enabled = !state.isLoading,
            )

            Spacer(Modifier.height(32.dp))

            Text(
                text = "By signing in you agree to our Terms of Service and Privacy Policy.",
                style = MaterialTheme.typography.bodySmall,
                color = Grey500,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}