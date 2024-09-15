package dev.emsi.mediaserver.ui.player

import androidx.activity.ComponentActivity
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import dev.emsi.mediaserver.service.SpotifyPlayer

class PlayerActivity : ComponentActivity() {
    private var player: SpotifyPlayer? = null
    private var mediaSession: MediaSession? = null

    override fun onStart() {
        super.onStart()
        player = SpotifyPlayer(this)
        mediaSession = MediaSession.Builder(this, player!!).build()
    }

    @OptIn(UnstableApi::class)
    override fun onStop() {
        super.onStop()
        player?.release()
    }
}