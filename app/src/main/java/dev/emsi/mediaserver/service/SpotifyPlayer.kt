package dev.emsi.mediaserver.service

import android.content.Context
import android.os.Looper
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.SimpleBasePlayer
import androidx.media3.common.util.UnstableApi
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import timber.log.Timber

@OptIn(UnstableApi::class)
class SpotifyPlayer(private val context: Context) : SimpleBasePlayer(Looper.getMainLooper()) {

    private var spotifyRemote: SpotifyAppRemote? = null

    private val myMediaItems = mutableListOf<MediaItem>()

    private var state = State.Builder()
        .setAvailableCommands(
            Player.Commands.Builder()
                .add(Player.COMMAND_PLAY_PAUSE)
                .add(Player.COMMAND_PREPARE)
                .add(Player.COMMAND_SET_MEDIA_ITEM)
                .add(Player.COMMAND_GET_CURRENT_MEDIA_ITEM)
                .add(Player.COMMAND_RELEASE)
                .build()
        )
        .setPlaybackState(Player.STATE_IDLE)
        .setAudioAttributes(AudioAttributes.DEFAULT)
        .build()

    init {
        connectToSpotifyRemote()
    }

    private fun updateState() {
        if (spotifyRemote != null && myMediaItems.isNotEmpty()) {
            Timber.d("Spotify play")
            myMediaItems.firstOrNull()?.also {
                spotifyRemote?.playerApi?.play(it.localConfiguration?.uri.toString())
                    ?.setResultCallback {
                        Timber.d(it.toString())
                    }
            }
        }
    }

    override fun getState(): State {
        return this.state
    }

    override fun handleSetMediaItems(
        mediaItems: MutableList<MediaItem>, startIndex: Int, startPositionMs: Long
    ): ListenableFuture<*> {
        Timber.d("handleSetMediaItems")
        myMediaItems.clear()
        myMediaItems.addAll(mediaItems)
        updateState()

        return Futures.immediateVoidFuture()
    }

    override fun handlePrepare(): ListenableFuture<*> {
        return Futures.immediateVoidFuture();
    }

    override fun handleSetPlayWhenReady(playWhenReady: Boolean): ListenableFuture<*> {
        return Futures.immediateVoidFuture()
    }

    private fun connectToSpotifyRemote() {
        val connectionParams = ConnectionParams.Builder(Spotify.CLIENT_ID)
            .setRedirectUri(Spotify.REDIRECT_URL)
            .showAuthView(true)
            .build()

        SpotifyAppRemote.connect(
            context,
            connectionParams,
            object : Connector.ConnectionListener {
                override fun onConnected(appRemote: SpotifyAppRemote) {
                    Timber.d("Spotify remote connected")
                    spotifyRemote = appRemote
                    updateState()
                }

                override fun onFailure(throwable: Throwable) {
                    Timber.e(throwable)
                }
            })
    }

    override fun handleRelease(): ListenableFuture<*> {
        Timber.d("handleRelease")
        spotifyRemote = null
        return Futures.immediateVoidFuture()
    }
}