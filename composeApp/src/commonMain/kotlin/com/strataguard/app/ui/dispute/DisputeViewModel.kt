package com.strataguard.app.ui.dispute

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.strataguard.app.data.dispute.Dispute
import com.strataguard.app.data.dispute.DisputeRepository
import com.strataguard.app.data.dispute.DisputeType
import com.strataguard.app.data.dispute.RiskAssessment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class DisputeUiState(
    val disputes: List<Dispute> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    // Create flow
    val showCreateSheet: Boolean = false,
    val selectedState: String = "NSW",
    val selectedType: String = DisputeType.BOND.name,
    val filingDeadline: String = "",
    val notes: String = "",
    val isSaving: Boolean = false,
    // Assessment
    val assessment: RiskAssessment? = null,
    val isAssessing: Boolean = false,
)

class DisputeViewModel(private val repository: DisputeRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(DisputeUiState())
    val uiState: StateFlow<DisputeUiState> = _uiState

    init { loadDisputes() }

    fun loadDisputes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val disputes = repository.getDisputes()
            _uiState.value = _uiState.value.copy(disputes = disputes, isLoading = false)
        }
    }

    fun showCreateSheet() {
        _uiState.value = _uiState.value.copy(showCreateSheet = true)
    }

    fun dismissCreateSheet() {
        _uiState.value = _uiState.value.copy(
            showCreateSheet = false, selectedState = "NSW",
            selectedType = DisputeType.BOND.name, filingDeadline = "", notes = "",
        )
    }

    fun onStateSelected(state: String) {
        _uiState.value = _uiState.value.copy(selectedState = state)
    }

    fun onTypeSelected(type: String) {
        _uiState.value = _uiState.value.copy(selectedType = type)
    }

    fun onFilingDeadlineChanged(deadline: String) {
        _uiState.value = _uiState.value.copy(filingDeadline = deadline)
    }

    fun onNotesChanged(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }

    fun createDispute(strataId: String? = null) {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true, error = null)
            repository.createDispute(
                state = state.selectedState,
                disputeType = state.selectedType,
                strataId = strataId,
                filingDeadline = state.filingDeadline.ifBlank { null },
                notes = state.notes.ifBlank { null },
            ).onSuccess {
                _uiState.value = _uiState.value.copy(isSaving = false, showCreateSheet = false)
                loadDisputes()
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isSaving = false, error = e.message ?: "Failed to create dispute")
            }
        }
    }

    fun runAssessment(disputeId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAssessing = true, error = null)
            repository.runAssessment(disputeId).onSuccess { result ->
                _uiState.value = _uiState.value.copy(isAssessing = false, assessment = result)
                loadDisputes()
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isAssessing = false, error = e.message)
            }
        }
    }

    fun deleteDispute(id: String) {
        viewModelScope.launch {
            repository.deleteDispute(id)
            loadDisputes()
        }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }
}
