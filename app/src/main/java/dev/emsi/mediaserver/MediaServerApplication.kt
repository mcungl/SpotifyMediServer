package dev.emsi.mediaserver

import android.app.Application
import com.adamratzman.spotify.auth.SpotifyDefaultCredentialStore
import com.spotify.android.appremote.BuildConfig
import com.spotify.android.appremote.api.SpotifyAppRemote
import dev.emsi.mediaserver.service.Spotify
import timber.log.Timber

class MediaServerApplication : Application() {

    val spotifyCredentialStore by lazy {
        SpotifyDefaultCredentialStore(
            clientId = Spotify.CLIENT_ID,
            redirectUri = Spotify.REDIRECT_URL,
            applicationContext = this
        )
    }

    override fun onCreate() {
        super.onCreate()

        SpotifyAppRemote.setDebugMode(BuildConfig.DEBUG)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}