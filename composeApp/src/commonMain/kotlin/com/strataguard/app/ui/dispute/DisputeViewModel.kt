package com.strataguard.app.ui.dispute

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.strataguard.app.data.dispute.Dispute
import com.strataguard.app.data.dispute.DisputeRepository
import com.strataguard.app.data.dispute.DisputeType
import com.strataguard.app.data.dispute.RiskAssessment
import com.strataguard.app.data.remote.PdfExportRequest
import com.strataguard.app.data.remote.StrataGuardApiClient
import com.strataguard.app.platform.cancelDeadlineReminders
import com.strataguard.app.platform.savePdfAndShare
import com.strataguard.app.platform.scheduleDeadlineReminders
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
    val assessingDisputeId: String? = null,
    // PDF export
    val exportingDisputeId: String? = null,
)

class DisputeViewModel(
    private val repository: DisputeRepository,
    private val apiClient: StrataGuardApiClient,
) : ViewModel() {

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
            ).onSuccess { dispute ->
                _uiState.value = _uiState.value.copy(isSaving = false, showCreateSheet = false)
                if (dispute.filingDeadline.isNotBlank()) {
                    scheduleDeadlineReminders(
                        disputeId = dispute.id,
                        disputeType = dispute.disputeType,
                        tribunal = dispute.tribunal,
                        deadlineIso = dispute.filingDeadline,
                    )
                }
                loadDisputes()
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isSaving = false, error = e.message ?: "Failed to create dispute")
            }
        }
    }

    fun runAssessment(disputeId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(assessingDisputeId = disputeId, error = null)
            repository.runAssessment(disputeId).onSuccess { result ->
                val updatedDisputes = _uiState.value.disputes.map { d ->
                    if (d.id == disputeId) d.copy(
                        riskScore = result.score,
                        riskVerdict = result.verdict,
                        riskFactors = result.factors,
                    ) else d
                }
                _uiState.value = _uiState.value.copy(
                    assessingDisputeId = null,
                    assessment = result,
                    disputes = updatedDisputes,
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    assessingDisputeId = null,
                    error = e.message ?: "Risk assessment failed",
                )
            }
        }
    }

    fun exportPdf(dispute: Dispute) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(exportingDisputeId = dispute.id, error = null)
            val request = PdfExportRequest(
                disputeType = dispute.disputeType,
                state = dispute.state,
                tribunal = dispute.tribunal,
                status = dispute.status,
                strataAddress = dispute.strataAddress.ifBlank { null },
                filingDeadline = dispute.filingDeadline.ifBlank { null },
                riskScore = if (dispute.riskScore > 0f) dispute.riskScore.toDouble() else null,
                riskVerdict = dispute.riskVerdict.ifBlank { null },
                riskFactors = dispute.riskFactors.ifEmpty { null },
            )
            apiClient.generatePdf(request)
                .onSuccess { bytes ->
                    _uiState.value = _uiState.value.copy(exportingDisputeId = null)
                    savePdfAndShare(bytes, "evidence-pack-${dispute.id.take(8)}.pdf")
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        exportingDisputeId = null,
                        error = "Could not generate PDF. Make sure the StrataGuard server is running.",
                    )
                }
        }
    }

    fun deleteDispute(id: String) {
        viewModelScope.launch {
            cancelDeadlineReminders(id)
            repository.deleteDispute(id)
            loadDisputes()
        }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }
}
