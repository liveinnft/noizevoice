val char = ('a' + (i - Input.Keys.A)).toString()
currentInput += char
game.audioManager.playSound("collect", 0.3f)
}
}

// Backspace
if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE) && currentInput.isNotEmpty()) {
    currentInput = currentInput.dropLast(1)
}

// Enter - проверка ответа
if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
    checkAnswer()
}

// Escape - выход
if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
    endMiniGame(false)
}
}

private fun checkAnswer() {
    when (currentMiniGame) {
        MiniGameType.LYRIC_CREATION -> {
            if (lyricWords.any { currentInput.contains(it, ignoreCase = true) }) {
                score += 100
                endMiniGame(true)
            }
        }

        MiniGameType.WORD_ASSOCIATION -> {
            val validRhymes = listOf("колос", "волос", "полюс", "компас")
            if (validRhymes.any { currentInput.contains(it, ignoreCase = true) }) {
                score += 100
                endMiniGame(true)
            }
        }

        MiniGameType.MEMORY_PUZZLE -> {
            if (currentInput.length >= 10) { // Минимальная длина воспоминания
                score += 50
                endMiniGame(true)
            }
        }

        else -> {}
    }
}

private fun generateRhythmSequence() {
    // TODO: Реализовать ритм-последовательность
    targetText = "Повторите ритм: A-S-D-A"
}

fun render(batch: SpriteBatch) {
    if (!isActive || currentMiniGame == null) return

    batch.begin()

    // Фон мини-игры
    renderBackground(batch)

    // Заголовок
    font.color = Color.CYAN
    font.draw(
        batch,
        getMiniGameTitle(),
        0f,
        NoizeGame.GAME_HEIGHT - 100f,
        NoizeGame.GAME_WIDTH,
        Align.center,
        false
    )

    // Инструкция/вопрос
    font.color = Color.WHITE
    font.draw(
        batch,
        targetText,
        0f,
        NoizeGame.GAME_HEIGHT - 200f,
        NoizeGame.GAME_WIDTH,
        Align.center,
        true
    )

    // Пользовательский ввод
    font.color = Color.YELLOW
    font.draw(
        batch,
        "Ваш ответ: $currentInput",
        0f,
        NoizeGame.GAME_HEIGHT / 2f,
        NoizeGame.GAME_WIDTH,
        Align.center,
        false
    )

    // Таймер
    val timeLeft = (30f - gameTimer).toInt()
    font.color = if (timeLeft < 10) Color.RED else Color.LIGHT_GRAY
    font.draw(batch, "Время: $timeLeft", 50f, NoizeGame.GAME_HEIGHT - 50f)

    // Счет
    font.color = Color.GREEN
    font.draw(batch, "Очки: $score", NoizeGame.GAME_WIDTH - 200f, NoizeGame.GAME_HEIGHT - 50f)

    // Управление
    font.color = Color.LIGHT_GRAY
    font.draw(
        batch,
        "ENTER - ответить, BACKSPACE - стереть, ESC - выход",
        0f,
        100f,
        NoizeGame.GAME_WIDTH,
        Align.center,
        false
    )

    batch.end()
}

private fun renderBackground(batch: SpriteBatch) {
    // Простой градиентный фон
    batch.end()

    val shapeRenderer = com.badlogic.gdx.graphics.glutils.ShapeRenderer()
    shapeRenderer.projectionMatrix = batch.projectionMatrix
    shapeRenderer.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled)

    // Цвет фона зависит от типа мини-игры
    val bgColor = when (currentMiniGame) {
        MiniGameType.LYRIC_CREATION -> Color.DARK_GRAY
        MiniGameType.MEMORY_PUZZLE -> Color.NAVY
        MiniGameType.RHYTHM_SEQUENCE -> Color.PURPLE
        MiniGameType.WORD_ASSOCIATION -> Color.MAROON
        else -> Color.BLACK
    }

    shapeRenderer.color = bgColor
    shapeRenderer.rect(0f, 0f, NoizeGame.GAME_WIDTH, NoizeGame.GAME_HEIGHT)

    shapeRenderer.end()
    batch.begin()
}

