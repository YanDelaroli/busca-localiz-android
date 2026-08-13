package com.yandelaroli.buscalocal.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DistanceCalculatorTest {
    @Test
    fun samePointHasZeroDistance() {
        val point = GeoPoint(latitude = -23.5505, longitude = -46.6333)

        assertEquals(0.0, DistanceCalculator.metersBetween(point, point), 0.001)
    }

    @Test
    fun saoPauloToRioHasExpectedApproximateDistance() {
        val saoPaulo = GeoPoint(latitude = -23.5505, longitude = -46.6333)
        val rioDeJaneiro = GeoPoint(latitude = -22.9068, longitude = -43.1729)

        val distance = DistanceCalculator.metersBetween(saoPaulo, rioDeJaneiro)

        assertTrue(distance in 350_000.0..370_000.0)
    }

    @Test
    fun oneDegreeOfLatitudeIsAboutOneHundredElevenKilometers() {
        val origin = GeoPoint(latitude = 0.0, longitude = 0.0)
        val destination = GeoPoint(latitude = 1.0, longitude = 0.0)

        val distance = DistanceCalculator.metersBetween(origin, destination)

        assertEquals(111_195.0, distance, 250.0)
    }
}
