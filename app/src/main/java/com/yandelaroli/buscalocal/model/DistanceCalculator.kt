package com.yandelaroli.buscalocal.model

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object DistanceCalculator {
    private const val EARTH_RADIUS_METERS = 6_371_000.0

    fun metersBetween(from: GeoPoint, to: GeoPoint): Double {
        val latitudeDelta = Math.toRadians(to.latitude - from.latitude)
        val longitudeDelta = Math.toRadians(to.longitude - from.longitude)
        val fromLatitude = Math.toRadians(from.latitude)
        val toLatitude = Math.toRadians(to.latitude)

        val haversine =
            sin(latitudeDelta / 2) * sin(latitudeDelta / 2) +
                cos(fromLatitude) * cos(toLatitude) *
                sin(longitudeDelta / 2) * sin(longitudeDelta / 2)

        return 2 * EARTH_RADIUS_METERS * asin(sqrt(haversine.coerceIn(0.0, 1.0)))
    }
}
