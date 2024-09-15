package dev.emsi.mediaserver.extensions

import android.content.Context
import com.adamratzman.spotify.SpotifyClientApi
import com.spotify.android.appremote.api.SpotifyAppRemote
import dev.emsi.mediaserver.MediaServerApplication
import kotlinx.coroutines.runBlocking


fun Context.getSpotifyApi(): SpotifyClientApi? {
    return runBlocking { (applicationContext as MediaServerApplication).spotifyCredentialStore.getSpotifyClientPkceApi() }
}

fun Context.isSpotifyRemoteAvailable(): Boolean {
    return SpotifyAppRemote.isSpotifyInstalled(this)
}

fun Context.isSpotifyApiAvailable(): Boolean {
    return getSpotifyApi() != null
}