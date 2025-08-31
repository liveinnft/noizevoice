package com.lionido.noizevoice.systems

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Align
import com.lionido.noizevoice.NoizeGame

class DialogueSystem(private val game: NoizeGame) {

    private lateinit var nameFont: BitmapFont
    private lateinit var textFont: BitmapFont
    private var isActive = false
    private var currentDialogue: Dialogue? = null
    private var currentLineIndex = 0
    private var typewriterTimer = 0f
    private var visibleCharacters = 0
    private val typewriterSpeed = 50f // символов в секунду

    data class Dialogue(
        val id: String,
        val lines: List<DialogueLine>,
        val backgroundMusic: String? = null,
        val onComplete: (() -> Unit)? = null
    )

    data class DialogueLine(
        val speaker: String,
        val text: String,
        val emotion: Emotion = Emotion.NEUTRAL,
        val soundEffect: String? = null
    )

    enum class Emotion {
        NEUTRAL, HAPPY, SAD, ANGRY, SURPRISED, THOUGHTFUL
    }

    // Предопределенные диалоги из ТЗ
    private val dialogues = mapOf(
        "prologue_intro" to Dialogue(
            "prologue_intro",
            listOf(
                DialogueLine("", "Студия разрушена...", Emotion.SAD),
                DialogueLine("", "Поломанные колонки, разбросанные инструменты...", Emotion.THOUGHTFUL),
                DialogueLine("Иван", "Студия разрушена… мои песни…", Emotion.SAD),
                DialogueLine("Тень", "Ты думал, что они твои? Без голоса ты никто.", Emotion.ANGRY),
                DialogueLine("Иван", "Верну всё. Свою музыку, свою свободу.", Emotion.ANGRY)
            ),
            "make_some_noize"
        ),

        "act1_street" to Dialogue(
            "act1_street",
            listOf(
                DialogueLine("Фанат", "Иван, они запрещают твои концерты!", Emotion.SURPRISED),
                DialogueLine("Иван", "Музыка сильнее любых запретов.", Emotion.THOUGHTFUL)
            )
        ),

        "act2_dorm" to Dialogue(
            "act2_dorm",
            listOf(
                DialogueLine("Друг", "Помнишь, как мы мечтали о сцене?", Emotion.HAPPY),
                DialogueLine("Иван", "Каждая песня — это мой голос. Я хочу, чтобы мир услышал.", Emotion.THOUGHTFUL)
            ),
            "vselennaya"
        ),

        "act3_silence" to Dialogue(
            "act3_silence",
            listOf(
                DialogueLine("Иван", "Я не слышу себя… Голоса нет.", Emotion.SAD),
                DialogueLine("Тень", "Твоя музыка исчезает вместе с твоей уверенностью.", Emotion.ANGRY)
            ),
            "vidihay"
        ),

        "act5_system" to Dialogue(
            "act5_system",
            listOf(
                DialogueLine("Иван", "Каждая песня, каждая история — это мой путь.", Emotion.THOUGHTFUL)
            )
        ),

        "final_barkov" to Dialogue(
            "final_barkov",
            listOf(
                DialogueLine("Барков", "Ты мог молчать и следовать правилам.", Emotion.ANGRY),
                DialogueLine("Иван", "Я буду звучать. Я буду слышимым.", Emotion.ANGRY)
            ),
            "mercedes"
        ),

        "epilogue_sea" to Dialogue(
            "epilogue_sea",
            listOf(
                DialogueLine("Иван", "Моя музыка, мои слова, моя свобода… Всё это со мной.", Emotion.HAPPY),
                DialogueLine("", "Моё море хранит мои песни, мои сны и мою свободу.", Emotion.THOUGHTFUL)
            ),
            "moe_more"
        )
    )

    init {
        nameFont = BitmapFont()
        nameFont.color = Color.CYAN
        nameFont.data.setScale(1.8f)

        textFont = BitmapFont()
        textFont.color = Color.WHITE
        textFont.data.setScale(1.4f)
    }

    fun startDialogue(dialogueId: String) {
        currentDialogue = dialogues[dialogueId]
        currentDialogue?.let { dialogue ->
            isActive = true
            currentLineIndex = 0
            typewriterTimer = 0f
            visibleCharacters = 0

            // Запуск фоновой музыки диалога
            dialogue.backgroundMusic?.let { music ->
                game.audioManager.playMusic(music)
            }

            game.gameStateManager.watchCutscene(dialogueId)
            Gdx.app.log("DialogueSystem", "Начинается диалог: $dialogueId")
        }
    }

    fun update(deltaTime: Float) {
        if (!isActive || currentDialogue == null) return

        val currentLine = getCurrentLine() ?: return

        // Эффект печатной машинки
        typewriterTimer += deltaTime
        val targetCharacters = (typewriterTimer * typewriterSpeed).toInt()
        visibleCharacters = targetCharacters.coerceAtMost(currentLine.text.length)

        handleInput()
    }

