package com.strataguard.app.ui.strata

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.strataguard.app.data.remote.StrataGuardApiClient
import com.strataguard.app.data.strata.StrataPlan
import com.strataguard.app.data.strata.StrataDocument
import com.strataguard.app.data.strata.StrataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StrataPlanDetailUiState(
    val plan: StrataPlan? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val documents: List<StrataDocument> = emptyList(),
    val isUploadingDocument: Boolean = false,
    val documentError: String? = null,
)

class StrataPlanDetailViewModel(
    private val repository: StrataRepository,
    private val apiClient: StrataGuardApiClient,
    private val spNumber: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StrataPlanDetailUiState())
    val uiState: StateFlow<StrataPlanDetailUiState> = _uiState.asStateFlow()

    init {
        load()
        loadDocuments()
    }

    fun retry() { load(); loadDocuments() }

    fun uploadDocument(bytes: ByteArray, filename: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingDocument = true, documentError = null) }
            apiClient.uploadDocument(
                planNumber = spNumber,
                bytes = bytes,
                filename = filename,
                title = null,
                docType = "strata_report",
            ).onSuccess { doc ->
                _uiState.update { state ->
                    state.copy(
                        isUploadingDocument = false,
                        documents = listOf(doc) + state.documents,
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(
                    isUploadingDocument = false,
                    documentError = e.message ?: "Upload failed",
                ) }
            }
        }
    }

    fun clearDocumentError() = _uiState.update { it.copy(documentError = null) }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { repository.getPlan(spNumber) }.fold(
                onSuccess = { plan ->
                    if (plan != null) {
                        _uiState.update { it.copy(isLoading = false, plan = plan) }
                    } else {
                        _uiState.update { it.copy(isLoading = false, error = "Plan not found.") }
                    }
                },
                onFailure = {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load plan. Please try again.") }
                },
            )
        }
    }

    private fun loadDocuments() {
        viewModelScope.launch {
            apiClient.listDocuments(spNumber).onSuccess { docs ->
                _uiState.update { it.copy(documents = docs) }
            }
        }
    }
}
