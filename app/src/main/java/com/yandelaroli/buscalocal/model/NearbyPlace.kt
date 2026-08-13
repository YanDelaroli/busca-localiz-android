package com.yandelaroli.buscalocal.model

data class NearbyPlace(
    val id: String,
    val name: String,
    val address: String,
    val location: GeoPoint,
    val rating: Double?,
    val ratingCount: Int,
    val status: PlaceStatus,
    val distanceMeters: Double,
) {
    val distanceLabel: String
        get() = if (distanceMeters < 1_000) {
            "${distanceMeters.toInt()} m"
        } else {
            "%.1f km".format(distanceMeters / 1_000)
        }
}

enum class PlaceStatus(val label: String?) {
    OPERATIONAL("Em funcionamento"),
    TEMPORARILY_CLOSED("Temporariamente fechado"),
    PERMANENTLY_CLOSED("Fechado permanentemente"),
    UNKNOWN(null),
}
