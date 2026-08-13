package com.yandelaroli.buscalocal.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Directions
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Hardware
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.LocalGasStation
import androidx.compose.material.icons.rounded.LocalGroceryStore
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.LocalMall
import androidx.compose.material.icons.rounded.LocalPharmacy
import androidx.compose.material.icons.rounded.LocationOff
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import com.yandelaroli.buscalocal.model.NearbyPlace
import com.yandelaroli.buscalocal.model.PlaceCategory
import com.yandelaroli.buscalocal.model.PlaceStatus
import com.yandelaroli.buscalocal.model.SearchArea

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    state: SearchUiState,
    onQueryChanged: (String) -> Unit,
    onSearch: () -> Unit,
    onCategorySelected: (PlaceCategory) -> Unit,
    onAreaSelected: (SearchArea) -> Unit,
    onPlaceSelected: (String) -> Unit,
    onRequestPermission: () -> Unit,
    onRefreshLocation: () -> Unit,
    onRetry: () -> Unit,
    onDismissError: () -> Unit,
    onOpenDirections: (NearbyPlace) -> Unit,
    onOpenLocationSettings: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        val message = state.errorMessage ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = message,
            actionLabel = "Tentar novamente",
            withDismissAction = true,
        )
        if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) onRetry()
        onDismissError()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Busca Local",
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Encontre o que precisa perto de você",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            SearchControls(
                state = state,
                onQueryChanged = onQueryChanged,
                onSearch = onSearch,
                onCategorySelected = onCategorySelected,
                onAreaSelected = onAreaSelected,
            )

            when {
                !state.apiKeyConfigured -> ApiKeySetupContent()
                !state.hasLocationPermission -> PermissionContent(onRequestPermission)
                state.userLocation == null -> LocationLoadingContent(
                    isLoading = state.isLocating,
                    onRetry = onRefreshLocation,
                    onOpenSettings = onOpenLocationSettings,
                )
                else -> SearchResultsContent(
                    state = state,
                    onPlaceSelected = onPlaceSelected,
                    onRefreshLocation = onRefreshLocation,
                    onOpenDirections = onOpenDirections,
                )
            }
        }
    }
}

@Composable
private fun SearchControls(
    state: SearchUiState,
    onQueryChanged: (String) -> Unit,
    onSearch: () -> Unit,
    onCategorySelected: (PlaceCategory) -> Unit,
    onAreaSelected: (SearchArea) -> Unit,
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        OutlinedTextField(
            value = state.query,
            onValueChange = onQueryChanged,
            modifier = Modifier.fillMaxWidth(),
            enabled = state.apiKeyConfigured,
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
            placeholder = { Text("Ex.: loja de bolo, papelaria…") },
            leadingIcon = {
                Icon(Icons.Rounded.Search, contentDescription = null)
            },
            trailingIcon = {
                if (state.query.isNotBlank()) {
                    IconButton(onClick = { onQueryChanged("") }) {
                        Icon(Icons.Rounded.Close, contentDescription = "Limpar busca")
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    focusManager.clearFocus()
                    onSearch()
                },
            ),
        )

        Spacer(Modifier.height(10.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(end = 8.dp),
        ) {
            items(PlaceCategory.all, key = PlaceCategory::id) { category ->
                FilterChip(
                    selected = !state.isTextSearchActive &&
                        state.selectedCategory.id == category.id,
                    onClick = { onCategorySelected(category) },
                    enabled = state.apiKeyConfigured,
                    label = { Text(category.label) },
                    leadingIcon = {
                        Icon(
                            imageVector = categoryIcon(category.id),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Área:",
                style = MaterialTheme.typography.labelLarge,
            )
            SearchArea.entries.forEach { area ->
                FilterChip(
                    selected = state.selectedArea == area,
                    onClick = { onAreaSelected(area) },
                    enabled = state.apiKeyConfigured,
                    label = { Text("${area.label} · ${area.description}") },
                )
            }
        }
    }
}

@Composable
private fun SearchResultsContent(
    state: SearchUiState,
    onPlaceSelected: (String) -> Unit,
    onRefreshLocation: () -> Unit,
    onOpenDirections: (NearbyPlace) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        MapSection(
            state = state,
            onPlaceSelected = onPlaceSelected,
            onRefreshLocation = onRefreshLocation,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = state.searchLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = if (state.isLoading) {
                        "Buscando locais…"
                    } else {
                        "${state.places.size} resultado(s) em ${state.selectedArea.label.lowercase()}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = "Dados do Google Maps",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        when {
            state.isLoading && state.places.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            state.hasSearched && state.places.isEmpty() -> EmptyResultsContent(
                modifier = Modifier.fillMaxSize(),
                searchArea = state.selectedArea,
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(state.places, key = NearbyPlace::id) { place ->
                    PlaceResultCard(
                        place = place,
                        selected = place.id == state.selectedPlaceId,
                        onClick = { onPlaceSelected(place.id) },
                        onOpenDirections = { onOpenDirections(place) },
                    )
                }
            }
        }
    }
}

@Composable
private fun MapSection(
    state: SearchUiState,
    onPlaceSelected: (String) -> Unit,
    onRefreshLocation: () -> Unit,
) {
    val userLocation = state.userLocation ?: return
    val cameraPositionState = rememberCameraPositionState()
    val selectedPlace = state.places.firstOrNull { it.id == state.selectedPlaceId }

    LaunchedEffect(userLocation) {
        cameraPositionState.animate(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(userLocation.latitude, userLocation.longitude),
                if (state.selectedArea == SearchArea.NEIGHBORHOOD) 14f else 11f,
            ),
            durationMs = 650,
        )
    }

    LaunchedEffect(selectedPlace?.id) {
        selectedPlace?.let { place ->
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLng(
                    LatLng(place.location.latitude, place.location.longitude),
                ),
                durationMs = 450,
            )
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(245.dp)
            .padding(start = 16.dp, end = 16.dp, top = 8.dp),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = state.hasLocationPermission,
                ),
                uiSettings = MapUiSettings(
                    myLocationButtonEnabled = false,
                    zoomControlsEnabled = false,
                    compassEnabled = true,
                ),
            ) {
                state.places.forEach { place ->
                    val selected = place.id == state.selectedPlaceId
                    Marker(
                        state = rememberUpdatedMarkerState(
                            position = LatLng(
                                place.location.latitude,
                                place.location.longitude,
                            ),
                        ),
                        title = place.name,
                        snippet = "${place.distanceLabel} · ${place.address}",
                        icon = BitmapDescriptorFactory.defaultMarker(
                            if (selected) BitmapDescriptorFactory.HUE_GREEN
                            else BitmapDescriptorFactory.HUE_RED,
                        ),
                        onClick = {
                            onPlaceSelected(place.id)
                            false
                        },
                    )
                }
            }

            if (state.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                )
            }

            SmallFloatingActionButton(
                onClick = onRefreshLocation,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp),
            ) {
                Icon(Icons.Rounded.MyLocation, contentDescription = "Atualizar localização")
            }
        }
    }
}

