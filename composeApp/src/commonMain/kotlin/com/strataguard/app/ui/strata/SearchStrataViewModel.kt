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
    // Near Me
    val isNearbyMode: Boolean = false,
    val nearbyDistances: Map<String, Double> = emptyMap(), // spNumber -> km
    val locationError: String? = null,
    // Add Building form
    val showAddBuilding: Boolean = false,
    val addPlanNumber: String = "",
    val addAddress: String = "",
    val addSuburb: String = "",
    val addPostcode: String = "",
    val addLotCount: String = "",
    val addAgent: String = "",
    val isSavingPlan: Boolean = false,
    val addError: String? = null,
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

    fun searchNearby(lat: Double, lng: Double) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, error = null, locationError = null, isNearbyMode = true,
                    results = emptyList(), nearbyDistances = emptyMap(), hasSearched = false)
            }
            runCatching {
                repository.searchNearby(lat, lng, radiusKm = 10.0)
            }.fold(
                onSuccess = { pairs ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            results = pairs.map { (plan, _) -> plan },
                            nearbyDistances = pairs.associate { (plan, dist) -> plan.spNumber to dist },
                            hasSearched = true,
                        )
                    }
                },
                onFailure = {
                    _uiState.update { it.copy(isLoading = false, error = "Location search failed. Please try again.", hasSearched = true) }
                },
            )
        }
    }

    fun onLocationPermissionDenied() {
        _uiState.update { it.copy(locationError = "Location permission denied. Enable it in Settings to use Near Me.") }
    }

    fun onLocationUnavailable() {
        _uiState.update { it.copy(locationError = "Couldn't get your location. Please try again.") }
    }

    fun exitNearbyMode() {
        _uiState.update {
            it.copy(isNearbyMode = false, results = emptyList(), nearbyDistances = emptyMap(),
                hasSearched = false, locationError = null)
        }
    }

    fun showAddBuilding() {
        val q = _uiState.value.query.trim()
        val prefilledNumber = if (q.uppercase().startsWith("SP") || q.uppercase().startsWith("OC")) q.uppercase() else ""
        _uiState.update {
            it.copy(
                showAddBuilding = true,
                addPlanNumber = prefilledNumber,
                addAddress = "",
                addSuburb = "",
                addPostcode = "",
                addLotCount = "",
                addAgent = "",
                addError = null,
            )
        }
    }

    fun dismissAddBuilding() = _uiState.update { it.copy(showAddBuilding = false, addError = null) }

    fun onAddPlanNumber(v: String) = _uiState.update { it.copy(addPlanNumber = v.uppercase(), addError = null) }
    fun onAddAddress(v: String) = _uiState.update { it.copy(addAddress = v, addError = null) }
    fun onAddSuburb(v: String) = _uiState.update { it.copy(addSuburb = v, addError = null) }
    fun onAddPostcode(v: String) = _uiState.update { it.copy(addPostcode = v.filter { c -> c.isDigit() }.take(4), addError = null) }
    fun onAddLotCount(v: String) = _uiState.update { it.copy(addLotCount = v.filter { c -> c.isDigit() }, addError = null) }
    fun onAddAgent(v: String) = _uiState.update { it.copy(addAgent = v, addError = null) }

    fun submitAddBuilding(onSuccess: (String) -> Unit) {
        val s = _uiState.value
        val spNumber = s.addPlanNumber.trim()
        val address = s.addAddress.trim()
        val suburb = s.addSuburb.trim()

        if (spNumber.isBlank()) {
            _uiState.update { it.copy(addError = "Plan number is required (e.g. SP12345)") }
            return
        }
        if (!spNumber.startsWith("SP") && !spNumber.startsWith("OC")) {
            _uiState.update { it.copy(addError = "NSW plans start with SP, VIC plans start with OC") }
            return
        }
        if (address.isBlank()) {
            _uiState.update { it.copy(addError = "Address is required") }
            return
        }
        if (suburb.isBlank()) {
            _uiState.update { it.copy(addError = "Suburb is required") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSavingPlan = true, addError = null) }
            val plan = StrataPlan(
                spNumber = spNumber,
                address = address,
                suburb = suburb,
                suburbLower = suburb.lowercase(),
                postcode = s.addPostcode.trim(),
                state = s.selectedState,
                lotCount = s.addLotCount.toIntOrNull() ?: 0,
                managingAgent = s.addAgent.trim(),
            )
            runCatching { repository.createPlan(plan) }.fold(
                onSuccess = {
                    _uiState.update { it.copy(isSavingPlan = false, showAddBuilding = false) }
                    onSuccess(spNumber)
                },
                onFailure = {
                    _uiState.update { it.copy(isSavingPlan = false, addError = "Failed to save. Please try again.") }
                },
            )
        }
    }

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
