package com.strataguard.app.ui.dispute

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import org.koin.compose.viewmodel.koinViewModel

private val NavyBlue = Color(0xFF1B2A4A)
private val Amber = Color(0xFFE8A020)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisputeListScreen(onNavigateBack: () -> Unit) {
    val vm: DisputeViewModel = koinViewModel()
    val state by vm.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dispute Risk Check", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text("←", color = Color.White, fontSize = 20.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyBlue),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { vm.showCreateSheet() },
                containerColor = Amber,
                contentColor = Color.White,
            ) { Text("+", fontSize = 24.sp) }
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
                            onDelete = { vm.deleteDispute(dispute.id) },
                            isAssessing = state.isAssessing,
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
            colors = ButtonDefaults.buttonColors(containerColor = Amber),
        ) { Text("Start a Dispute Case", color = Color.White) }
    }
}

@Composable
private fun DisputeCard(
    dispute: Dispute,
    onAssess: () -> Unit,
    onDelete: () -> Unit,
    isAssessing: Boolean,
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
                Text("Filing deadline: ${dispute.filingDeadline}", fontSize = 12.sp, color = Color(0xFFCC4400))
            }

            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onAssess,
                    enabled = !isAssessing,
                    modifier = Modifier.weight(1f),
                ) {
                    if (isAssessing) {
                        CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Assess Risk", fontSize = 13.sp)
                    }
                }
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFCC4400)),
                ) { Text("Delete", fontSize = 13.sp) }
            }
        }
    }
}

@Composable
private fun VerdictChip(verdict: String, score: Float) {
    val (bg, label) = when (verdict) {
        "STRONG" -> Color(0xFF2DA05A) to "STRONG  ${(score * 100).toInt()}%"
        "MODERATE" -> Amber to "MODERATE  ${(score * 100).toInt()}%"
        else -> Color(0xFFDC3545) to "WEAK  ${(score * 100).toInt()}%"
    }
    Surface(color = bg, shape = RoundedCornerShape(16.dp)) {
        Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
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
                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
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
