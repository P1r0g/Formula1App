package com.example.formula1app

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner

class ScreenLifecycleLogger {

    private val observers =
        mutableMapOf<Lifecycle, LifecycleEventObserver>()

    @Composable
    fun Observe(screenName: String) {
        val lifecycle = LocalLifecycleOwner.current.lifecycle

        SideEffect {
            if (
                lifecycle.currentState != Lifecycle.State.DESTROYED &&
                !observers.containsKey(lifecycle)
            ) {
                val observer = object : LifecycleEventObserver {

                    override fun onStateChanged(
                        source: LifecycleOwner,
                        event: Lifecycle.Event
                    ) {
                        Log.d(
                            TAG,
                            "$screenName: ${event.name}"
                        )

                        if (event == Lifecycle.Event.ON_DESTROY) {
                            source.lifecycle.removeObserver(this)
                            observers.remove(source.lifecycle)
                        }
                    }
                }

                observers[lifecycle] = observer
                lifecycle.addObserver(observer)
            }
        }
    }

    private companion object {
        const val TAG = "Formula1Lifecycle"
    }
}