package com.strataguard.app.ui.strata

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.strataguard.app.data.strata.StrataPlan
import com.strataguard.app.data.strata.StrataRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchStrataUiState(
    val query: String = "",
    val selectedState: String = "NSW",
    val isLoading: Boolean = false,
    val isSeeding: Boolean = false,
    val results: List<StrataPlan> = emptyList(),
    val error: String? = null,
    val hasSearched: Boolean = false,
)

@OptIn(FlowPreview::class)
class SearchStrataViewModel(
    private val repository: StrataRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchStrataUiState())
    val uiState: StateFlow<SearchStrataUiState> = _uiState.asStateFlow()

    private val queryFlow = MutableStateFlow("")
    private var searchJob: Job? = null

    init {
        seed()
        viewModelScope.launch {
            queryFlow
                .debounce(400)
                .distinctUntilChanged()
                .collect { q -> if (q.length >= 2) executeSearch(q) }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query, error = null) }
        queryFlow.value = query
        if (query.length < 2) {
            _uiState.update { it.copy(results = emptyList(), hasSearched = false) }
        }
    }

    fun onStateToggle(state: String) {
        _uiState.update { it.copy(selectedState = state, results = emptyList(), hasSearched = false) }
        val q = _uiState.value.query
        if (q.length >= 2) executeSearch(q)
    }

    fun retry() = executeSearch(_uiState.value.query)

    private fun seed() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSeeding = true) }
            runCatching { repository.seedIfEmpty() }
            _uiState.update { it.copy(isSeeding = false) }
        }
    }

    private fun executeSearch(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                repository.searchPlans(query.trim(), _uiState.value.selectedState)
            }.fold(
                onSuccess = { results ->
                    _uiState.update { it.copy(isLoading = false, results = results, hasSearched = true) }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(isLoading = false, error = "Search failed. Please try again.", hasSearched = true)
                    }
                },
            )
        }
    }
}
