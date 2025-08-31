package com.lionido.noizevoice.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.utils.Align
import com.lionido.noizevoice.NoizeGame
import com.lionido.noizevoice.managers.GameStateManager

class CassetteCollectionScreen(game: NoizeGame) : BaseScreen(game) {

    private lateinit var titleFont: BitmapFont
    private lateinit var textFont: BitmapFont
    private var selectedTrack = 0

    private val allTracks = listOf(
        TrackInfo("make_some_noize", "Make Some Noize", "Первый трек, с которого всё началось"),
        TrackInfo("vidihay", "Выдыхай", "Способность к стелсу и восстановлению"),
        TrackInfo("vselennaya", "Вселенная бесконечна?", "Философские размышления и телепортация"),
        TrackInfo("igra_slov", "Игра слов", "Мастерство ритма и словесных атак"),
        TrackInfo("moe_more", "Моё море", "Личная свобода и внутренний покой"),
        TrackInfo("mercedes", "Mercedes S666", "Противостояние системе")
    )

    data class TrackInfo(
        val id: String,
        val title: String,
        val description: String
    )

    override fun show() {
        titleFont = BitmapFont()
        titleFont.color = Color.CYAN
        titleFont.data.setScale(2.5f)

        textFont = BitmapFont()
        textFont.color = Color.WHITE
        textFont.data.setScale(1.3f)

        // Тихая фоновая музыка
        game.audioManager.playMusic("moe_more")
    }

    override fun render(delta: Float) {
        handleInput()

        game.batch.begin()

        // Заголовок
        titleFont.draw(
            game.batch,
            "Коллекция кассет",
            0f,
            NoizeGame.GAME_HEIGHT - 50f,
            NoizeGame.GAME_WIDTH,
            Align.center,
            false
        )

        // Информация о прогрессе
        textFont.color = Color.LIGHT_GRAY
        textFont.draw(
            game.batch,
            "Собрано: ${game.gameStateManager.collectedCassettes.size}/${allTracks.size}",
            0f,
            NoizeGame.GAME_HEIGHT - 120f,
            NoizeGame.GAME_WIDTH,
            Align.center,
            false
        )

        // Список треков
        for (i in allTracks.indices) {
            val track = allTracks[i]
            val isCollected = game.gameStateManager.collectedCassettes.contains(track.id)
            val isSelected = i == selectedTrack

            val y = NoizeGame.GAME_HEIGHT - 200f - i * 80f

            // Название трека
            textFont.color = when {
                !isCollected -> Color.DARK_GRAY
                isSelected -> Color.YELLOW
                else -> Color.WHITE
            }

            val title = if (isCollected) track.title else "???"
            textFont.draw(game.batch, title, 100f, y)

            // Описание (только для собранных)
            if (isCollected) {
                textFont.color = if (isSelected) Color.LIGHT_GRAY else Color.GRAY
                textFont.draw(
                    game.batch,
                    track.description,
                    100f,
                    y - 30f,
                    NoizeGame.GAME_WIDTH - 200f,
                    Align.left,
                    true
                )

                // Индикатор воспроизведения
                if (isSelected) {
                    textFont.color = Color.CYAN
                    textFont.draw(game.batch, "ENTER - воспроизвести", 100f, y - 60f)
                }
            }

            // Визуальная кассета
            renderCassetteIcon(600f, y, isCollected, isSelected)
        }

        // Управление
        textFont.color = Color.LIGHT_GRAY
        textFont.draw(
            game.batch,
            "Стрелки - навигация, ENTER - играть трек, ESC - назад",
            0f,
            50f,
            NoizeGame.GAME_WIDTH,
            Align.center,
            false
        )

        game.batch.end()
    }

    private fun renderCassetteIcon(x: Float, y: Float, isCollected: Boolean, isSelected: Boolean) {
        game.batch.end()

        val shapeRenderer = com.badlogic.gdx.graphics.glutils.ShapeRenderer()
        shapeRenderer.projectionMatrix = game.batch.projectionMatrix
        shapeRenderer.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled)

        if (isCollected) {
            // Цвет кассеты по треку
            shapeRenderer.color = when (allTracks[selectedTrack].id) {
                "make_some_noize" -> Color.CYAN
                "vidihay" -> Color.GREEN
                "vselennaya" -> Color.PURPLE
                "igra_slov" -> Color.YELLOW
                "moe_more" -> Color.BLUE
                "mercedes" -> Color.RED
                else -> Color.GRAY
            }

            if (isSelected) {
                shapeRenderer.color = Color(shapeRenderer.color.r, shapeRenderer.color.g, shapeRenderer.color.b, 1f)
            } else {
                shapeRenderer.color = Color(shapeRenderer.color.r, shapeRenderer.color.g, shapeRenderer.color.b, 0.7f)
            }
        } else {
            shapeRenderer.color = Color.DARK_GRAY
        }

        // Корпус кассеты
        shapeRenderer.rect(x, y - 20f, 60f, 40f)

        if (isCollected) {
            // Отверстия
            shapeRenderer.color = Color.BLACK
            shapeRenderer.circle(x + 12f, y, 8f)
            shapeRenderer.circle(x + 48f, y, 8f)

            // Лента
            shapeRenderer.color = Color.BROWN
            shapeRenderer.rect(x + 20f, y - 2f, 20f, 4f)
        }

        shapeRenderer.end()
        game.batch.begin()
    }

    private fun handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            selectedTrack = (selectedTrack - 1 + allTracks.size) % allTracks.size
            game.audioManager.playSound("collect", 0.5f)
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            selectedTrack = (selectedTrack + 1) % allTracks.size
            game.audioManager.playSound("collect", 0.5f)
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            val track = allTracks[selectedTrack]
            if (game.gameStateManager.collectedCassettes.contains(track.id)) {
                game.audioManager.playMusic(track.id)
                game.audioManager.playSound("ability")
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