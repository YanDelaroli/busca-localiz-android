package com.yandelaroli.buscalocal.data

import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchByTextRequest
import com.google.android.libraries.places.api.net.SearchNearbyRequest
import com.google.android.libraries.places.api.net.SearchNearbyResponse
import com.google.android.libraries.places.api.net.SearchByTextResponse
import com.google.android.gms.maps.model.LatLng
import com.yandelaroli.buscalocal.model.DistanceCalculator
import com.yandelaroli.buscalocal.model.GeoPoint
import com.yandelaroli.buscalocal.model.NearbyPlace
import com.yandelaroli.buscalocal.model.PlaceCategory
import com.yandelaroli.buscalocal.model.PlaceStatus
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

class GooglePlacesRepository(
    private val placesClient: PlacesClient,
) {
    private val placeFields = listOf(
        Place.Field.ID,
        Place.Field.DISPLAY_NAME,
        Place.Field.FORMATTED_ADDRESS,
        Place.Field.LOCATION,
        Place.Field.RATING,
        Place.Field.USER_RATING_COUNT,
        Place.Field.BUSINESS_STATUS,
    )

    suspend fun searchByCategory(
        category: PlaceCategory,
        center: GeoPoint,
        radiusMeters: Double,
    ): List<NearbyPlace> = suspendCancellableCoroutine { continuation ->
        val cancellation = CancellationTokenSource()
        val bounds = CircularBounds.newInstance(center.toLatLng(), radiusMeters)
        val request = SearchNearbyRequest.builder(bounds, placeFields)
            .setIncludedTypes(category.placeTypes)
            .setMaxResultCount(MAX_RESULTS)
            .setRankPreference(SearchNearbyRequest.RankPreference.DISTANCE)
            .setCancellationToken(cancellation.token)
            .build()

        placesClient.searchNearby(request)
            .addOnSuccessListener { response: SearchNearbyResponse ->
                if (continuation.isActive) {
                    continuation.resume(
                        response.places.toNearbyPlaces(center, radiusMeters),
                    )
                }
            }
            .addOnFailureListener { error ->
                if (continuation.isActive) continuation.resumeWithException(error)
            }

        continuation.invokeOnCancellation { cancellation.cancel() }
    }

    suspend fun searchByText(
        query: String,
        center: GeoPoint,
        radiusMeters: Double,
    ): List<NearbyPlace> = suspendCancellableCoroutine { continuation ->
        val cancellation = CancellationTokenSource()
        val bounds = CircularBounds.newInstance(center.toLatLng(), radiusMeters)
        val request = SearchByTextRequest.builder(query.trim(), placeFields)
            .setLocationBias(bounds)
            .setMaxResultCount(MAX_RESULTS)
            .setRankPreference(SearchByTextRequest.RankPreference.DISTANCE)
            .setRegionCode("BR")
            .setCancellationToken(cancellation.token)
            .build()

        placesClient.searchByText(request)
            .addOnSuccessListener { response: SearchByTextResponse ->
                if (continuation.isActive) {
                    continuation.resume(
                        response.places.toNearbyPlaces(center, radiusMeters),
                    )
                }
            }
            .addOnFailureListener { error ->
                if (continuation.isActive) continuation.resumeWithException(error)
            }

        continuation.invokeOnCancellation { cancellation.cancel() }
    }

    private fun List<Place>.toNearbyPlaces(
        center: GeoPoint,
        radiusMeters: Double,
    ): List<NearbyPlace> = mapNotNull { place ->
        val placeLocation = place.location ?: return@mapNotNull null
        val location = GeoPoint(placeLocation.latitude, placeLocation.longitude)
        val distance = DistanceCalculator.metersBetween(center, location)

        if (distance > radiusMeters) return@mapNotNull null

        NearbyPlace(
            id = place.id ?: "${placeLocation.latitude},${placeLocation.longitude}",
            name = place.displayName?.takeIf(String::isNotBlank) ?: "Local sem nome",
            address = place.formattedAddress?.takeIf(String::isNotBlank)
                ?: "Endereço não informado",
            location = location,
            rating = place.rating,
            ratingCount = place.userRatingCount ?: 0,
            status = when (place.businessStatus) {
                Place.BusinessStatus.OPERATIONAL -> PlaceStatus.OPERATIONAL
                Place.BusinessStatus.CLOSED_TEMPORARILY -> PlaceStatus.TEMPORARILY_CLOSED
                Place.BusinessStatus.CLOSED_PERMANENTLY -> PlaceStatus.PERMANENTLY_CLOSED
                else -> PlaceStatus.UNKNOWN
            },
            distanceMeters = distance,
        )
    }.sortedBy(NearbyPlace::distanceMeters)

    private fun GeoPoint.toLatLng() = LatLng(latitude, longitude)

    private companion object {
        const val MAX_RESULTS = 20
    }
}
