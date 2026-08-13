package com.yandelaroli.buscalocal.model

enum class SearchArea(
    val label: String,
    val radiusMeters: Double,
    val description: String,
) {
    NEIGHBORHOOD(
        label = "Bairro",
        radiusMeters = 3_000.0,
        description = "até 3 km",
    ),
    CITY(
        label = "Cidade",
        radiusMeters = 20_000.0,
        description = "até 20 km",
    ),
}