private fun getMiniGameTitle(): String {
    return when (currentMiniGame) {
        MiniGameType.LYRIC_CREATION -> "Сочини куплет"
        MiniGameType.MEMORY_PUZZLE -> "Восстанови воспоминание"
        MiniGameType.RHYTHM_SEQUENCE -> "Повтори ритм"
        MiniGameType.WORD_ASSOCIATION -> "Игра слов"
        else -> "Мини-игра"
    }
}

private fun endMiniGame(success: Boolean) {
    isActive = false

    if (success) {
        // Награды за успешное прохождение
        game.gameStateManager.restoreEnergy(20)
        game.audioManager.playSound("ability")

        when (currentMiniGame) {
            MiniGameType.LYRIC_CREATION -> {
                game.gameStateManager.unlockMemory("created_lyric_${System.currentTimeMillis()}")
            }
            MiniGameType.MEMORY_PUZZLE -> {
                game.gameStateManager.heal(15)
            }
            MiniGameType.WORD_ASSOCIATION -> {
                game.gameStateManager.restoreEnergy(30)
            }
            else -> {}
        }

        Gdx.app.log("MiniGame", "Мини-игра завершена успешно! Счет: $score")
    } else {
        game.audioManager.playSound("hurt", 0.5f)
        Gdx.app.log("MiniGame", "Мини-игра провалена")
    }

    currentMiniGame = null
    gameTimer = 0f
    currentInput = ""
    targetText = ""
}

fun isActive(): Boolean = isActive

fun dispose() {
    font.dispose()
}
}package com.lionido.noizevoice.managers

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.Align
import com.lionido.noizevoice.NoizeGame

class MiniGameManager(private val game: NoizeGame) {

    private lateinit var font: BitmapFont
    private var isActive = false
    private var currentMiniGame: MiniGameType? = null
    private var gameTimer = 0f
    private var score = 0
    private var currentInput = ""
    private var targetText = ""

    enum class MiniGameType {
        LYRIC_CREATION,    // Сочинение куплета
        MEMORY_PUZZLE,     // Восстановление воспоминаний
        RHYTHM_SEQUENCE,   // Повторение ритм-последовательности
        WORD_ASSOCIATION   // Игра слов
    }

    // Данные для мини-игр
    private val lyricTemplates = listOf(
        "Мой голос _ через шум",
        "Свобода _ в каждой ноте",
        "Музыка _ сильнее запретов",
        "Я _ своими словами",
        "Звук _ через стены"
    )

    private val lyricWords = listOf(
        "звучит", "пробивается", "живет", "дышит", "говорю",
        "кричу", "пою", "борюсь", "лечу", "прорывается"
    )

    private val memoryFragments = listOf(
        "Первый концерт в клубе...",
        "Студия на Арбате...",
        "Запись дебютного альбома...",
        "Встреча с продюсером...",
        "Интервью на радио..."
    )

    init {
        font = BitmapFont()
        font.color = Color.WHITE
        font.data.setScale(1.5f)
    }

    fun startMiniGame(type: MiniGameType) {
        isActive = true
        currentMiniGame = type
        gameTimer = 0f
        score = 0
        currentInput = ""

        when (type) {
            MiniGameType.LYRIC_CREATION -> {
                targetText = lyricTemplates.random()
                Gdx.app.log("MiniGame", "Начинается создание куплета")
            }

            MiniGameType.MEMORY_PUZZLE -> {
                targetText = memoryFragments.random()
                Gdx.app.log("MiniGame", "Начинается восстановление воспоминания")
            }

            MiniGameType.RHYTHM_SEQUENCE -> {
                generateRhythmSequence()
                Gdx.app.log("MiniGame", "Начинается ритм-последовательность")
            }

            MiniGameType.WORD_ASSOCIATION -> {
                targetText = "Найди рифму к слову: ГОЛОС"
                Gdx.app.log("MiniGame", "Начинается игра слов")
            }
        }
    }

    fun update(deltaTime: Float) {
        if (!isActive) return

        gameTimer += deltaTime
        handleMiniGameInput()

        // Таймаут мини-игры
        if (gameTimer > 30f) {
            endMiniGame(false)
        }
    }

    private fun handleMiniGameInput() {
        // Обработка текстового ввода
        for (i in Input.Keys.A..Input.Keys.Z) {
            if (Gdx.input.isKeyJustPressed(i)) {
                val char = ('a