package com.strataguard.app.ui.strata

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.strataguard.app.data.strata.RiskFlag
import com.strataguard.app.data.strata.SinkingFundStatus
import com.strataguard.app.data.strata.StrataPlan
import com.strataguard.app.data.strata.StrataDocument
import com.strataguard.app.data.strata.WorkOrder
import com.strataguard.app.data.strata.WorkOrderType
import com.strataguard.app.data.strata.displaySinkingFundStatus
import com.strataguard.app.data.strata.displayType
import com.strataguard.app.data.strata.severityColor
import com.strataguard.app.platform.rememberDocumentPickerHandler
import com.strataguard.app.ui.theme.Amber100
import com.strataguard.app.ui.theme.Amber500
import com.strataguard.app.ui.theme.ErrorLight
import com.strataguard.app.ui.theme.ErrorRed
import com.strataguard.app.ui.theme.Grey500
import com.strataguard.app.ui.theme.Navy100
import com.strataguard.app.ui.theme.Navy50
import com.strataguard.app.ui.theme.Navy800
import com.strataguard.app.ui.theme.Navy900
import com.strataguard.app.ui.theme.SuccessGreen
import com.strataguard.app.ui.theme.SuccessLight
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StrataPlanDetailScreen(
    spNumber: String,
    onNavigateBack: () -> Unit,
    viewModel: StrataPlanDetailViewModel = koinViewModel(parameters = { parametersOf(spNumber) }),
) {
    val state by viewModel.uiState.collectAsState()

    val docPicker = rememberDocumentPickerHandler { bytes, filename ->
        viewModel.uploadDocument(bytes, filename)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        spNumber,
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
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Navy800,
                )

                state.error != null -> Text(
                    state.error!!,
                    color = ErrorRed,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.Center),
                )

                state.plan != null -> PlanDetailContent(
                    plan = state.plan!!,
                    documents = state.documents,
                    isUploadingDocument = state.isUploadingDocument,
                    onUploadDocument = { docPicker.pickPdf() },
                )
            }

            state.documentError?.let { err ->
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                    action = { TextButton(onClick = viewModel::clearDocumentError) { Text("Dismiss") } },
                ) { Text(err) }
            }
        }
    }
}

@Composable
private fun PlanDetailContent(
    plan: StrataPlan,
    documents: List<StrataDocument>,
    isUploadingDocument: Boolean,
    onUploadDocument: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        // Header card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Navy900)
                .padding(20.dp),
        ) {
            Text(
                plan.spNumber,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                ),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "${plan.address}, ${plan.state} ${plan.postcode}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.8f),
                ),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Registered ${plan.registrationDate}",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.White.copy(alpha = 0.6f),
                ),
            )
        }

        Spacer(Modifier.height(16.dp))

        // Key stats
        Section(title = "Building Info") {
            StatGrid(
                listOf(
                    "Lots" to plan.lotCount.toString(),
                    "Class" to plan.buildingClass,
                    "Last AGM" to plan.lastAGM.ifEmpty { "Not reported" },
                    "State" to plan.state,
                ),
            )
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Navy100)

        // Managing agent
        Section(title = "Managing Agent") {
            if (plan.managingAgent.isEmpty()) {
                Text("No managing agent on record", style = MaterialTheme.typography.bodyMedium, color = Grey500)
            } else {
                Text(
                    plan.managingAgent,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Navy800,
                    ),
                )
                if (plan.managingAgentLicence.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Lic: ${plan.managingAgentLicence}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Grey500,
                        )
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(SuccessLight, RoundedCornerShape(20.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                        ) {
                            Text(
                                "✓ Verified",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = SuccessGreen,
                                    fontWeight = FontWeight.SemiBold,
                                ),
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Navy100)

        // Work orders
        Section(
            title = if (plan.workOrders.isEmpty()) "Work Orders" else "⚠️ Active Work Orders (${plan.workOrders.size})",
        ) {
            if (plan.workOrders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SuccessLight, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                ) {
                    Text(
                        "✓ No active building work orders on record",
                        style = MaterialTheme.typography.bodyMedium.copy(color = SuccessGreen),
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    plan.workOrders.forEach { order ->
                        WorkOrderCard(order)
                    }
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Navy100)

        // Sinking fund
        Section(title = "Sinking Fund (Capital Works)") {
            SinkingFundChip(plan.displaySinkingFundStatus)
            if (plan.displaySinkingFundStatus == SinkingFundStatus.UNKNOWN) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Levy and fund balances are private. Contact your strata manager or obtain a Section 184 Certificate for details.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Grey500,
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Navy100)

        // Strata documents + OCR risk flags
        Section(title = "Strata Documents") {
            Button(
                onClick = onUploadDocument,
                enabled = !isUploadingDocument,
                colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (isUploadingDocument) {
                    CircularProgressIndicator(Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Analysing document…", color = Color.White)
                } else {
                    Text("Upload Strata Document (PDF)", color = Color.White)
                }
            }
            if (documents.isEmpty() && !isUploadingDocument) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Upload strata minutes, AGM reports or defect notices. StrataGuard will automatically scan for risk flags like water ingress, combustible cladding, and building defects.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Grey500,
                )
            }
            if (documents.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    documents.forEach { doc -> StrataDocumentCard(doc) }
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Navy100)

        // Data attribution
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Navy50)
                .padding(16.dp),
        ) {
            Text(
                "Data sources",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Navy800,
                ),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Scheme details: NSW Strata Hub (nsw.gov.au/strata)\nWork orders: NSW Building Commission\nManaging agent: NSW Fair Trading licence register\nSinking fund status is indicative only.",
                style = MaterialTheme.typography.labelSmall,
                color = Grey500,
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun WorkOrderCard(order: WorkOrder) {
    val typeColor = when (order.displayType) {
        WorkOrderType.STOP_WORK -> ErrorRed
        WorkOrderType.PROHIBITION -> ErrorRed
        WorkOrderType.RECTIFICATION -> Amber500
        WorkOrderType.UNKNOWN -> Grey500
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ErrorLight),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(typeColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                ) {
                    Text(
                        order.displayType.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = typeColor,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    order.issueDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = Grey500,
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                order.description,
                style = MaterialTheme.typography.bodySmall,
                color = Navy800,
            )
        }
    }
}

@Composable
private fun SinkingFundChip(status: SinkingFundStatus) {
    val (label, color, bg) = when (status) {
        SinkingFundStatus.ADEQUATE -> Triple("✓ Adequate", SuccessGreen, SuccessLight)
        SinkingFundStatus.LOW -> Triple("⚠ Low", Amber500, Amber100)
        SinkingFundStatus.CRITICAL -> Triple("✕ Critical", ErrorRed, ErrorLight)
        SinkingFundStatus.UNKNOWN -> Triple("— Unknown", Grey500, Navy50)
    }
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = color,
            ),
        )
    }
}

