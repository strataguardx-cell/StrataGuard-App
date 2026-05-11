package com.strataguard.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.strataguard.app.data.auth.AuthRepository
import com.strataguard.app.ui.theme.Amber500
import com.strataguard.app.ui.theme.Grey500
import com.strataguard.app.ui.theme.Navy700
import com.strataguard.app.ui.theme.Navy800
import com.strataguard.app.ui.theme.Navy900
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

private data class Feature(
    val emoji: String,
    val title: String,
    val description: String,
    val comingSoon: Boolean = false,
)

private val features = listOf(
    Feature("🏢", "Search a Strata Plan", "Check building health, defects & sinking fund status"),
    Feature("📸", "Document Evidence", "Timestamped photo & video records you control"),
    Feature("⚖️", "Dispute Risk Check", "AI cross-references strata defects against your bond claim", comingSoon = true),
    Feature("📋", "Know Your Rights", "NSW & VIC state-specific tenant guidance", comingSoon = true),
)

@Composable
fun HomeScreen(
    onSignOut: () -> Unit,
    onSearchStrata: () -> Unit = {},
    onDocumentEvidence: () -> Unit = {},
    authRepository: AuthRepository = koinInject(),
) {
    val scope = rememberCoroutineScope()
    val displayName = authRepository.currentUserDisplayName ?: authRepository.currentUserEmail ?: "there"
    val firstName = displayName.split(" ").firstOrNull() ?: displayName

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(listOf(Navy900, Navy700)),
                    )
                    .padding(24.dp),
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "StrataGuard",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = androidx.compose.ui.graphics.Color.White,
                            ),
                        )
                        TextButton(onClick = {
                            scope.launch {
                                authRepository.signOut()
                                onSignOut()
                            }
                        }) {
                            Text(
                                "Sign out",
                                style = MaterialTheme.typography.bodyMedium,
                                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f),
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    Text(
                        text = "Hi, $firstName 👋",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.ui.graphics.Color.White,
                        ),
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = "You're protected by StrataGuard.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.75f),
                        ),
                    )

                    Spacer(Modifier.height(16.dp))

                    // Beta badge
                    Box(
                        modifier = Modifier
                            .background(Amber500.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = "🚀  Beta — NSW & VIC",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Amber500,
                                fontWeight = FontWeight.SemiBold,
                            ),
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "What would you like to do?",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Navy800,
                modifier = Modifier.padding(horizontal = 24.dp),
            )

            Spacer(Modifier.height(12.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(features) { feature ->
                    FeatureCard(
                        feature = feature,
                        onClick = when {
                            feature.title == "Search a Strata Plan" -> onSearchStrata
                            feature.title == "Document Evidence" -> onDocumentEvidence
                            else -> null
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureCard(feature: Feature, onClick: (() -> Unit)? = null) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = feature.emoji, fontSize = 28.sp)

            Spacer(Modifier.height(10.dp))

            Text(
                text = feature.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Navy800,
                ),
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = feature.description,
                style = MaterialTheme.typography.bodySmall,
                color = Grey500,
            )

            if (feature.comingSoon) {
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .background(Amber500.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = "Coming soon",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Amber500,
                            fontWeight = FontWeight.Medium,
                        ),
                    )
                }
            }
        }
    }
}