package com.yandelaroli.buscalocal.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaceCategoryTest {
    @Test
    fun categoryIdsAreUnique() {
        val ids = PlaceCategory.all.map(PlaceCategory::id)

        assertEquals(ids.size, ids.distinct().size)
    }

    @Test
    fun everyCategoryHasAtLeastOneGooglePlaceType() {
        assertTrue(PlaceCategory.all.all { it.placeTypes.isNotEmpty() })
    }

    @Test
    fun defaultCategoryIsAvailableInList() {
        assertTrue(PlaceCategory.default in PlaceCategory.all)
    }
}
