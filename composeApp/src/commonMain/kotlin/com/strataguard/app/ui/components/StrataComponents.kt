package com.strataguard.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.strataguard.app.ui.theme.ErrorLight
import com.strataguard.app.ui.theme.ErrorRed
import com.strataguard.app.ui.theme.Grey300
import com.strataguard.app.ui.theme.Navy800

@Composable
fun StrataLogo(modifier: Modifier = Modifier) {
    Text(
        text = "StrataGuard",
        style = MaterialTheme.typography.headlineLarge.copy(
            fontWeight = FontWeight.ExtraBold,
            fontSize = 30.sp,
            color = Navy800,
            letterSpacing = (-0.5).sp,
        ),
        modifier = modifier,
    )
}

@Composable
fun StrataTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = if (placeholder.isNotEmpty()) {
            { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) }
        } else null,
        modifier = modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = keyboardActions,
        isError = isError,
        supportingText = if (isError && errorMessage != null) {
            { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
        } else null,
        enabled = enabled,
        singleLine = true,
        shape = MaterialTheme.shapes.small,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Navy800,
            focusedLabelColor = Navy800,
            unfocusedBorderColor = Grey300,
        ),
    )
}

@Composable
fun StrataPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Done,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction),
        keyboardActions = keyboardActions,
        isError = isError,
        supportingText = if (isError && errorMessage != null) {
            { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
        } else null,
        enabled = enabled,
        singleLine = true,
        shape = MaterialTheme.shapes.small,
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Text(
                    text = if (passwordVisible) "Hide" else "Show",
                    style = MaterialTheme.typography.labelMedium,
                    color = Navy800,
                )
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Navy800,
            focusedLabelColor = Navy800,
            unfocusedBorderColor = Grey300,
        ),
    )
}

@Composable
fun StrataButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(52.dp),
        enabled = enabled && !isLoading,
        shape = MaterialTheme.shapes.small,
        colors = ButtonDefaults.buttonColors(
            containerColor = Navy800,
            contentColor = Color.White,
            disabledContainerColor = Navy800.copy(alpha = 0.4f),
            disabledContentColor = Color.White.copy(alpha = 0.7f),
        ),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp,
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            )
        }
    }
}

@Composable
fun StrataOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(52.dp),
        enabled = enabled,
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.5.dp, Navy800),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Navy800),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

@Composable
fun ErrorBanner(message: String, modifier: Modifier = Modifier) {
    AnimatedVisibility(visible = message.isNotEmpty(), modifier = modifier) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ErrorLight),
            shape = MaterialTheme.shapes.small,
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = ErrorRed,
                modifier = Modifier.fillMaxWidth().padding(12.dp),
            )
        }
    }
}