package com.yandelaroli.buscalocal

object ApiKeyStatus {
    val isConfigured: Boolean
        get() = BuildConfig.MAPS_API_KEY.isNotBlank() &&
            BuildConfig.MAPS_API_KEY != "YOUR_API_KEY" &&
            BuildConfig.MAPS_API_KEY != "SUA_CHAVE_AQUI"
}
