package com.yandelaroli.buscalocal

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yandelaroli.buscalocal.location.DeviceLocationProvider
import com.yandelaroli.buscalocal.model.GeoPoint
import com.yandelaroli.buscalocal.model.NearbyPlace
import com.yandelaroli.buscalocal.ui.SearchScreen
import com.yandelaroli.buscalocal.ui.SearchViewModel
import com.yandelaroli.buscalocal.ui.SearchViewModelFactory
import com.yandelaroli.buscalocal.ui.theme.BuscaLocalTheme

class MainActivity : ComponentActivity() {
    private val viewModel: SearchViewModel by viewModels {
        val app = application as BuscaLocalApplication
        SearchViewModelFactory(
            placesRepository = app.createPlacesRepository(),
            locationProvider = DeviceLocationProvider(applicationContext),
            apiKeyConfigured = ApiKeyStatus.isConfigured,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BuscaLocalTheme {
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                var permissionGranted by remember { mutableStateOf(hasLocationPermission()) }
                val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions(),
                ) { result ->
                    permissionGranted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        result[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
                        hasLocationPermission()
                }

                LaunchedEffect(permissionGranted) {
                    viewModel.onLocationPermissionChanged(permissionGranted)
                }

                SearchScreen(
                    state = state,
                    onQueryChanged = viewModel::updateQuery,
                    onSearch = viewModel::submitTextSearch,
                    onCategorySelected = viewModel::selectCategory,
                    onAreaSelected = viewModel::selectArea,
                    onPlaceSelected = viewModel::selectPlace,
                    onRequestPermission = {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                            ),
                        )
                    },
                    onRefreshLocation = viewModel::refreshLocation,
                    onRetry = viewModel::retry,
                    onDismissError = viewModel::clearError,
                    onOpenDirections = ::openInGoogleMaps,
                    onOpenExternalSearch = { query ->
                        openSearchInGoogleMaps(query, state.userLocation)
                    },
                    onOpenLocationSettings = {
                        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onLocationPermissionChanged(hasLocationPermission())
    }

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED

    private fun openInGoogleMaps(place: NearbyPlace) {
        val latitude = place.location.latitude
        val longitude = place.location.longitude
        val geoUri = Uri.parse(
            "geo:$latitude,$longitude?q=$latitude,$longitude(${Uri.encode(place.name)})",
        )
        val mapsIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
            setPackage("com.google.android.apps.maps")
        }

        try {
            startActivity(mapsIntent)
        } catch (_: ActivityNotFoundException) {
            val browserUri = Uri.parse(
                "https://www.google.com/maps/search/?api=1" +
                    "&query=$latitude,$longitude&query_place_id=${Uri.encode(place.id)}",
            )
            startActivity(Intent(Intent.ACTION_VIEW, browserUri))
        }
    }

    private fun openSearchInGoogleMaps(query: String, location: GeoPoint?) {
        val center = location?.let { "${it.latitude},${it.longitude}" } ?: "0,0"
        val geoUri = Uri.parse("geo:$center?q=${Uri.encode(query)}")
        val mapsIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
            setPackage("com.google.android.apps.maps")
        }

        try {
            startActivity(mapsIntent)
        } catch (_: ActivityNotFoundException) {
            val browserQuery = location?.let {
                "$query perto de ${it.latitude},${it.longitude}"
            } ?: query
            val browserUri = Uri.parse(
                "https://www.google.com/maps/search/?api=1&query=${Uri.encode(browserQuery)}",
            )
            startActivity(Intent(Intent.ACTION_VIEW, browserUri))
        }
    }
}