    private fun handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) ||
            Gdx.input.isKeyJustPressed(Input.Keys.ENTER) ||
            Gdx.input.justTouched()) {

            val currentLine = getCurrentLine() ?: return

            if (visibleCharacters < currentLine.text.length) {
                // Ускорить печать
                visibleCharacters = currentLine.text.length
                typewriterTimer = currentLine.text.length / typewriterSpeed
            } else {
                // Переход к следующей реплике
                nextLine()
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            skipDialogue()
        }
    }

    private fun nextLine() {
        currentDialogue?.let { dialogue ->
            currentLineIndex++

            if (currentLineIndex >= dialogue.lines.size) {
                endDialogue()
            } else {
                typewriterTimer = 0f
                visibleCharacters = 0

                // Звуковой эффект для новой реплики
                val currentLine = getCurrentLine()
                currentLine?.soundEffect?.let { sound ->
                    game.audioManager.playSound(sound)
                }
            }
        }
    }

    private fun endDialogue() {
        isActive = false
        currentDialogue?.onComplete?.invoke()
        currentDialogue = null
        currentLineIndex = 0

        Gdx.app.log("DialogueSystem", "Диалог завершен")
    }

    private fun skipDialogue() {
        endDialogue()
    }

    private fun getCurrentLine(): DialogueLine? {
        return currentDialogue?.lines?.getOrNull(currentLineIndex)
    }

    fun render(batch: SpriteBatch) {
        if (!isActive || currentDialogue == null) return

        val currentLine = getCurrentLine() ?: return

        // Фон диалогового окна
        batch.end()
        val shapeRenderer = ShapeRenderer()
        shapeRenderer.projectionMatrix = batch.projectionMatrix
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // Полупрозрачная подложка
        shapeRenderer.color = Color(0f, 0f, 0f, 0.8f)
        shapeRenderer.rect(0f, 0f, NoizeGame.GAME_WIDTH, NoizeGame.GAME_HEIGHT * 0.3f)

        // Рамка диалога
        shapeRenderer.color = getCharacterColor(currentLine.speaker)
        shapeRenderer.rect(20f, 20f, NoizeGame.GAME_WIDTH - 40f, NoizeGame.GAME_HEIGHT * 0.25f)

        // Внутренний фон
        shapeRenderer.color = Color(0.1f, 0.1f, 0.1f, 0.9f)
        shapeRenderer.rect(25f, 25f, NoizeGame.GAME_WIDTH - 50f, NoizeGame.GAME_HEIGHT * 0.25f - 10f)

        shapeRenderer.end()
        batch.begin()

        // Имя персонажа
        if (currentLine.speaker.isNotEmpty()) {
            nameFont.color = getCharacterColor(currentLine.speaker)
            nameFont.draw(batch, currentLine.speaker, 50f, NoizeGame.GAME_HEIGHT * 0.25f - 20f)
        }

        // Текст диалога с эффектом печатной машинки
        textFont.color = getEmotionColor(currentLine.emotion)
        val visibleText = currentLine.text.take(visibleCharacters)
        textFont.draw(
            batch,
            visibleText,
            50f,
            NoizeGame.GAME_HEIGHT * 0.2f,
            NoizeGame.GAME_WIDTH - 100f,
            Align.left,
            true
        )

        // Индикатор продолжения
        if (visibleCharacters >= currentLine.text.length) {
            textFont.color = Color.YELLOW
            textFont.draw(batch, "ПРОБЕЛ - продолжить", NoizeGame.GAME_WIDTH - 250f, 50f)
        }

        // Индикатор пропуска
        textFont.color = Color.LIGHT_GRAY
        textFont.draw(batch, "ESC - пропустить", 50f, 50f)
    }

    private fun getCharacterColor(speaker: String): Color {
        return when (speaker) {
            "Иван" -> Color.CYAN
            "Тень" -> Color.RED
            "Барков" -> Color.PURPLE
            "Фанат" -> Color.GREEN
            "Друг" -> Color.ORANGE
            "Protivo Gunz" -> Color.YELLOW
            else -> Color.WHITE
        }
    }

    private fun getEmotionColor(emotion: Emotion): Color {
        return when (emotion) {
            Emotion.HAPPY -> Color.LIGHT_GRAY
            Emotion.SAD -> Color.BLUE
            Emotion.ANGRY -> Color.RED
            Emotion.SURPRISED -> Color.YELLOW
            Emotion.THOUGHTFUL -> Color.PURPLE
            else -> Color.WHITE
        }
    }

    fun isActive(): Boolean = isActive

    fun createCustomDialogue(
        id: String,
        lines: List<DialogueLine>,
        backgroundMusic: String? = null,
        onComplete: (() -> Unit)? = null
    ): Dialogue {
        return Dialogue(id, lines, backgroundMusic, onComplete)
    }

    fun dispose() {
        nameFont.dispose()
        textFont.dispose()
    }
}package com.lionido.noizevoice.systems

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Align
import com.lionido.noizevoice.NoizeGame

class DialogueSystem(private val game: NoizeGame) {

    private lateinit var nameFont: BitmapFont
    private lateinit var textFont: BitmapFont
    private var isActive = false
    private var currentDialogue: Dialogue? = null
    private var currentLineIndex = 0
    private var typewriterTimer = 0f
    private var visibleCharacters = 0
    private val typewriterSpeed = 50f // символов в секунду

    data class Dialogue(
        val id: String,
        val lines: List<DialogueLine>,
        val background: