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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.strataguard.app.data.strata.StrataPlan
import com.strataguard.app.data.strata.hasActiveWorkOrders
import com.strataguard.app.platform.LocationResult
import com.strataguard.app.platform.rememberLocationProvider
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

    val locationProvider = rememberLocationProvider { result ->
        when (result) {
            is LocationResult.Success -> viewModel.searchNearby(result.location.latitude, result.location.longitude)
            is LocationResult.PermissionDenied -> viewModel.onLocationPermissionDenied()
            is LocationResult.Unavailable -> viewModel.onLocationUnavailable()
        }
    }

    if (state.showAddBuilding) {
        AddBuildingSheet(
            state = state.selectedState,
            planNumber = state.addPlanNumber,
            address = state.addAddress,
            suburb = state.addSuburb,
            postcode = state.addPostcode,
            lotCount = state.addLotCount,
            agent = state.addAgent,
            isSaving = state.isSavingPlan,
            error = state.addError,
            onPlanNumber = viewModel::onAddPlanNumber,
            onAddress = viewModel::onAddAddress,
            onSuburb = viewModel::onAddSuburb,
            onPostcode = viewModel::onAddPostcode,
            onLotCount = viewModel::onAddLotCount,
            onAgent = viewModel::onAddAgent,
            onSubmit = { viewModel.submitAddBuilding(onPlanSelected) },
            onDismiss = viewModel::dismissAddBuilding,
        )
    }

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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
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
            // State toggle + Near Me
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Navy900)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (state.isNearbyMode) {
                    // Near Me active — show exit pill
                    Box(
                        modifier = Modifier
                            .background(Amber500, RoundedCornerShape(20.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("📍 Near Me", style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold, color = Navy900))
                    }
                    Spacer(Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                            .clickable { viewModel.exitNearbyMode() }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                    ) {
                        Text("✕ Exit", style = MaterialTheme.typography.labelLarge.copy(color = Color.White))
                    }
                } else {
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
                    Spacer(Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                            .clickable { locationProvider.requestLocation() }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (state.isLoading && !state.isNearbyMode) {
                            CircularProgressIndicator(Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("📍 Near Me", style = MaterialTheme.typography.labelLarge.copy(color = Color.White))
                        }
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

            HorizontalDivider(color = Navy100)

            Box(Modifier.weight(1f)) {
                when {
                    state.isSeeding -> LoadingHint("Setting up data…")
                    state.isLoading && state.isNearbyMode -> LoadingHint("Finding plans near you…")
                    state.error != null -> ErrorMessage(state.error!!, onRetry = viewModel::retry)
                    state.isNearbyMode && state.hasSearched && state.results.isEmpty() ->
                        NearbyEmpty(onExit = viewModel::exitNearbyMode)
                    state.isNearbyMode && state.results.isNotEmpty() ->
                        ResultsList(
                            results = state.results,
                            onPlanSelected = onPlanSelected,
                            distances = state.nearbyDistances,
                        )
                    !state.isNearbyMode && state.query.length < 2 && !state.hasSearched ->
                        SearchHint(state.selectedState, onNearMe = { locationProvider.requestLocation() })
                    state.hasSearched && state.results.isEmpty() && !state.isLoading -> NoResults(
                        query = state.query,
                        onAddBuilding = viewModel::showAddBuilding,
                    )
                    else -> ResultsList(results = state.results, onPlanSelected = onPlanSelected)
                }

                state.locationError?.let { err ->
                    Snackbar(
                        modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                        action = {
                            TextButton(onClick = { viewModel.exitNearbyMode() }) { Text("OK") }
                        },
                    ) { Text(err) }
                }
            }
        }
    }
}

@Composable
private fun ResultsList(
    results: List<StrataPlan>,
    onPlanSelected: (String) -> Unit,
    distances: Map<String, Double> = emptyMap(),
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            if (results.isNotEmpty()) {
                val label = if (distances.isNotEmpty())
                    "${results.size} plan${if (results.size == 1) "" else "s"} within 10 km"
                else
                    "${results.size} plan${if (results.size == 1) "" else "s"} found"
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium,
                    color = Grey500,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                )
            }
        }
        items(results) { plan ->
            PlanResultCard(
                plan = plan,
                onClick = { onPlanSelected(plan.spNumber) },
                distanceKm = distances[plan.spNumber],
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Navy100)
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun PlanResultCard(plan: StrataPlan, onClick: () -> Unit, distanceKm: Double? = null) {
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
                distanceKm?.let {
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(Navy100, RoundedCornerShape(20.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        val wholeKm = it.toInt()
                        val dec = ((it - wholeKm) * 10).toInt()
                        val label = if (it < 1.0) "${(it * 1000).toInt()} m" else "$wholeKm.$dec km"
                        Text(
                            "📍 $label",
                            style = MaterialTheme.typography.labelSmall.copy(color = Navy800),
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
private fun SearchHint(state: String, onNearMe: () -> Unit) {
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
        Button(
            onClick = onNearMe,
            colors = ButtonDefaults.buttonColors(containerColor = Navy900),
            shape = RoundedCornerShape(10.dp),
        ) {
            Text("📍  Find Plans Near Me", color = Color.White, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(20.dp))
        Text(
            "Data sourced from NSW Strata Hub and NSW Building Commission",
            style = MaterialTheme.typography.labelSmall,
            color = Grey500,
        )
    }
}

@Composable
private fun NearbyEmpty(onExit: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("📍", style = MaterialTheme.typography.displaySmall)
        Spacer(Modifier.height(16.dp))
        Text(
            "No plans found nearby",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, color = Navy800),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "We couldn't find any strata plans within 10 km. Try searching by plan number or suburb.",
            style = MaterialTheme.typography.bodyMedium,
            color = Grey500,
        )
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onExit,
            colors = ButtonDefaults.buttonColors(containerColor = Navy900),
            shape = RoundedCornerShape(10.dp),
        ) {
            Text("Search by Name", color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun NoResults(query: String, onAddBuilding: () -> Unit) {
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
            "This plan isn't in our database yet.",
            style = MaterialTheme.typography.bodyMedium,
            color = Grey500,
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onAddBuilding,
            colors = ButtonDefaults.buttonColors(containerColor = Navy900),
            shape = RoundedCornerShape(10.dp),
        ) {
            Text("Add This Building", color = Color.White, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "Help other residents by contributing your building's data.",
            style = MaterialTheme.typography.labelSmall,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddBuildingSheet(
    state: String,
    planNumber: String,
    address: String,
    suburb: String,
    postcode: String,
    lotCount: String,
    agent: String,
    isSaving: Boolean,
    error: String?,
    onPlanNumber: (String) -> Unit,
    onAddress: (String) -> Unit,
    onSuburb: (String) -> Unit,
    onPostcode: (String) -> Unit,
    onLotCount: (String) -> Unit,
    onAgent: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Add This Building", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text(
                "Help other residents by contributing your building's data. It will be available to the StrataGuard community.",
                style = MaterialTheme.typography.bodySmall,
                color = Grey500,
            )

            val prefix = if (state == "NSW") "SP" else "OC"
            OutlinedTextField(
                value = planNumber,
                onValueChange = onPlanNumber,
                label = { Text("Plan Number (e.g. ${prefix}12345) *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
            )

            OutlinedTextField(
                value = address,
                onValueChange = onAddress,
                label = { Text("Street Address *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = suburb,
                    onValueChange = onSuburb,
                    label = { Text("Suburb *") },
                    modifier = Modifier.weight(2f),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                )
                OutlinedTextField(
                    value = postcode,
                    onValueChange = onPostcode,
                    label = { Text("Postcode") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(10.dp),
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = lotCount,
                    onValueChange = onLotCount,
                    label = { Text("Total Lots") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(10.dp),
                )
                OutlinedTextField(
                    value = agent,
                    onValueChange = onAgent,
                    label = { Text("Managing Agent") },
                    modifier = Modifier.weight(2f),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                )
            }

            error?.let {
                Text(it, color = ErrorRed, fontSize = 13.sp)
            }

            Button(
                onClick = onSubmit,
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Navy900),
                shape = RoundedCornerShape(10.dp),
            ) {
                if (isSaving) {
                    CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Saving...", color = Color.White)
                } else {
                    Text("Add Building", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                elevation = null,
            ) {
                Text("Cancel", color = Grey500)
            }
        }
    }
}
