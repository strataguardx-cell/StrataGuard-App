package com.strataguard.app.ui.dispute

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.strataguard.app.data.dispute.Dispute
import com.strataguard.app.data.dispute.DisputeType
import com.strataguard.app.data.dispute.AustralianState
import com.strataguard.app.data.dispute.verdictLabel
import com.strataguard.app.ui.theme.Amber500
import com.strataguard.app.ui.theme.ErrorRed
import com.strataguard.app.ui.theme.Navy800
import com.strataguard.app.ui.theme.Navy900
import com.strataguard.app.ui.theme.SuccessGreen
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisputeListScreen(onNavigateBack: () -> Unit) {
    val vm: DisputeViewModel = koinViewModel()
    val state by vm.uiState.collectAsState()
    var paywallDispute by remember { mutableStateOf<Dispute?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dispute Risk Check", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy900),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { vm.showCreateSheet() },
                containerColor = Amber500,
                contentColor = Color.White,
            ) { Icon(Icons.Default.Add, contentDescription = "New dispute") }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.disputes.isEmpty() -> DisputeEmptyState(onNew = { vm.showCreateSheet() })
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        Text(
                            "${state.disputes.size} dispute${if (state.disputes.size != 1) "s" else ""}",
                            color = Color.Gray, fontSize = 13.sp,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                    }
                    items(state.disputes, key = { it.id }) { dispute ->
                        DisputeCard(
                            dispute = dispute,
                            onAssess = { vm.runAssessment(dispute.id) },
                            onExport = { paywallDispute = dispute },
                            onDelete = { vm.deleteDispute(dispute.id) },
                            isAssessing = state.assessingDisputeId == dispute.id,
                            isExporting = state.exportingDisputeId == dispute.id,
                        )
                    }
                }
            }
            state.error?.let { err ->
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                    action = { TextButton(onClick = { vm.clearError() }) { Text("Dismiss") } },
                ) { Text(err) }
            }
        }
    }

    if (state.showCreateSheet) {
        CreateDisputeSheet(
            selectedState = state.selectedState,
            selectedType = state.selectedType,
            filingDeadline = state.filingDeadline,
            notes = state.notes,
            isSaving = state.isSaving,
            onStateSelected = vm::onStateSelected,
            onTypeSelected = vm::onTypeSelected,
            onDeadlineChanged = vm::onFilingDeadlineChanged,
            onNotesChanged = vm::onNotesChanged,
            onSave = { vm.createDispute() },
            onDismiss = vm::dismissCreateSheet,
        )
    }

    paywallDispute?.let { dispute ->
        PaywallSheet(
            dispute = dispute,
            isExporting = state.exportingDisputeId == dispute.id,
            onConfirmPurchase = {
                vm.exportPdf(dispute)
                paywallDispute = null
            },
            onDismiss = { paywallDispute = null },
        )
    }
}

@Composable
private fun DisputeEmptyState(onNew: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("⚖️", fontSize = 56.sp)
        Spacer(Modifier.height(16.dp))
        Text("No disputes yet", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            "Create a dispute case to get an AI risk assessment and generate a tribunal-ready evidence pack.",
            color = Color.Gray, fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onNew,
            colors = ButtonDefaults.buttonColors(containerColor = Amber500),
        ) { Text("Start a Dispute Case", color = Color.White) }
    }
}

@Composable
private fun DisputeCard(
    dispute: Dispute,
    onAssess: () -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit,
    isAssessing: Boolean,
    isExporting: Boolean,
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        DisputeType.entries.find { it.name == dispute.disputeType }?.displayName ?: dispute.disputeType,
                        fontWeight = FontWeight.Bold, fontSize = 15.sp,
                    )
                    Text("${dispute.tribunal}  •  ${dispute.state}", color = Color.Gray, fontSize = 12.sp)
                }
                if (dispute.riskVerdict.isNotBlank()) {
                    VerdictChip(dispute.riskVerdict, dispute.riskScore)
                }
            }

            if (dispute.strataAddress.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(dispute.strataAddress, fontSize = 12.sp, color = Color.Gray)
            }

            if (dispute.filingDeadline.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text("Filing deadline: ${dispute.filingDeadline}", fontSize = 12.sp, color = ErrorRed)
            }

            Spacer(Modifier.height(12.dp))

            // Primary action row: Assess + Export
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onAssess,
                    enabled = !isAssessing && !isExporting,
                    modifier = Modifier.weight(1f),
                ) {
                    if (isAssessing) {
                        CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Assess Risk", fontSize = 13.sp)
                    }
                }
                // Export enabled only after a risk assessment has been run
                Button(
                    onClick = onExport,
                    enabled = dispute.riskVerdict.isNotBlank() && !isExporting && !isAssessing,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Amber500),
                ) {
                    if (isExporting) {
                        CircularProgressIndicator(Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Export PDF", fontSize = 13.sp, color = Color.White)
                    }
                }
            }

            Spacer(Modifier.height(4.dp))
            TextButton(
                onClick = onDelete,
                modifier = Modifier.align(Alignment.End),
            ) {
                Text("Delete", fontSize = 12.sp, color = ErrorRed)
            }
        }
    }
}

