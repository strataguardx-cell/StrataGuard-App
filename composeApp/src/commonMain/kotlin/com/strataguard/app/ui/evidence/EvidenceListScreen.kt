package com.strataguard.app.ui.evidence

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.strataguard.app.data.evidence.AiFlag
import com.strataguard.app.data.evidence.AiVerdict
import com.strataguard.app.data.evidence.EvidenceItem
import com.strataguard.app.data.evidence.flags
import com.strataguard.app.data.evidence.verdict
import com.strataguard.app.platform.ExifAnalysisResult
import com.strataguard.app.platform.ImagePickerHandler
import com.strataguard.app.platform.rememberImagePickerHandler
import com.strataguard.app.ui.components.ErrorBanner
import com.strataguard.app.ui.theme.Amber100
import com.strataguard.app.ui.theme.Amber500
import com.strataguard.app.ui.theme.ErrorLight
import com.strataguard.app.ui.theme.ErrorRed
import com.strataguard.app.ui.theme.Grey300
import com.strataguard.app.ui.theme.Grey400
import com.strataguard.app.ui.theme.Grey500
import com.strataguard.app.ui.theme.Navy800
import com.strataguard.app.ui.theme.Navy900
import com.strataguard.app.ui.theme.SuccessGreen
import com.strataguard.app.ui.theme.SuccessLight
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvidenceListScreen(
    onNavigateBack: () -> Unit,
    viewModel: EvidenceViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showSheet by remember { mutableStateOf(false) }
    var showCaptureOptions by remember { mutableStateOf(false) }

    val picker = rememberImagePickerHandler { bytes, isFromCamera ->
        viewModel.onImageSelected(bytes, isFromCamera)
        showSheet = true
        scope.launch { sheetState.expand() }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Document Evidence",
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
        floatingActionButton = {
            if (state.items.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { showCaptureOptions = true },
                    containerColor = Navy800,
                    contentColor = Color.White,
                ) {
                    Text("+", style = MaterialTheme.typography.headlineSmall)
                }
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Navy800)
                }

                state.items.isEmpty() -> EmptyEvidenceState(picker = picker)

                else -> EvidenceTimeline(
                    items = state.items,
                    onDelete = { viewModel.deleteEvidence(it) },
                )
            }

            if (state.error != null) {
                ErrorBanner(
                    message = state.error!!,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                )
            }
        }
    }

    if (showCaptureOptions) {
        ModalBottomSheet(
            onDismissRequest = { showCaptureOptions = false },
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
            ) {
                Text(
                    "Add Evidence",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Navy800,
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { showCaptureOptions = false; picker.captureFromCamera() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text("📷  Camera", style = MaterialTheme.typography.labelLarge)
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { showCaptureOptions = false; picker.pickFromGallery() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text("Upload from Gallery", style = MaterialTheme.typography.labelLarge, color = Navy800)
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showSheet = false
                viewModel.dismissPending()
            },
            sheetState = sheetState,
        ) {
            AddEvidenceSheet(
                state = state,
                onSave = { title, desc ->
                    viewModel.saveEvidence(title, desc)
                    scope.launch {
                        sheetState.hide()
                        showSheet = false
                    }
                },
                onDismiss = {
                    scope.launch {
                        sheetState.hide()
                        showSheet = false
                        viewModel.dismissPending()
                    }
                },
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Empty state
// ---------------------------------------------------------------------------

@Composable
private fun EmptyEvidenceState(picker: ImagePickerHandler) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("📸", style = MaterialTheme.typography.displayMedium)
        Spacer(Modifier.height(16.dp))
        Text(
            "No evidence yet",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = Navy800,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Capture timestamped evidence using your camera or upload from your gallery. Every item is checked for AI generation.",
            style = MaterialTheme.typography.bodyMedium,
            color = Grey500,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(32.dp))

        Button(
            onClick = { picker.captureFromCamera() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Navy800),
            shape = RoundedCornerShape(10.dp),
        ) {
            Text("📷  Capture with Camera", style = MaterialTheme.typography.labelLarge)
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = { picker.pickFromGallery() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Navy800.copy(alpha = 0.12f)),
            shape = RoundedCornerShape(10.dp),
        ) {
            Text("Upload from Gallery", style = MaterialTheme.typography.labelLarge, color = Navy800)
        }

        Spacer(Modifier.height(16.dp))
        Text(
            "Camera captures are auto-verified. Gallery uploads are checked for AI generation.",
            style = MaterialTheme.typography.labelSmall,
            color = Grey400,
            textAlign = TextAlign.Center,
        )
    }
}

// ---------------------------------------------------------------------------
// Timeline list
// ---------------------------------------------------------------------------

@Composable
private fun EvidenceTimeline(items: List<EvidenceItem>, onDelete: (String) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Box(modifier = Modifier.fillMaxWidth().background(Navy900).padding(16.dp)) {
                Text(
                    "${items.size} item${if (items.size != 1) "s" else ""}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.7f),
                )
            }
        }
        items(items, key = { it.id }) { item ->
            EvidenceCard(item = item, onDelete = { onDelete(item.id) })
            HorizontalDivider(color = Grey300.copy(alpha = 0.5f))
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

@OptIn(ExperimentalEncodingApi::class)
@Composable
private fun EvidenceCard(item: EvidenceItem, onDelete: () -> Unit) {
    val verdict = item.verdict
    var confirmDelete by remember { mutableStateOf(false) }

    val thumbnailBytes: ByteArray? = remember(item.thumbnail) {
        if (item.thumbnail.isEmpty()) null
        else runCatching { Base64.decode(item.thumbnail) }.getOrNull()
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Grey300),
        ) {
            if (thumbnailBytes != null) {
                AsyncImage(
                    model = thumbnailBytes,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("📷", style = MaterialTheme.typography.titleLarge)
                }
            }
            VerdictBadge(
                verdict = verdict,
                compact = true,
                modifier = Modifier.align(Alignment.BottomStart).padding(4.dp),
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Navy800,
            )
            Spacer(Modifier.height(2.dp))
            Text(formatTimestamp(item.capturedAt), style = MaterialTheme.typography.labelSmall, color = Grey500)
            if (item.description.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(item.description, style = MaterialTheme.typography.bodySmall, color = Grey500, maxLines = 2)
            }
        }

        if (!confirmDelete) {
            TextButton(onClick = { confirmDelete = true }) {
                Text("Delete", style = MaterialTheme.typography.labelSmall, color = ErrorRed)
            }
        } else {
            TextButton(onClick = { confirmDelete = false; onDelete() }) {
                Text("Confirm", style = MaterialTheme.typography.labelSmall, color = ErrorRed)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Add Evidence bottom sheet
// ---------------------------------------------------------------------------

@Composable
private fun AddEvidenceSheet(
    state: EvidenceUiState,
    onSave: (title: String, description: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp),
    ) {
        Text(
            "Add Evidence",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = Navy800,
        )
        Spacer(Modifier.height(16.dp))

        when {
            state.isAnalyzing -> Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Navy800, strokeWidth = 2.dp)
                Spacer(Modifier.width(12.dp))
                Text("Checking for AI generation…", style = MaterialTheme.typography.bodyMedium, color = Grey500)
            }

            state.pendingBytes != null && state.pendingAnalysis != null -> {
                ImagePreviewAndVerdict(bytes = state.pendingBytes, analysis = state.pendingAnalysis)
                Spacer(Modifier.height(16.dp))
            }
        }

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            placeholder = { Text("e.g. Water stain on ceiling", color = Grey400) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Navy800,
                focusedLabelColor = Navy800,
                unfocusedBorderColor = Grey300,
            ),
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description (optional)") },
            placeholder = { Text("Add any relevant notes…", color = Grey400) },
            modifier = Modifier.fillMaxWidth().height(100.dp),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Navy800,
                focusedLabelColor = Navy800,
                unfocusedBorderColor = Grey300,
            ),
        )

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = { onSave(title, description) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            enabled = title.isNotBlank() && !state.isSaving && state.pendingAnalysis != null,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Navy800),
        ) {
            if (state.isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text(
                    "Save Evidence",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
            Text("Cancel", color = Grey500)
        }
    }
}

@Composable
private fun ImagePreviewAndVerdict(bytes: ByteArray, analysis: ExifAnalysisResult) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Grey300),
    ) {
        AsyncImage(
            model = bytes,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        VerdictBadge(
            verdict = analysis.verdict,
            compact = false,
            modifier = Modifier.align(Alignment.BottomStart).padding(10.dp),
        )
    }

    if (analysis.flags.isNotEmpty()) {
        Spacer(Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            analysis.flags.forEach { flag ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("•", color = Amber500, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.width(6.dp))
                    Text(flag.displayName, style = MaterialTheme.typography.bodySmall, color = Grey500)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Verdict badge
// ---------------------------------------------------------------------------

@Composable
private fun VerdictBadge(verdict: AiVerdict, compact: Boolean, modifier: Modifier = Modifier) {
    val (label, color, bg) = when (verdict) {
        AiVerdict.AUTHENTIC -> Triple(if (compact) "✓" else "✓ Verified", SuccessGreen, SuccessLight)
        AiVerdict.SUSPICIOUS -> Triple(if (compact) "⚠" else "⚠ Unverified", Amber500, Amber100)
        AiVerdict.AI_GENERATED -> Triple(if (compact) "✕" else "✕ AI-Generated", ErrorRed, ErrorLight)
        AiVerdict.PENDING -> Triple(if (compact) "…" else "Analyzing…", Grey500, Grey300)
    }
    Box(
        modifier = modifier
            .background(bg.copy(alpha = 0.92f), RoundedCornerShape(4.dp))
            .padding(horizontal = if (compact) 4.dp else 8.dp, vertical = if (compact) 2.dp else 4.dp),
    ) {
        Text(
            label,
            style = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.Bold,
        )
    }
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

private fun formatTimestamp(epochMillis: Long): String = runCatching {
    val local = Instant.fromEpochMilliseconds(epochMillis)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    val month = local.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
    val h = local.hour.toString().padStart(2, '0')
    val m = local.minute.toString().padStart(2, '0')
    "$month ${local.dayOfMonth}, ${local.year}  $h:$m"
}.getOrElse { "Unknown date" }
