package com.strataguard.app.ui.evidence

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.strataguard.app.data.evidence.AiFlag
import com.strataguard.app.data.evidence.AiVerdict
import com.strataguard.app.data.evidence.EvidenceItem
import com.strataguard.app.data.evidence.EvidenceRepository
import com.strataguard.app.data.evidence.FirebaseEvidenceRepository
import com.strataguard.app.data.evidence.SyncStatus
import com.strataguard.app.platform.ExifAnalysisResult
import com.strataguard.app.platform.analyzeImageExif
import com.strataguard.app.platform.dequeuePendingEvidence
import com.strataguard.app.platform.removePendingEvidence
import com.strataguard.app.platform.scheduleEvidenceSync
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class EvidenceUiState(
    val items: List<EvidenceItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val pendingUploadCount: Int = 0,
    // Pending add-evidence flow
    val pendingBytes: ByteArray? = null,
    val pendingAnalysis: ExifAnalysisResult? = null,
    val isAnalyzing: Boolean = false,
    val isSaving: Boolean = false,
)

class EvidenceViewModel(private val repository: EvidenceRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(EvidenceUiState())
    val uiState: StateFlow<EvidenceUiState> = _uiState

    init {
        loadEvidence()
    }

    fun loadEvidence() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // Opportunistically flush queue on every load (covers iOS foreground-retry path)
            tryFlushPendingQueue()

            val items = repository.getEvidence()
            val pendingCount = items.count { it.syncStatus == SyncStatus.PENDING.name }
            _uiState.value = _uiState.value.copy(items = items, isLoading = false, pendingUploadCount = pendingCount)
        }
    }

    private suspend fun tryFlushPendingQueue() {
        val pending = dequeuePendingEvidence()
        if (pending.isEmpty()) return
        val repo = repository as? FirebaseEvidenceRepository ?: return
        for (item in pending) {
            val result = runCatching { repo.saveItem(item.copy(syncStatus = SyncStatus.SYNCED.name)) }
            if (result.isSuccess) removePendingEvidence(item.id)
        }
        // If still items in queue, re-schedule WorkManager sync (Android)
        if (dequeuePendingEvidence().isNotEmpty()) scheduleEvidenceSync()
    }

    fun onImageSelected(bytes: ByteArray, isFromCamera: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                pendingBytes = bytes,
                isAnalyzing = true,
                pendingAnalysis = null,
            )
            val result = if (isFromCamera) {
                ExifAnalysisResult(AiVerdict.AUTHENTIC, 0.0f, emptyList())
            } else {
                withContext(Dispatchers.Default) { analyzeImageExif(bytes) }
            }
            _uiState.value = _uiState.value.copy(
                pendingAnalysis = result,
                isAnalyzing = false,
            )
        }
    }

    fun saveEvidence(title: String, description: String) {
        val state = _uiState.value
        val bytes = state.pendingBytes ?: return
        val analysis = state.pendingAnalysis ?: return

        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true, error = null)
            repository.addEvidence(
                imageBytes = bytes,
                title = title,
                description = description,
                verdict = analysis.verdict,
                flags = analysis.flags,
                isFromCamera = analysis.verdict == AiVerdict.AUTHENTIC && analysis.flags.isEmpty(),
            ).onSuccess {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    pendingBytes = null,
                    pendingAnalysis = null,
                )
                loadEvidence()
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message ?: "Failed to save evidence. Check Firebase Storage is enabled.",
                )
            }
        }
    }

    fun deleteEvidence(id: String) {
        viewModelScope.launch {
            repository.deleteEvidence(id)
            loadEvidence()
        }
    }

    fun dismissPending() {
        _uiState.value = _uiState.value.copy(
            pendingBytes = null,
            pendingAnalysis = null,
            isAnalyzing = false,
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
