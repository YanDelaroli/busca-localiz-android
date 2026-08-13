package com.yandelaroli.buscalocal

import android.app.Application
import com.google.android.libraries.places.api.Places
import com.yandelaroli.buscalocal.data.GooglePlacesRepository

class BuscaLocalApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (ApiKeyStatus.isConfigured && !Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(
                applicationContext,
                BuildConfig.MAPS_API_KEY,
            )
        }
    }

    fun createPlacesRepository(): GooglePlacesRepository? =
        if (ApiKeyStatus.isConfigured && Places.isInitialized()) {
            GooglePlacesRepository(Places.createClient(this))
        } else {
            null
        }
}
