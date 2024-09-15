package dev.emsi.mediaserver.ui.main

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainViewModel : ViewModel() {

    private val _spotifyRemoteAvailable = MutableStateFlow(false)
    val spotifyRemoteAvailable: StateFlow<Boolean> = _spotifyRemoteAvailable

    private val _spotifyApiAvailable = MutableStateFlow(false)
    val spotifyApiAvailable: StateFlow<Boolean> = _spotifyApiAvailable


    fun setSpotifyRemoteAvailable(value: Boolean) {
        _spotifyRemoteAvailable.value = value
    }

    fun setSpotifyApiAvailable(value: Boolean) {
        _spotifyApiAvailable.value = value
    }

}