package com.yandelaroli.buscalocal.model

data class PlaceCategory(
    val id: String,
    val label: String,
    val placeTypes: List<String>,
) {
    companion object {
        val all = listOf(
            PlaceCategory("pharmacy", "Farmácia", listOf("pharmacy")),
            PlaceCategory("bakery", "Bolos e padarias", listOf("bakery", "dessert_shop")),
            PlaceCategory("supermarket", "Supermercado", listOf("supermarket", "grocery_store")),
            PlaceCategory("gym", "Academia", listOf("gym")),
            PlaceCategory("restaurant", "Restaurante", listOf("restaurant")),
            PlaceCategory("cafe", "Cafeteria", listOf("cafe")),
            PlaceCategory("gas_station", "Posto", listOf("gas_station")),
            PlaceCategory("hospital", "Hospital", listOf("hospital")),
            PlaceCategory("bank", "Banco", listOf("bank")),
            PlaceCategory("pet_store", "Pet shop", listOf("pet_store", "veterinary_care")),
            PlaceCategory("hardware_store", "Material de construção", listOf("hardware_store")),
            PlaceCategory("shopping_mall", "Shopping", listOf("shopping_mall")),
        )

        val default = all.first()
    }
}
