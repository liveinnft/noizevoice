package com.lionido.noizevoice

import android.os.Bundle
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration

class MainActivity : AndroidApplication() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val config = AndroidApplicationConfiguration().apply {
            useAccelerometer = false
            useCompass = false
            useWakelock = true
            hideStatusBar = true
            useImmersiveMode = true
        }

        initialize(NoizeGame(), config)
    }
}