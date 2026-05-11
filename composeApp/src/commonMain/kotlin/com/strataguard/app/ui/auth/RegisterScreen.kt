package com.strataguard.app.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.strataguard.app.ui.components.ErrorBanner
import com.strataguard.app.ui.components.StrataButton
import com.strataguard.app.ui.components.StrataPasswordField
import com.strataguard.app.ui.components.StrataTextField
import com.strataguard.app.ui.theme.Amber500
import com.strataguard.app.ui.theme.Grey500
import com.strataguard.app.ui.theme.Navy800
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: RegisterViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) onRegisterSuccess()
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 24.dp),
        ) {
            Spacer(Modifier.height(16.dp))

            IconButton(onClick = onNavigateBack, enabled = !state.isLoading) {
                Text("←", style = MaterialTheme.typography.titleLarge, color = Navy800)
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Create your account",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = Navy800,
            )

            Spacer(Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Free during beta",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Amber500,
                )
                Text("·", color = Grey500)
                Text(
                    text = "NSW & VIC supported",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Grey500,
                )
            }

            Spacer(Modifier.height(28.dp))

            ErrorBanner(message = state.errorMessage)
            if (state.errorMessage.isNotEmpty()) Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StrataTextField(
                    value = state.firstName,
                    onValueChange = viewModel::onFirstNameChange,
                    label = "First name",
                    modifier = Modifier.weight(1f),
                    imeAction = ImeAction.Next,
                    enabled = !state.isLoading,
                )
                StrataTextField(
                    value = state.lastName,
                    onValueChange = viewModel::onLastNameChange,
                    label = "Last name",
                    modifier = Modifier.weight(1f),
                    imeAction = ImeAction.Next,
                    enabled = !state.isLoading,
                )
            }

            Spacer(Modifier.height(12.dp))

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
                label = "Password (8+ characters)",
                imeAction = ImeAction.Next,
                enabled = !state.isLoading,
            )

            Spacer(Modifier.height(12.dp))

            StrataPasswordField(
                value = state.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChange,
                label = "Confirm password",
                imeAction = ImeAction.Done,
                enabled = !state.isLoading,
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Your state",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Navy800,
            )

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                AustralianState.entries.forEach { ausState ->
                    Row(
                        modifier = Modifier
                            .selectable(
                                selected = state.selectedState == ausState,
                                onClick = { viewModel.onStateChange(ausState) },
                                role = Role.RadioButton,
                                enabled = !state.isLoading,
                            )
                            .padding(end = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        RadioButton(
                            selected = state.selectedState == ausState,
                            onClick = null,
                            colors = RadioButtonDefaults.colors(selectedColor = Navy800),
                        )
                        Text(
                            text = ausState.label,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (state.selectedState == ausState) Navy800 else Grey500,
                        )
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            StrataButton(
                text = "Create Free Account",
                onClick = viewModel::register,
                isLoading = state.isLoading,
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "By creating an account you agree to our Terms of Service and Privacy Policy. Your data is stored securely in Australia.",
                style = MaterialTheme.typography.bodySmall,
                color = Grey500,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}