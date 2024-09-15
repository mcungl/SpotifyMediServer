package dev.emsi.mediaserver.service

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Intent
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSourceBitmapLoader
import androidx.media3.session.CacheBitmapLoader
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaLibraryService.MediaLibrarySession
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionError
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.ListenableFuture
import dev.emsi.mediaserver.ui.player.PlayerActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.guava.future
import timber.log.Timber

@UnstableApi
class SpotifyMediaLibraryService : MediaLibraryService(), MediaLibrarySession.Callback {

    private lateinit var player: SpotifyPlayer
    private lateinit var spotifyMusicSource: SpotifyMusicSource

    private lateinit var mediaLibrarySession: MediaLibrarySession

    @OptIn(ExperimentalCoroutinesApi::class)
    private val limitedDispatcher = Dispatchers.IO.limitedParallelism(1)

    override fun onCreate() {
        super.onCreate()
        Timber.d("onCreate")

        spotifyMusicSource = SpotifyMusicSource(this)
        player = SpotifyPlayer(this)

        setupSession()
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("onDestroy")

        player.release()
        mediaLibrarySession.release()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession {
        return mediaLibrarySession
    }

    private fun setupSession() {
        mediaLibrarySession =
            MediaLibrarySession.Builder(this, player, this)
                .setSessionActivity(
                    PendingIntent.getActivity(
                        this,
                        0,
                        Intent(this, PlayerActivity::class.java),
                        FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT
                    )
                )
                .setBitmapLoader(CacheBitmapLoader(DataSourceBitmapLoader(/* context= */ this)))
                .build()
    }

    override fun onGetLibraryRoot(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        params: LibraryParams?
    ): ListenableFuture<LibraryResult<MediaItem>> {
        return spotifyMusicSource.callWhenMusicSourceReady {
            LibraryResult.ofItem(
                MediaItem
                    .Builder()
                    .setMediaId("root")
                    .setMediaMetadata(
                        MediaMetadata
                            .Builder()
                            .setMediaType(MediaMetadata.MEDIA_TYPE_FOLDER_PLAYLISTS)
                            .setIsPlayable(false)
                            .setIsBrowsable(true)
                            .build()
                    )
                    .build(), params
            )

        }
    }

    override fun onGetChildren(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        parentId: String,
        page: Int,
        pageSize: Int,
        params: LibraryParams?
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        return spotifyMusicSource.callWhenMusicSourceReady {
            if (parentId == "root") {
                LibraryResult.ofItemList(spotifyMusicSource.root, params)
            } else {
                CoroutineScope(limitedDispatcher).future {
                    LibraryResult.ofItemList(
                        spotifyMusicSource.getSongsForPlaylist(parentId)
                            .also { Timber.d("Number of tracks: ${it?.size}") },
                        params
                    )
                }.get()
            }
        }
    }

    override fun onGetItem(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        mediaId: String
    ): ListenableFuture<LibraryResult<MediaItem>> {
        return spotifyMusicSource.callWhenMusicSourceReady {
            CoroutineScope(limitedDispatcher).future {
                spotifyMusicSource.getSong(mediaId)?.let {
                    LibraryResult.ofItem(it, null)
                } ?: LibraryResult.ofError(SessionError.ERROR_BAD_VALUE)
            }.get().also {
                Timber.d(it.toString())
            }
        }
    }

    override fun onAddMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: List<MediaItem>
    ): ListenableFuture<List<MediaItem>> {
        return spotifyMusicSource.callWhenMusicSourceReady {
            CoroutineScope(limitedDispatcher).future {
                mediaItems.map { mediaItem ->
                    spotifyMusicSource.getSong(mediaItem.mediaId) ?: mediaItem
                }
            }.get()
        }
    }
}