@Composable
private fun StatGrid(items: List<Pair<String, String>>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { (label, value) ->
                    Column(modifier = Modifier.weight(1f)) {
                        Text(label, style = MaterialTheme.typography.labelSmall, color = Grey500)
                        Spacer(Modifier.height(2.dp))
                        Text(
                            value,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Navy800,
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StrataDocumentCard(doc: StrataDocument) {
    var expanded by remember { mutableStateOf(doc.riskFlags.isNotEmpty()) }
    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        doc.title ?: doc.originalFilename ?: "Document",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Navy800,
                    )
                    Text(
                        doc.originalFilename ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = Grey500,
                    )
                }
                OcrStatusChip(doc.ocrStatus)
            }

            if (doc.riskFlags.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "${doc.riskFlags.size} risk flag${if (doc.riskFlags.size != 1) "s" else ""} detected",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = ErrorRed,
                    )
                    TextButton(onClick = { expanded = !expanded }) {
                        Text(if (expanded) "Hide" else "Show", fontSize = 12.sp)
                    }
                }
                if (expanded) {
                    Spacer(Modifier.height(4.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        doc.riskFlags.forEach { flag -> RiskFlagRow(flag) }
                    }
                }
            } else if (doc.ocrStatus == "completed") {
                Spacer(Modifier.height(6.dp))
                Text(
                    "No risk flags found in this document.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SuccessGreen,
                )
            }
        }
    }
}

@Composable
private fun OcrStatusChip(status: String) {
    val (label, color, bg) = when (status) {
        "completed" -> Triple("Analysed", SuccessGreen, SuccessLight)
        "processing" -> Triple("Processing…", Amber500, Amber100)
        "failed" -> Triple("Failed", ErrorRed, ErrorLight)
        else -> Triple("Pending", Grey500, Navy50)
    }
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(20.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall.copy(color = color, fontWeight = FontWeight.SemiBold))
    }
}

@Composable
private fun RiskFlagRow(flag: RiskFlag) {
    val color = Color(flag.severityColor)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .background(color, RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp),
        ) {
            Text(
                flag.severity,
                style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold),
            )
        }
        Spacer(Modifier.width(8.dp))
        Column {
            Text(flag.label, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, color = Navy800))
            if (flag.context.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(flag.context, style = MaterialTheme.typography.labelSmall, color = Grey500)
            }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Navy800,
            ),
        )
        Spacer(Modifier.height(10.dp))
        content()
    }
}
