package com.lionido.noizevoice.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.utils.Align
import com.lionido.noizevoice.NoizeGame

class PrologueScreen(game: NoizeGame) : BaseScreen(game) {

    private lateinit var font: BitmapFont
    private var dialogueTimer = 0f
    private var currentDialogue = 0

    private val dialogues = listOf(
        "Студия разрушена...",
        "Поломанные колонки, разбросанные инструменты...",
        "Мои песни... Где мои песни?",
        "Иван: \"Студия разрушена… мои песни…\"",
        "Тень: \"Ты думал, что они твои? Без голоса ты никто.\"",
        "Иван: \"Верну всё. Свою музыку, свою свободу.\"",
        "Нажмите ПРОБЕЛ, чтобы начать путешествие"
    )

    override fun show() {
        font = BitmapFont()
        font.color = Color.WHITE
        font.data.setScale(1.8f)

        // Тихая, атмосферная версия Make Some Noize
        game.audioManager.playMusic("make_some_noize")

        Gdx.app.log("Prologue", "Пролог начался")
    }

    override fun render(delta: Float) {
        dialogueTimer += delta
        handleInput()

        game.batch.begin()

        // Фон пролога (тёмный, разрушенная студия)
        drawBackground()

        // Диалог
        if (currentDialogue < dialogues.size) {
            val dialogue = dialogues[currentDialogue]

            // Особое выделение для реплик персонажей
            if (dialogue.startsWith("Иван:")) {
                font.color = Color.CYAN
            } else if (dialogue.startsWith("Тень:")) {
                font.color = Color.RED
            } else {
                font.color = Color.WHITE
            }

            font.draw(
                game.batch,
                dialogue,
                50f,
                NoizeGame.GAME_HEIGHT * 0.3f,
                NoizeGame.GAME_WIDTH - 100f,
                Align.left,
                true
            )
        }

        // Инструкция по управлению
        if (currentDialogue >= dialogues.size - 1) {
            font.color = Color.YELLOW
            font.draw(
                game.batch,
                "ПРОБЕЛ - продолжить",
                0f,
                100f,
                NoizeGame.GAME_WIDTH,
                Align.center,
                false
            )
        }

        game.batch.end()

        // Автоматическое продвижение диалога
        if (dialogueTimer >= 2f && currentDialogue < dialogues.size - 1) {
            currentDialogue++
            dialogueTimer = 0f
        }
    }

    private fun drawBackground() {
        // Простой тёмный фон (пока без спрайтов)
        // TODO: Добавить спрайты разрушенной студии
    }

    private fun handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.justTouched()) {
            if (currentDialogue < dialogues.size - 1) {
                currentDialogue++
                dialogueTimer = 0f
                game.audioManager.playSound("collect", 0.3f)
            } else {
                // Переход к первому уровню
                game.setScreen(GameplayScreen(game, "act1_level1"))
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(MainMenuScreen(game))
        }
    }

    override fun dispose() {
        font.dispose()
    }
}