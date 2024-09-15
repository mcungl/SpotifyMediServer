package dev.emsi.mediaserver.service

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.adamratzman.spotify.SpotifyClientApi
import dev.emsi.mediaserver.extensions.getSpotifyApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class SpotifyMusicSource(context: Context) : AbstractMusicSource() {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var spotifyApi: SpotifyClientApi? = null

    var root: List<MediaItem> = emptyList()
        private set

    init {
        musicSourceState = MusicSourceState.Initializing

        spotifyApi = context.getSpotifyApi()

        coroutineScope.launch {
            root = buildRoot()
            musicSourceState = MusicSourceState.Initialized
        }
    }

    private suspend fun buildRoot(): List<MediaItem> {
        return spotifyApi?.run {
            playlists.getUserPlaylists(getUserId())
                .filterNotNull()
                .filter {
                    (it.tracks.total ?: 0) > 0
                }
                .map { playlist ->
                    val metaData = MediaMetadata.Builder()
                        .setTitle(playlist.name)
                        .setAlbumTitle(playlist.name)
                        .setIsPlayable(false)
                        .setIsBrowsable(true)
                        .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
                        .setArtworkUri(Uri.parse(playlist.uri.uri))
                        .build()

                    MediaItem.Builder()
                        .setMediaId(playlist.id)
                        .setUri(playlist.uri.uri)
                        .setMediaMetadata(metaData).build()
                }
        } ?: listOf(
            MediaItem.Builder()
                .setMediaId("unavailable")
                .setUri(Uri.EMPTY)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle("Spotify API unavailable, check app")
                        .setIsPlayable(false)
                        .setIsBrowsable(false)
                        .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
                        .setArtworkUri(Uri.EMPTY)
                        .build()
                )
                .build()
        )
    }

    suspend fun getSongsForPlaylist(id: String): List<MediaItem> {
        Timber.d("getSongsForPlaylist $id")
        return spotifyApi?.run {
            playlists.getPlaylistTracks(id).let {
                it
            }.mapNotNull { playlistTrack ->
                playlistTrack?.track?.asTrack?.let { track ->
                    val metaData = MediaMetadata.Builder().apply {
                        setTitle(track.name)
                            .setArtist(
                                track.artists.mapNotNull { artist -> artist.name }
                                    .joinToString(", ")
                            )
                        setIsPlayable(true)
                        setIsBrowsable(false)
                        setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
                    }.build()

                    MediaItem.Builder()
                        .setMediaId(track.id)
                        .setUri(track.uri.uri)
                        .setMediaMetadata(metaData).build()
                }
            }
        }?.filterNotNull() ?: listOf()
    }

    suspend fun getSong(id: String): MediaItem? {
        return spotifyApi?.run {
            tracks.getTrack(id, null)?.let { track ->
                val metaData = MediaMetadata.Builder()
                    .setTitle(track.name)
                    .setArtist(
                        track.artists.mapNotNull { artist -> artist.name }
                            .joinToString(", ")
                    )
                    .setIsPlayable(true)
                    .setIsBrowsable(false)
                    .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
                    .build()

                MediaItem.Builder()
                    .setMediaId(track.id)
                    .setUri(track.uri.uri)
                    .setMediaMetadata(metaData).build()
            }
        }
    }


    private suspend fun updateCatalog(): List<MediaItem>? {
        return spotifyApi?.run {
            playlists.getUserPlaylists(getUserId()).filterNotNull()
                .take(3)
                .map { playlist ->
                    playlists.getPlaylistTracks(playlist.id).mapNotNull { playlistTrack ->
                        playlistTrack?.track?.asTrack?.let { track ->
                            val metaData = MediaMetadata.Builder()
                                .setTitle(track.name)
                                .setArtist(
                                    track.artists.mapNotNull { artist -> artist.name }
                                        .joinToString(", ")
                                )
                                .setAlbumTitle(playlist.name)
                                .setIsPlayable(true)
                                .setIsBrowsable(false)
                                .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
                                .setArtworkUri(Uri.parse(playlist.uri.uri))
                                .build()

                            MediaItem.Builder()
                                .setMediaId(track.id)
                                .setUri(track.uri.uri)
                                .setMediaMetadata(metaData).build()
                        }
                    }
                }.flatten()
        }
    }


}