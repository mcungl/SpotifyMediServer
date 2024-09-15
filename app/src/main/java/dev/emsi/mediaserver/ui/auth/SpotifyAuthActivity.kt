package dev.emsi.mediaserver.ui.auth

import com.adamratzman.spotify.SpotifyClientApi
import com.adamratzman.spotify.SpotifyScope
import com.adamratzman.spotify.auth.pkce.AbstractSpotifyPkceLoginActivity
import dev.emsi.mediaserver.MediaServerApplication
import dev.emsi.mediaserver.service.Spotify
import timber.log.Timber

class SpotifyAuthActivity : AbstractSpotifyPkceLoginActivity() {
    override val clientId = Spotify.CLIENT_ID
    override val redirectUri = Spotify.REDIRECT_URL
    override val scopes = SpotifyScope.entries




    override fun onSuccess(api: SpotifyClientApi) {
        val credentialStore = (application as MediaServerApplication).spotifyCredentialStore
        credentialStore.setSpotifyApi(api)

        Timber.d("Auth success¬")
        finish()
    }

    override fun onFailure(exception: Exception) {
        exception.printStackTrace()
        Timber.d("Auth failed: ${exception.message}")
    }
}