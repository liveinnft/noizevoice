package com.lionido.noizevoice.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.utils.Align
import com.lionido.noizevoice.NoizeGame

class SplashScreen(game: NoizeGame) : BaseScreen(game) {

    private var splashTimer = 0f
    private val splashDuration = 3f
    private lateinit var font: BitmapFont

    override fun show() {
        font = BitmapFont()
        font.color = Color.WHITE
        font.data.setScale(2f)

        // Запуск главного трека
        game.audioManager.playMusic("make_some_noize")

        Gdx.app.log("SplashScreen", "NOIZE: Битва за голос - Загрузка...")
    }

    override fun render(delta: Float) {
        splashTimer += delta

        game.batch.begin()

        // Заголовок игры
        font.draw(
            game.batch,
            "NOIZE: Битва за голос",
            0f,
            NoizeGame.GAME_HEIGHT * 0.6f,
            NoizeGame.GAME_WIDTH,
            Align.center,
            false
        )

        // Подзаголовок
        font.draw(
            game.batch,
            "История Ивана Алексеева",
            0f,
            NoizeGame.GAME_HEIGHT * 0.5f,
            NoizeGame.GAME_WIDTH,
            Align.center,
            false
        )

        // Индикатор загрузки
        val loadingText = if (splashTimer < 1f) "Загрузка."
        else if (splashTimer < 2f) "Загрузка.."
        else "Загрузка..."

        font.draw(
            game.batch,
            loadingText,
            0f,
            NoizeGame.GAME_HEIGHT * 0.3f,
            NoizeGame.GAME_WIDTH,
            Align.center,
            false
        )

        // Информация о разработке
        font.draw(
            game.batch,
            "Нажмите любую клавишу для продолжения",
            0f,
            NoizeGame.GAME_HEIGHT * 0.2f,
            NoizeGame.GAME_WIDTH,
            Align.center,
            false
        )

        game.batch.end()

        // Переход к главному меню
        if (splashTimer >= splashDuration || Gdx.input.justTouched() || Gdx.input.isKeyJustPressed(-1)) {
            game.setScreen(MainMenuScreen(game))
        }
    }

    override fun dispose() {
        font.dispose()
    }
}