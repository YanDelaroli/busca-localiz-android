package com.yandelaroli.buscalocal.ui

import com.yandelaroli.buscalocal.model.GeoPoint
import com.yandelaroli.buscalocal.model.NearbyPlace
import com.yandelaroli.buscalocal.model.PlaceCategory
import com.yandelaroli.buscalocal.model.SearchArea

data class SearchUiState(
    val apiKeyConfigured: Boolean,
    val hasLocationPermission: Boolean = false,
    val isLocating: Boolean = false,
    val isLoading: Boolean = false,
    val userLocation: GeoPoint? = null,
    val selectedCategory: PlaceCategory = PlaceCategory.default,
    val selectedArea: SearchArea = SearchArea.NEIGHBORHOOD,
    val query: String = "",
    val isTextSearchActive: Boolean = false,
    val searchLabel: String = PlaceCategory.default.label,
    val places: List<NearbyPlace> = emptyList(),
    val selectedPlaceId: String? = null,
    val hasSearched: Boolean = false,
    val errorMessage: String? = null,
)
