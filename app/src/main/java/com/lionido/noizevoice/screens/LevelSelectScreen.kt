package com.lionido.noizevoice.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Circle
import com.badlogic.gdx.utils.Align
import com.lionido.noizevoice.NoizeGame
import com.lionido.noizevoice.managers.LevelManager

class LevelSelectScreen(game: NoizeGame) : BaseScreen(game) {

    private lateinit var titleFont: BitmapFont
    private lateinit var textFont: BitmapFont
    private lateinit var levelManager: LevelManager
    private var selectedAct = 1
    private var selectedLevel = 0

    private val actNames = mapOf(
        1 to "Улицы города",
        2 to "Общежитие РГГУ",
        3 to "Беззвучная зона",
        4 to "Дорога",
        5 to "Узел системы",
        6 to "Финал",
        7 to "Эпилог"
    )

    override fun show() {
        titleFont = BitmapFont()
        titleFont.color = Color.CYAN
        titleFont.data.setScale(2.2f)

        textFont = BitmapFont()
        textFont.color = Color.WHITE
        textFont.data.setScale(1.3f)

        levelManager = LevelManager(game)

        // Музыка в зависимости от выбранного акта
        updateBackgroundMusic()
    }

    override fun render(delta: Float) {
        handleInput()

        game.batch.begin()

        // Заголовок
        titleFont.draw(
            game.batch,
            "Выбор уровня",
            0f,
            NoizeGame.GAME_HEIGHT - 50f,
            NoizeGame.GAME_WIDTH,
            Align.center,
            false
        )

        // Название текущего акта
        textFont.color = Color.YELLOW
        textFont.draw(
            game.batch,
            "Акт $selectedAct: ${actNames[selectedAct]}",
            0f,
            NoizeGame.GAME_HEIGHT - 150f,
            NoizeGame.GAME_WIDTH,
            Align.center,
            false
        )

        // Уровни текущего акта
        renderLevels()

        // Навигация по актам
        renderActNavigation()

        // Статистика
        renderStats()

        // Управление
        textFont.color = Color.LIGHT_GRAY
        textFont.draw(
            game.batch,
            "Стрелки - навигация, ENTER - играть, ESC - назад",
            0f,
            50f,
            NoizeGame.GAME_WIDTH,
            Align.center,
            false
        )

        game.batch.end()

        // Рендер индикаторов уровней
        renderLevelIndicators()
    }

    private fun renderLevels() {
        val levelsInAct = levelManager.getLevelsInAct(selectedAct)

        if (levelsInAct.isEmpty()) {
            textFont.color = Color.GRAY
            textFont.draw(
                game.batch,
                "Нет доступных уровней",
                0f,
                NoizeGame.GAME_HEIGHT / 2f,
                NoizeGame.GAME_WIDTH,
                Align.center,
                false
            )
            return
        }

        for (i in levelsInAct.indices) {
            val level = levelsInAct[i]
            val isSelected = i == selectedLevel
            val isUnlocked = levelManager.isLevelUnlocked(level.id)
            val isCompleted = game.gameStateManager.completedLevels.contains(level.id)

            val y = NoizeGame.GAME_HEIGHT - 250f - i * 80f

            // Название уровня
            textFont.color = when {
                !isUnlocked -> Color.DARK_GRAY
                isCompleted -> Color.GREEN
                isSelected -> Color.YELLOW
                else -> Color.WHITE
            }

            val levelName = if (isUnlocked) level.name else "???"
            textFont.draw(game.batch, levelName, 100f, y)

            // Статус
            if (isUnlocked) {
                textFont.color = Color.LIGHT_GRAY
                val status = if (isCompleted) "✓ Завершен" else "Доступен"
                textFont.draw(game.batch, status, 400f, y)
            }
        }
    }

    private fun renderLevelIndicators() {
        val levelsInAct = levelManager.getLevelsInAct(selectedAct)

        game.batch.end()
        val shapeRenderer = ShapeRenderer()
        shapeRenderer.projectionMatrix = game.batch.projectionMatrix
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        for (i in levelsInAct.indices) {
            val level = levelsInAct[i]
            val isSelected = i == selectedLevel
            val isUnlocked = levelManager.isLevelUnlocked(level.id)
            val isCompleted = game.gameStateManager.completedLevels.contains(level.id)

            val x = 50f
            val y = NoizeGame.GAME_HEIGHT - 270f - i * 80f

            val color = when {
                !isUnlocked -> Color.DARK_GRAY
                isCompleted -> Color.GREEN
                isSelected -> Color.YELLOW
                else -> Color.WHITE
            }

            renderer.color = color
            renderer.circle(x, y, if (isSelected) 12f else 8f)
        }

        shapeRenderer.end()
        game.batch.begin()
    }

    private fun renderActNavigation() {
        textFont.color = Color.CYAN
        textFont.draw(game.batch, "< Акт", 100f, NoizeGame.GAME_HEIGHT - 120f)
        textFont.draw(game.batch, "Акт >", NoizeGame.GAME_WIDTH - 150f, NoizeGame.GAME_HEIGHT - 120f)
    }

    private fun renderStats() {
        val progress = (game.gameStateManager.getProgress() * 100).toInt()

        textFont.color = Color.LIGHT_GRAY
        textFont.draw(
            game.batch,
            "Общий прогресс: $progress%",
            50f,
            150f
        )

        textFont.draw(
            game.batch,
            game.gameStateManager.getCassetteProgress(),
            50f,
            120f
        )

        textFont.draw(
            game.batch,
            "Время игры: ${(game.gameStateManager.playTime / 60).toInt()} мин",
            50f,
            90f
        )
    }

    private fun handleInput() {
        val levelsInAct = levelManager.getLevelsInAct(selectedAct)

        // Навигация по уровням
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            selectedLevel = (selectedLevel - 1 + levelsInAct.size) % levelsInAct.size
            game.audioManager.playSound("collect", 0.5f)
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            selectedLevel = (selectedLevel + 1) % levelsInAct.size
            game.audioManager.playSound("collect", 0.5f)
        }

        // Навигация по актам
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            selectedAct = (selectedAct - 1).coerceAtLeast(1)
            selectedLevel = 0
            updateBackgroundMusic()
            game.audioManager.playSound("ability", 0.7f)
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            selectedAct = (selectedAct + 1).coerceAtMost(7)
            selectedLevel = 0
            updateBackgroundMusic()
            game.audioManager.playSound("ability", 0.7f)
        }

        // Запуск уровня
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            if (levelsInAct.isNotEmpty()) {
                val level = levelsInAct[selectedLevel]
                if (levelManager.isLevelUnlocked(level.id)) {
                    game.setScreen(GameplayScreen(game, level.id))
                }
            }
        }

        // Назад
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(MainMenuScreen(game))
        }
    }

    private fun updateBackgroundMusic() {
        val music = when (selectedAct) {
            1 -> "make_some_noize"
            2 -> "vselennaya"
            3 -> "vidihay"
            4 -> "igra_slov"
            5, 6 -> "mercedes"
            7 -> "moe_more"
            else -> "make_some_noize"
        }
        game.audioManager.playMusic(music)
    }

    override fun dispose() {
        titleFont.dispose()
        textFont.dispose()
    }
}