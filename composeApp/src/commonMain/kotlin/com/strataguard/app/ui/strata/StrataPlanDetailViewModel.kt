package com.strataguard.app.ui.strata

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.strataguard.app.data.strata.StrataPlan
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
)

class StrataPlanDetailViewModel(
    private val repository: StrataRepository,
    private val spNumber: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StrataPlanDetailUiState())
    val uiState: StateFlow<StrataPlanDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun retry() = load()

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
}
