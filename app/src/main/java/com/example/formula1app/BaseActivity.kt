package com.example.formula1app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity

open class BaseActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logLifecycle("onCreate")
    }

    override fun onStart() {
        super.onStart()
        logLifecycle("onStart")
    }

    override fun onResume() {
        super.onResume()
        logLifecycle("onResume")
    }

    override fun onPause() {
        super.onPause()
        logLifecycle("onPause")
    }

    override fun onStop() {
        super.onStop()
        logLifecycle("onStop")
    }

    override fun onRestart() {
        super.onRestart()
        logLifecycle("onRestart")
    }

    override fun onDestroy() {
        super.onDestroy()
        logLifecycle("onDestroy")
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        Log.d(
            "Formula1Lifecycle",
            "${javaClass.simpleName}: onWindowFocusChanged($hasFocus)"
        )
    }

    private fun logLifecycle(method: String) {
        Log.d(
            "Formula1Lifecycle",
            "${javaClass.simpleName}: $method"
        )
    }
}