@Composable
private fun VerdictChip(verdict: String, score: Float) {
    val (bg, label) = when (verdict) {
        "STRONG" -> SuccessGreen to "STRONG  ${(score * 100).toInt()}%"
        "MODERATE" -> Amber500 to "MODERATE  ${(score * 100).toInt()}%"
        else -> ErrorRed to "WEAK  ${(score * 100).toInt()}%"
    }
    Surface(color = bg, shape = RoundedCornerShape(16.dp)) {
        Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaywallSheet(
    dispute: Dispute,
    isExporting: Boolean,
    onConfirmPurchase: () -> Unit,
    onDismiss: () -> Unit,
) {
    var purchased by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (purchased) {
                // Success state
                Spacer(Modifier.height(8.dp))
                Text("🎉", fontSize = 48.sp)
                Text("Evidence Pack Generating!", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(
                    "Your tribunal-ready PDF is being prepared. It will appear in your downloads shortly.",
                    color = Color.Gray, fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                ) { Text("Done", color = Color.White, fontWeight = FontWeight.Bold) }
            } else {
                // Paywall state
                Spacer(Modifier.height(8.dp))
                Surface(
                    color = Amber500.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        "EVIDENCE PACK",
                        color = Amber500, fontWeight = FontWeight.Bold, fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }

                Text(
                    "$49",
                    fontWeight = FontWeight.Bold, fontSize = 48.sp, color = Navy800,
                )
                Text("one-time · per dispute", color = Color.Gray, fontSize = 13.sp)

                Spacer(Modifier.height(4.dp))

                // Feature list
                val features = listOf(
                    "📄  Tribunal-ready PDF formatted for ${dispute.tribunal}",
                    "📸  All your evidence photos with timestamps",
                    "⚖️  Risk assessment with score breakdown",
                    "📋  Chronological incident timeline",
                    "✅  AI-authenticity verification on every photo",
                )
                Column(
                    Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    features.forEach { feature ->
                        Text(feature, fontSize = 14.sp)
                    }
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = {
                        purchased = true
                        onConfirmPurchase()
                    },
                    enabled = !isExporting,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    if (isExporting) {
                        CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Generating...", color = Color.White, fontWeight = FontWeight.Bold)
                    } else {
                        Text("Get My Evidence Pack — $49", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Not now", color = Color.Gray)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateDisputeSheet(
    selectedState: String,
    selectedType: String,
    filingDeadline: String,
    notes: String,
    isSaving: Boolean,
    onStateSelected: (String) -> Unit,
    onTypeSelected: (String) -> Unit,
    onDeadlineChanged: (String) -> Unit,
    onNotesChanged: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("New Dispute Case", fontWeight = FontWeight.Bold, fontSize = 18.sp)

            // State selector
            Text("State / Territory", fontWeight = FontWeight.Medium, fontSize = 13.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("NSW", "VIC", "QLD", "WA", "SA").forEach { s ->
                    FilterChip(
                        selected = selectedState == s,
                        onClick = { onStateSelected(s) },
                        label = { Text(s) },
                    )
                }
            }

            // Dispute type selector
            Text("Dispute Type", fontWeight = FontWeight.Medium, fontSize = 13.sp)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                DisputeType.entries.forEach { type ->
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = selectedType == type.name,
                            onClick = { onTypeSelected(type.name) },
                        )
                        Column {
                            Text(type.displayName, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text(type.description, fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }

            OutlinedTextField(
                value = filingDeadline,
                onValueChange = onDeadlineChanged,
                label = { Text("Filing Deadline (YYYY-MM-DD, optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChanged,
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth().height(100.dp),
            )

            Button(
                onClick = onSave,
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Navy800),
            ) {
                if (isSaving) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                else Text("Create Dispute Case", color = Color.White)
            }

            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel", color = Color.Gray)
            }
        }
    }
}
