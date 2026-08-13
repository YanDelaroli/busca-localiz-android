package com.yandelaroli.buscalocal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.yandelaroli.buscalocal.data.GooglePlacesRepository
import com.yandelaroli.buscalocal.location.DeviceLocationProvider
import com.yandelaroli.buscalocal.model.GeoPoint
import com.yandelaroli.buscalocal.model.PlaceCategory
import com.yandelaroli.buscalocal.model.SearchArea
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchViewModel(
    private val placesRepository: GooglePlacesRepository?,
    private val locationProvider: DeviceLocationProvider,
    apiKeyConfigured: Boolean,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        SearchUiState(apiKeyConfigured = apiKeyConfigured),
    )
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var activeTextQuery: String? = null
    private var locationJob: Job? = null
    private var searchJob: Job? = null

    fun onLocationPermissionChanged(granted: Boolean) {
        val shouldLoadLocation = granted &&
            (!_uiState.value.hasLocationPermission || _uiState.value.userLocation == null)

        _uiState.update {
            it.copy(
                hasLocationPermission = granted,
                errorMessage = if (granted) null else it.errorMessage,
            )
        }

        if (shouldLoadLocation) refreshLocation()
    }

    fun refreshLocation() {
        if (!_uiState.value.hasLocationPermission || locationJob?.isActive == true) return

        locationJob = viewModelScope.launch {
            _uiState.update {
                it.copy(isLocating = true, errorMessage = null)
            }

            try {
                val location = locationProvider.getCurrentLocation()
                if (location == null) {
                    _uiState.update {
                        it.copy(
                            isLocating = false,
                            errorMessage = "Não consegui obter sua localização. Ative a localização do aparelho e tente novamente.",
                        )
                    }
                    return@launch
                }

                _uiState.update {
                    it.copy(isLocating = false, userLocation = location)
                }
                searchAt(location)
            } catch (error: CancellationException) {
                throw error
            } catch (_: SecurityException) {
                _uiState.update {
                    it.copy(
                        isLocating = false,
                        hasLocationPermission = false,
                        errorMessage = "A permissão de localização é necessária para encontrar locais próximos.",
                    )
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isLocating = false,
                        errorMessage = "Não consegui obter sua localização agora. Verifique o GPS e tente novamente.",
                    )
                }
            }
        }
    }

    fun updateQuery(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun submitTextSearch() {
        val query = _uiState.value.query.trim()
        if (query.isBlank()) return

        activeTextQuery = query
        _uiState.update {
            it.copy(
                searchLabel = query,
                isTextSearchActive = true,
                selectedPlaceId = null,
            )
        }
        searchCurrentLocation()
    }

    fun selectCategory(category: PlaceCategory) {
        activeTextQuery = null
        _uiState.update {
            it.copy(
                selectedCategory = category,
                query = "",
                isTextSearchActive = false,
                searchLabel = category.label,
                selectedPlaceId = null,
            )
        }
        searchCurrentLocation()
    }

    fun selectArea(area: SearchArea) {
        if (area == _uiState.value.selectedArea) return
        _uiState.update {
            it.copy(selectedArea = area, selectedPlaceId = null)
        }
        searchCurrentLocation()
    }

    fun selectPlace(placeId: String) {
        _uiState.update { it.copy(selectedPlaceId = placeId) }
    }

    fun retry() {
        if (_uiState.value.userLocation == null) refreshLocation() else searchCurrentLocation()
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun searchCurrentLocation() {
        val location = _uiState.value.userLocation ?: return
        searchAt(location)
    }

    private fun searchAt(location: GeoPoint) {
        val repository = placesRepository
        if (repository == null) {
            _uiState.update {
                it.copy(isLoading = false, errorMessage = null)
            }
            return
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, errorMessage = null, selectedPlaceId = null)
            }

            try {
                val state = _uiState.value
                val places = activeTextQuery?.let { query ->
                    repository.searchByText(
                        query = query,
                        center = location,
                        radiusMeters = state.selectedArea.radiusMeters,
                    )
                } ?: repository.searchByCategory(
                    category = state.selectedCategory,
                    center = location,
                    radiusMeters = state.selectedArea.radiusMeters,
                )

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        places = places,
                        hasSearched = true,
                        selectedPlaceId = places.firstOrNull()?.id,
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        hasSearched = true,
                        places = emptyList(),
                        errorMessage = "A busca não pôde ser concluída. Verifique sua internet e a configuração da API do Google Maps.",
                    )
                }
            }
        }
    }
}

class SearchViewModelFactory(
    private val placesRepository: GooglePlacesRepository?,
    private val locationProvider: DeviceLocationProvider,
    private val apiKeyConfigured: Boolean,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
        extras: CreationExtras,
    ): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchViewModel(
                placesRepository = placesRepository,
                locationProvider = locationProvider,
                apiKeyConfigured = apiKeyConfigured,
            ) as T
        }
        throw IllegalArgumentException("ViewModel desconhecido: ${modelClass.name}")
    }
}
