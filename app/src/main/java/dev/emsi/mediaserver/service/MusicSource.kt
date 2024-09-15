package dev.emsi.mediaserver.service

import android.os.ConditionVariable
import android.util.Log
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.ListeningExecutorService
import com.google.common.util.concurrent.MoreExecutors
import timber.log.Timber
import java.util.concurrent.Executors


interface MusicSource {
    val executorService: ListeningExecutorService
    fun whenReady(performAction: (Boolean) -> Unit): Boolean

    /** Returns a function that opens the condition variable when called. */
    fun openWhenReady(conditionVariable: ConditionVariable): (Boolean) -> Unit = {
        val successfullyInitialized = it
        if (!successfullyInitialized) {
            Log.e(TAG, "loading music source failed")
        }
        conditionVariable.open()
    }

    /**
     * Returns a future that executes the action when the music source is ready. This may be an
     * immediate execution if the music source is ready, or a deferred asynchronous execution if the
     * music source is still loading.
     *
     * @param action The function to be called when the music source is ready.
     */
    fun <T> callWhenMusicSourceReady(action: () -> T): ListenableFuture<T> {
        val conditionVariable = ConditionVariable()
        return if (whenReady(openWhenReady(conditionVariable))) {
            Futures.immediateFuture(action())
        } else {
            executorService.submit<T> {
                Timber.d("block ${Thread.currentThread()}")

                conditionVariable.block()
                action()
            }
        }
    }

}

sealed class MusicSourceState {
    data object Created : MusicSourceState()
    data object Initializing : MusicSourceState()
    data object Initialized : MusicSourceState()
    data class Error(val error: Throwable) : MusicSourceState()
}

/**
 * Base class for music sources in UAMP.
 */
abstract class AbstractMusicSource : MusicSource {


    override val executorService: ListeningExecutorService by lazy {
        MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor())
    }

    var musicSourceState: MusicSourceState = MusicSourceState.Created
        set(value) {
            Timber.d("State change $value on ${Thread.currentThread()}")
            if (value == MusicSourceState.Initialized || value is MusicSourceState.Error) {
                synchronized(onReadyListeners) {
                    field = value
                    onReadyListeners.forEach { listener ->
                        listener(musicSourceState == MusicSourceState.Initialized)
                    }
                }
            } else {
                field = value
            }
        }

    private val onReadyListeners = mutableListOf<(Boolean) -> Unit>()


    override fun whenReady(performAction: (Boolean) -> Unit): Boolean =
        when (musicSourceState) {
            MusicSourceState.Created, MusicSourceState.Initializing -> {
                onReadyListeners += performAction
                false
            }

            else -> {
                performAction((musicSourceState is MusicSourceState.Error).not())
                true
            }
        }

}


private const val TAG = "MusicSource"


