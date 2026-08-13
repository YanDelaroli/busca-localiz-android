package com.yandelaroli.buscalocal.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import com.yandelaroli.buscalocal.model.GeoPoint
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

class DeviceLocationProvider(context: Context) {
    private val client = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): GeoPoint? {
        val cancellation = CancellationTokenSource()
        val request = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMaxUpdateAgeMillis(30_000)
            .setDurationMillis(15_000)
            .build()

        val current = client.getCurrentLocation(request, cancellation.token)
            .awaitNullable(cancellation)
            ?: client.lastLocation.awaitNullable()

        return current?.let { GeoPoint(it.latitude, it.longitude) }
    }

    private suspend fun Task<Location>.awaitNullable(
        cancellationTokenSource: CancellationTokenSource? = null,
    ): Location? = suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { location ->
            if (continuation.isActive) continuation.resume(location)
        }
        addOnFailureListener { error ->
            if (continuation.isActive) continuation.resumeWithException(error)
        }
        addOnCanceledListener {
            if (continuation.isActive) continuation.resume(null)
        }
        continuation.invokeOnCancellation { cancellationTokenSource?.cancel() }
    }
}
