package com.lionido.noizevoice.screens

import com.badlogic.gdx.Screen
import com.lionido.noizevoice.NoizeGame

abstract class BaseScreen(protected val game: NoizeGame) : Screen {

    override fun show() {
        // Переопределяется в наследниках
    }

    override fun render(delta: Float) {
        // Переопределяется в наследниках
    }

    override fun resize(width: Int, height: Int) {
        game.viewport.update(width, height)
    }

    override fun pause() {
        game.audioManager.pauseMusic()
    }

    override fun resume() {
        game.audioManager.resumeMusic()
    }

    override fun hide() {
        // Переопределяется в наследниках
    }

    override fun dispose() {
        // Переопределяется в наследниках
    }
}