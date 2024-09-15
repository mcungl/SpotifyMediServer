package dev.emsi.mediaserver.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import dev.emsi.mediaserver.R
import dev.emsi.mediaserver.extensions.getSpotifyApi
import dev.emsi.mediaserver.extensions.isSpotifyApiAvailable
import dev.emsi.mediaserver.extensions.isSpotifyRemoteAvailable
import dev.emsi.mediaserver.ui.auth.SpotifyAuthActivity
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels<MainViewModel> {
        ViewModelProvider.AndroidViewModelFactory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.setSpotifyApiAvailable(isSpotifyApiAvailable())
        viewModel.setSpotifyRemoteAvailable(isSpotifyRemoteAvailable())

        runBlocking {
            getSpotifyApi()?.also {
                it.getUserId()
            }
        }

        setContent {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(),
            ) {
                SpotifyButtons()
            }
        }
    }

    @Composable
    fun SpotifyButtons() {
        val spotifyApiAvailable = viewModel.spotifyApiAvailable.collectAsState().value
        val spotifyRemoteAvailable = viewModel.spotifyRemoteAvailable.collectAsState().value

        Button(onClick = {
            if (spotifyApiAvailable.not()) {
                startActivity(
                    Intent(
                        this@MainActivity, SpotifyAuthActivity::class.java
                    )
                )
            }
        }, Modifier.padding(16.dp), content = {

            if (spotifyApiAvailable) {
                Text("Spotify api available")
                Spacer(modifier = Modifier.width(8.dp)) // Adjust spacing
                Image(
                    painter = painterResource(id = R.drawable.ic_ok),
                    contentDescription = null
                )
            } else {
                Text("Spotify api unavailable")
                Spacer(modifier = Modifier.width(8.dp)) // Adjust spacing
                Image(
                    painter = painterResource(id = R.drawable.ic_error),
                    contentDescription = null
                )
            }
        })
        Button(onClick = {
            if (spotifyRemoteAvailable.not()) {
                Toast.makeText(
                    this,
                    "Spotify needs to be installed to enable spotify remote playback.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }, Modifier.padding(16.dp), content = {
            if (spotifyRemoteAvailable) {
                Text("Spotify remote available")
                Spacer(modifier = Modifier.width(8.dp)) // Adjust spacing
                Image(
                    painter = painterResource(id = R.drawable.ic_ok),
                    contentDescription = null
                )
            } else {
                Text("Spotify remote unavailable")
                Spacer(modifier = Modifier.width(8.dp)) // Adjust spacing
                Image(
                    painter = painterResource(id = R.drawable.ic_error),
                    contentDescription = null
                )
            }
        })
    }
}

