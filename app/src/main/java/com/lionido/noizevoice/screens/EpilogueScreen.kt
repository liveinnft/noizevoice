package com.lionido.noizevoice.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Align
import com.lionido.noizevoice.NoizeGame
import com.lionido.noizevoice.systems.ParticleSystem

class EpilogueScreen(game: NoizeGame) : BaseScreen(game) {

    private lateinit var titleFont: BitmapFont
    private lateinit var textFont: BitmapFont
    private lateinit var particleSystem: ParticleSystem

    private var epilogueTimer = 0f
    private var currentText = 0
    private var waveTimer = 0f

    private val epilogueTexts = listOf(
        "Студия восстановлена...",
        "Голос вернулся...",
        "Музыка звучит свободно...",
        "Иван: \"Моя музыка, мои слова, моя свобода… Всё это со мной.\"",
        "Моё море хранит мои песни, мои сны и мою свободу.",
        "",
        "ПОЗДРАВЛЯЕМ!",
        "Вы завершили игру NOIZE: Битва за голос",
        "",
        "Статистика прохождения:",
        "",
        "Спасибо за игру!"
    )

    override fun show() {
        titleFont = BitmapFont()
        titleFont.color = Color.CYAN
        titleFont.data.setScale(2.5f)

        textFont = BitmapFont()
        textFont.color = Color.WHITE
        textFont.data.setScale(1.6f)

        particleSystem = ParticleSystem()

        // Спокойная финальная музыка
        game.audioManager.playMusic("moe_more")

        // Отмечаем игру как завершенную
        game.gameStateManager.isGameCompleted = true

        Gdx.app.log("Epilogue", "Игра завершена! Поздравляем!")
    }

    override fun render(delta: Float) {
        epilogueTimer += delta
        waveTimer += delta

        // Обновление систем
        particleSystem.update(delta)

        // Создание волн на море
        if (waveTimer >= 2f) {
            createSeaWaves()
            waveTimer = 0f
        }

        // Автоматическое продвижение текста
        if (epilogueTimer >= 4f && currentText < epilogueTexts.size - 1) {
            currentText++
            epilogueTimer = 0f
        }

        game.batch.begin()

        // Фон моря на закате
        renderSeaBackground()

        // Основной текст эпилога
        renderEpilogueText()

        // Статистика игры
        if (currentText >= 9) {
            renderGameStats()
        }

        // Финальные благодарности
        if (currentText >= epilogueTexts.size - 1) {
            renderCredits()
        }

        game.batch.end()

        // Эффекты частиц
        particleSystem.render(game.batch)

        handleInput()
    }

    private fun renderSeaBackground() {
        game.batch.end()

        val shapeRenderer = ShapeRenderer()
        shapeRenderer.projectionMatrix = game.batch.projectionMatrix
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // Градиент неба (закат)
        for (i in 0 until 20) {
            val y = NoizeGame.GAME_HEIGHT * (1f - i / 20f)
            val height = NoizeGame.GAME_HEIGHT / 20f
            val color = Color(
                1f,
                0.6f + i * 0.02f,
                0.2f + i * 0.04f,
                1f
            )
            shapeRenderer.color = color
            shapeRenderer.rect(0f, y, NoizeGame.GAME_WIDTH, height)
        }

        // Море
        shapeRenderer.color = Color(0.1f, 0.3f, 0.8f, 1f)
        shapeRenderer.rect(0f, 0f, NoizeGame.GAME_WIDTH, NoizeGame.GAME_HEIGHT * 0.4f)

        // Волны
        renderWaves(shapeRenderer)

        shapeRenderer.end()
        game.batch.begin()
    }

    private fun renderWaves(renderer: ShapeRenderer) {
        renderer.color = Color(0.2f, 0.4f, 0.9f, 0.8f)

        for (i in 0 until 5) {
            val waveY = NoizeGame.GAME_HEIGHT * 0.1f + i * 30f
            val waveOffset = MathUtils.sin(waveTimer + i) * 20f

            for (x in 0 until NoizeGame.GAME_WIDTH.toInt() step 40) {
                val waveHeight = MathUtils.sin((x + waveOffset) * 0.01f) * 10f
                renderer.rect(x.toFloat(), waveY + waveHeight, 40f, 15f)
            }
        }
    }

    private fun createSeaWaves() {
        // Эффекты частиц для волн
        for (i in 0 until 5) {
            val x = MathUtils.random(0f, NoizeGame.GAME_WIDTH)
            particleSystem.createEnergyBurst(x, NoizeGame.GAME_HEIGHT * 0.2f)
        }
    }

    private fun renderEpilogueText() {
        if (currentText < epilogueTexts.size) {
            val text = epilogueTexts[currentText]

            when {
                text.startsWith("Иван:") -> {
                    textFont.color = Color.CYAN
                }
                text == "ПОЗДРАВЛЯЕМ!" -> {
                    titleFont.color = Color.GOLD
                    titleFont.draw(
                        game.batch,
                        text,
                        0f,
                        NoizeGame.GAME_HEIGHT * 0.7f,
                        NoizeGame.GAME_WIDTH,
                        Align.center,
                        false
                    )
                    return
                }
                text.isEmpty() -> return
                else -> {
                    textFont.color = Color.WHITE
                }
            }

            textFont.draw(
                game.batch,
                text,
                0f,
                NoizeGame.GAME_HEIGHT * 0.6f,
                NoizeGame.GAME_WIDTH,
                Align.center,
                true
            )
        }
    }

    private fun renderGameStats() {
        textFont.color = Color.YELLOW

        val stats = listOf(
            "Собрано кассет: ${game.gameStateManager.collectedCassettes.size}/6",
            "Разблокировано воспоминаний: ${game.gameStateManager.unlockedMemories.size}",
            "Прочитано дневников: ${game.gameStateManager.readDiaries.size}",
            "Время игры: ${(game.gameStateManager.playTime / 60).toInt()} минут",
            "Завершено уровней: ${game.gameStateManager.completedLevels.size}"
        )

        for (i in stats.indices) {
            textFont.draw(
                game.batch,
                stats[i],
                0f,
                NoizeGame.GAME_HEIGHT * 0.45f - i * 40f,
                NoizeGame.GAME_WIDTH,
                Align.center,
                false
            )
        }
    }

    private fun renderCredits() {
        textFont.color = Color.LIGHT_GRAY
        textFont.draw(
            game.batch,
            "Игра создана с уважением к творчеству Ивана Алексеева (Noize MC)",
            0f,
            150f,
            NoizeGame.GAME_WIDTH,
            Align.center,
            true
        )

        textFont.color = Color.CYAN
        textFont.draw(
            game.batch,
            "ПРОБЕЛ - в главное меню",
            0f,
            80f,
            NoizeGame.GAME_WIDTH,
            Align.center,
            false
        )
    }

    private fun handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.justTouched()) {
            if (currentText < epilogueTexts.size - 1) {
                currentText = epilogueTexts.size - 1
                epilogueTimer = 0f
            } else {
                game.setScreen(MainMenuScreen(game))
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(MainMenuScreen(game))
        }
    }

    override fun dispose() {
        titleFont.dispose()
        textFont.dispose()
    }
}