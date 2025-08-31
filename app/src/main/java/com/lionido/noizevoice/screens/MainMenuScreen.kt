package com.lionido.noizevoice.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.utils.Align
import com.lionido.noizevoice.NoizeGame

class MainMenuScreen(game: NoizeGame) : BaseScreen(game) {

    private lateinit var titleFont: BitmapFont
    private lateinit var menuFont: BitmapFont
    private var selectedOption = 0
    private val menuOptions = listOf(
        "Новая игра",
        "Продолжить",
        "Коллекция кассет",
        "Настройки",
        "Об игре",
        "Выход"
    )

    override fun show() {
        titleFont = BitmapFont()
        titleFont.color = Color.CYAN
        titleFont.data.setScale(3f)

        menuFont = BitmapFont()
        menuFont.color = Color.WHITE
        menuFont.data.setScale(1.5f)

        // Фоновая музыка меню
        game.audioManager.playMusic("make_some_noize")

        Gdx.app.log("MainMenu", "Главное меню загружено")
    }

    override fun render(delta: Float) {
        handleInput()

        game.batch.begin()

        // Заголовок
        titleFont.draw(
            game.batch,
            "NOIZE",
            0f,
            NoizeGame.GAME_HEIGHT * 0.8f,
            NoizeGame.GAME_WIDTH,
            Align.center,
            false
        )

        titleFont.draw(
            game.batch,
            "Битва за голос",
            0f,
            NoizeGame.GAME_HEIGHT * 0.7f,
            NoizeGame.GAME_WIDTH,
            Align.center,
            false
        )

        // Меню
        for (i in menuOptions.indices) {
            val color = if (i == selectedOption) Color.YELLOW else Color.WHITE
            menuFont.color = color

            menuFont.draw(
                game.batch,
                menuOptions[i],
                0f,
                NoizeGame.GAME_HEIGHT * 0.5f - i * 60f,
                NoizeGame.GAME_WIDTH,
                Align.center,
                false
            )
        }

        // Информация о прогрессе
        if (game.gameStateManager.collectedCassettes.isNotEmpty()) {
            menuFont.color = Color.LIGHT_GRAY
            menuFont.draw(
                game.batch,
                "Прогресс: ${game.gameStateManager.getCassetteProgress()}",
                50f,
                100f
            )

            menuFont.draw(
                game.batch,
                "Воспоминания: ${game.gameStateManager.getMemoryProgress()}",
                50f,
                60f
            )
        }

        game.batch.end()
    }

    private fun handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            selectedOption = (selectedOption - 1 + menuOptions.size) % menuOptions.size
            game.audioManager.playSound("collect", 0.5f)
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            selectedOption = (selectedOption + 1) % menuOptions.size
            game.audioManager.playSound("collect", 0.5f)
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            selectOption()
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (selectedOption != menuOptions.size - 1) {
                selectedOption = menuOptions.size - 1 // Выход
            }
        }
    }

    private fun selectOption() {
        game.audioManager.playSound("ability")

        when (selectedOption) {
            0 -> { // Новая игра
                game.gameStateManager = GameStateManager(game) // Сброс прогресса
                game.setScreen(PrologueScreen(game))
            }
            1 -> { // Продолжить
                if (game.gameStateManager.completedLevels.isNotEmpty()) {
                    // Загрузка последнего уровня
                    game.setScreen(GameplayScreen(game, "last_level"))
                } else {
                    game.setScreen(PrologueScreen(game))
                }
            }
            2 -> { // Коллекция кассет
                game.setScreen(CassetteCollectionScreen(game))
            }
            3 -> { // Настройки
                game.setScreen(SettingsScreen(game))
            }
            4 -> { // Об игре
                game.setScreen(AboutScreen(game))
            }
            5 -> { // Выход
                Gdx.app.exit()
            }
        }
    }

    override fun dispose() {
        titleFont.dispose()
        menuFont.dispose()
    }
}