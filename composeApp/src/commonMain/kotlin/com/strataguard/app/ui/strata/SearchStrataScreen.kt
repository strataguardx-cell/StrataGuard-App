package com.strataguard.app.ui.strata

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.strataguard.app.data.strata.StrataPlan
import com.strataguard.app.data.strata.hasActiveWorkOrders
import com.strataguard.app.ui.theme.Amber500
import com.strataguard.app.ui.theme.ErrorRed
import com.strataguard.app.ui.theme.Grey500
import com.strataguard.app.ui.theme.Navy100
import com.strataguard.app.ui.theme.Navy800
import com.strataguard.app.ui.theme.Navy900
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchStrataScreen(
    onNavigateBack: () -> Unit,
    onPlanSelected: (String) -> Unit,
    viewModel: SearchStrataViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Search a Strata Plan",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", style = MaterialTheme.typography.titleLarge, color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy900),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // State toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Navy900)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf("NSW", "VIC").forEach { st ->
                    val selected = state.selectedState == st
                    Box(
                        modifier = Modifier
                            .background(
                                if (selected) Amber500 else Color.White.copy(alpha = 0.12f),
                                RoundedCornerShape(20.dp),
                            )
                            .clickable { viewModel.onStateToggle(st) }
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            st,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) Navy900 else Color.White,
                            ),
                        )
                    }
                }
            }

            // Search field
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                placeholder = {
                    Text(
                        if (state.selectedState == "NSW") "SP number or suburb (e.g. SP83985, Mascot)"
                        else "OC number or suburb (e.g. OC1234567, Southbank)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Grey500,
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Navy800,
                    unfocusedBorderColor = Navy100,
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { viewModel.retry() }),
                leadingIcon = { Text("🔍", style = MaterialTheme.typography.bodyLarge) },
                trailingIcon = {
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Navy800)
                    } else if (state.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQueryChange("") }) {
                            Text("✕", style = MaterialTheme.typography.bodyMedium, color = Grey500)
                        }
                    }
                },
            )

            Divider(color = Navy100)

            when {
                state.isSeeding -> LoadingHint("Setting up data…")
                state.error != null -> ErrorMessage(state.error!!, onRetry = viewModel::retry)
                state.query.length < 2 && !state.hasSearched -> SearchHint(state.selectedState)
                state.hasSearched && state.results.isEmpty() && !state.isLoading -> NoResults(state.query)
                else -> ResultsList(results = state.results, onPlanSelected = onPlanSelected)
            }
        }
    }
}

@Composable
private fun ResultsList(results: List<StrataPlan>, onPlanSelected: (String) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            if (results.isNotEmpty()) {
                Text(
                    "${results.size} plan${if (results.size == 1) "" else "s"} found",
                    style = MaterialTheme.typography.labelMedium,
                    color = Grey500,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                )
            }
        }
        items(results) { plan ->
            PlanResultCard(plan = plan, onClick = { onPlanSelected(plan.spNumber) })
            Divider(modifier = Modifier.padding(horizontal = 16.dp), color = Navy100)
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun PlanResultCard(plan: StrataPlan, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    plan.spNumber,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Navy900,
                    ),
                )
                if (plan.hasActiveWorkOrders) {
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(ErrorRed.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text(
                            "⚠ Orders",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = ErrorRed,
                                fontWeight = FontWeight.SemiBold,
                            ),
                        )
                    }
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(
                "${plan.address}, ${plan.state} ${plan.postcode}",
                style = MaterialTheme.typography.bodyMedium,
                color = Grey500,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "${plan.lotCount} lots · ${plan.managingAgent.ifEmpty { "No agent on record" }}",
                style = MaterialTheme.typography.bodySmall,
                color = Grey500,
            )
        }
        Text("›", style = MaterialTheme.typography.titleLarge, color = Grey500)
    }
}

@Composable
private fun SearchHint(state: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("🏢", style = MaterialTheme.typography.displaySmall)
        Spacer(Modifier.height(16.dp))
        Text(
            "Search by SP number or suburb",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, color = Navy800),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            if (state == "NSW") "Try \"SP83985\" or \"Mascot\""
            else "Try \"OC7654321\" or \"Southbank\"",
            style = MaterialTheme.typography.bodyMedium,
            color = Grey500,
        )
        Spacer(Modifier.height(20.dp))
        Text(
            "Data sourced from NSW Strata Hub and NSW Building Commission",
            style = MaterialTheme.typography.labelSmall,
            color = Grey500,
        )
    }
}

@Composable
private fun NoResults(query: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("🔍", style = MaterialTheme.typography.displaySmall)
        Spacer(Modifier.height(16.dp))
        Text(
            "No plans found for \"$query\"",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, color = Navy800),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Check the SP number format (e.g. SP83985) or try a different suburb name.",
            style = MaterialTheme.typography.bodyMedium,
            color = Grey500,
        )
    }
}

@Composable
private fun LoadingHint(message: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Navy800, strokeWidth = 2.dp)
            Spacer(Modifier.height(12.dp))
            Text(message, style = MaterialTheme.typography.bodyMedium, color = Grey500)
        }
    }
}

@Composable
private fun ErrorMessage(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("⚠️", style = MaterialTheme.typography.displaySmall)
        Spacer(Modifier.height(12.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = ErrorRed)
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .background(Navy900, RoundedCornerShape(8.dp))
                .clickable(onClick = onRetry)
                .padding(horizontal = 20.dp, vertical = 10.dp),
        ) {
            Text("Retry", style = MaterialTheme.typography.labelLarge, color = Color.White)
        }
    }
}