@Composable
private fun PlaceResultCard(
    place: NearbyPlace,
    selected: Boolean,
    onClick: () -> Unit,
    onOpenDirections: () -> Unit,
) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outlineVariant,
        ),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.32f)
            else MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = place.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = place.address,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
                AssistChip(
                    onClick = onClick,
                    label = { Text(place.distanceLabel) },
                )
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider()
            Spacer(Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (place.rating != null) {
                    Icon(
                        Icons.Rounded.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(19.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "%.1f".format(place.rating),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = " (${place.ratingCount})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Text(
                        text = "Sem avaliações",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                place.status.label?.let { statusLabel ->
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = when (place.status) {
                            PlaceStatus.OPERATIONAL -> Color(0xFF2E7D32)
                            PlaceStatus.TEMPORARILY_CLOSED -> Color(0xFFB26A00)
                            PlaceStatus.PERMANENTLY_CLOSED -> MaterialTheme.colorScheme.error
                            PlaceStatus.UNKNOWN -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }

                Spacer(Modifier.weight(1f))
                FilledTonalButton(onClick = onOpenDirections) {
                    Icon(
                        Icons.Rounded.Directions,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Como chegar")
                }
            }
        }
    }
}

@Composable
private fun ApiKeySetupContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(22.dp)) {
                Icon(
                    Icons.Rounded.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(42.dp),
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "Configure o Google Maps",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Ative Maps SDK for Android e Places API (New) no Google Cloud. " +
                        "Depois, adicione sua chave ao arquivo local.properties:",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "MAPS_API_KEY=SUA_CHAVE_AQUI",
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(10.dp),
                        )
                        .padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "A chave não deve ser enviada para o GitHub. Veja o passo a passo completo no README.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PermissionContent(onRequestPermission: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Rounded.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Encontre locais perto de você",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Permita o acesso à localização para buscar estabelecimentos no seu bairro ou cidade.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(20.dp))
            Button(onClick = onRequestPermission) {
                Icon(Icons.Rounded.MyLocation, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Usar minha localização")
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = "A localização é usada somente enquanto você utiliza o aplicativo.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun LocationLoadingContent(
    isLoading: Boolean,
    onRetry: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (isLoading) {
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
                Text("Obtendo sua localização…")
            } else {
                Icon(
                    Icons.Rounded.LocationOff,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.error,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Localização indisponível",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(14.dp))
                Button(onClick = onRetry) {
                    Icon(Icons.Rounded.Refresh, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Tentar novamente")
                }
            }
            TextButton(onClick = onOpenSettings) {
                Text("Abrir configurações de localização")
            }
        }
    }
}

@Composable
private fun EmptyResultsContent(
    modifier: Modifier = Modifier,
    searchArea: SearchArea,
) {
    Box(modifier = modifier.padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Rounded.SearchOff,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Nenhum local encontrado",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = if (searchArea == SearchArea.NEIGHBORHOOD) {
                    "Tente selecionar Cidade para ampliar a busca."
                } else {
                    "Tente outra categoria ou termo de busca."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun categoryIcon(categoryId: String): ImageVector = when (categoryId) {
    "pharmacy" -> Icons.Rounded.LocalPharmacy
    "bakery" -> Icons.Rounded.Cake
    "supermarket" -> Icons.Rounded.LocalGroceryStore
    "gym" -> Icons.Rounded.FitnessCenter
    "restaurant" -> Icons.Rounded.Restaurant
    "cafe" -> Icons.Rounded.LocalCafe
    "gas_station" -> Icons.Rounded.LocalGasStation
    "hospital" -> Icons.Rounded.LocalHospital
    "bank" -> Icons.Rounded.AccountBalance
    "pet_store" -> Icons.Rounded.Pets
    "hardware_store" -> Icons.Rounded.Hardware
    "shopping_mall" -> Icons.Rounded.LocalMall
    else -> Icons.Rounded.Storefront
}
