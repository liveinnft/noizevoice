package com.lionido.noizevoice

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.viewport.FitViewport
import com.lionido.noizevoice.managers.AudioManager
import com.lionido.noizevoice.managers.GameStateManager
import com.lionido.noizevoice.screens.SplashScreen

class NoizeGame : Game() {
    companion object {
        const val GAME_WIDTH = 1920f
        const val GAME_HEIGHT = 1080f
        const val PPM = 100f // Pixels per meter
    }

    lateinit var batch: SpriteBatch
    lateinit var camera: OrthographicCamera
    lateinit var viewport: FitViewport
    lateinit var audioManager: AudioManager
    lateinit var gameStateManager: GameStateManager

    override fun create() {
        // Инициализация графики
        batch = SpriteBatch()
        camera = OrthographicCamera()
        viewport = FitViewport(GAME_WIDTH, GAME_HEIGHT, camera)

        // Инициализация менеджеров
        audioManager = AudioManager()
        gameStateManager = GameStateManager(this)

        // Запуск с экрана заставки
        setScreen(SplashScreen(this))

        Gdx.app.log("NoizeGame", "Игра инициализирована")
    }

    override fun render() {
        // Очистка экрана
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        // Обновление камеры
        camera.update()
        batch.projectionMatrix = camera.combined

        super.render()
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
        super.resize(width, height)
    }

    override fun dispose() {
        batch.dispose()
        audioManager.dispose()
        super.dispose()
    }
}