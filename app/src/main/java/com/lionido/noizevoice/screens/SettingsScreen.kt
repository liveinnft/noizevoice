package com.lionido.noizevoice.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.utils.Align
import com.lionido.noizevoice.NoizeGame

class SettingsScreen(game: NoizeGame) : BaseScreen(game) {

    private lateinit var titleFont: BitmapFont
    private lateinit var menuFont: BitmapFont
    private var selectedOption = 0

    private val settings = mutableListOf(
        Setting("Громкость музыки", SettingType.SLIDER, game.audioManager.musicVolume),
        Setting("Громкость звуков", SettingType.SLIDER, game.audioManager.soundVolume),
        Setting("Музыка", SettingType.TOGGLE, if (game.audioManager.isMusicEnabled) 1f else 0f),
        Setting("Звуки", SettingType.TOGGLE, if (game.audioManager.isSoundEnabled) 1f else 0f),
        Setting("Сбросить прогресс", SettingType.BUTTON, 0f),
        Setting("Назад", SettingType.BUTTON, 0f)
    )

    data class Setting(
        val name: String,
        val type: SettingType,
        var value: Float
    )

    enum class SettingType {
        SLIDER, TOGGLE, BUTTON
    }

    override fun show() {
        titleFont = BitmapFont()
        titleFont.color = Color.CYAN
        titleFont.data.setScale(2.5f)

        menuFont = BitmapFont()
        menuFont.color = Color.WHITE
        menuFont.data.setScale(1.5f)
    }

    override fun render(delta: Float) {
        handleInput()

        game.batch.begin()

        // Заголовок
        titleFont.draw(
            game.batch,
            "Настройки",
            0f,
            NoizeGame.GAME_HEIGHT - 50f,
            NoizeGame.GAME_WIDTH,
            Align.center,
            false
        )

        // Настройки
        for (i in settings.indices) {
            val setting = settings[i]
            val y = NoizeGame.GAME_HEIGHT - 200f - i * 80f
            val isSelected = i == selectedOption

            // Название настройки
            menuFont.color = if (isSelected) Color.YELLOW else Color.WHITE
            menuFont.draw(game.batch, setting.name, 100f, y)

            // Значение настройки
            when (setting.type) {
                SettingType.SLIDER -> {
                    renderSlider(500f, y - 10f, setting.value, isSelected)
                    menuFont.color = Color.LIGHT_GRAY
                    menuFont.draw(game.batch, "${(setting.value * 100).toInt()}%", 700f, y)
                }

                SettingType.TOGGLE -> {
                    val status = if (setting.value > 0f) "ВКЛ" else "ВЫКЛ"
                    menuFont.color = if (setting.value > 0f) Color.GREEN else Color.RED
                    menuFont.draw(game.batch, status, 500f, y)
                }

                SettingType.BUTTON -> {
                    if (isSelected) {
                        menuFont.color = Color.YELLOW
                        menuFont.draw(game.batch, ">>> НАЖАТЬ <<<", 500f, y)
                    }
                }
            }
        }

        // Управление
        menuFont.color = Color.LIGHT_GRAY
        menuFont.draw(
            game.batch,
            "Стрелки - навигация, ВЛЕВО/ВПРАВО - изменить, ENTER - применить",
            0f,
            100f,
            NoizeGame.GAME_WIDTH,
            Align.center,
            false
        )

        game.batch.end()
    }

    private fun renderSlider(x: Float, y: Float, value: Float, isSelected: Boolean) {
        game.batch.end()

        val shapeRenderer = com.badlogic.gdx.graphics.glutils.ShapeRenderer()
        shapeRenderer.projectionMatrix = game.batch.projectionMatrix
        shapeRenderer.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled)

        // Полоса слайдера
        shapeRenderer.color = if (isSelected) Color.YELLOW else Color.GRAY
        shapeRenderer.rect(x, y, 150f, 10f)

        // Заполнение
        shapeRenderer.color = if (isSelected) Color.CYAN else Color.WHITE
        shapeRenderer.rect(x, y, 150f * value, 10f)

        // Ползунок
        shapeRenderer.color = if (isSelected) Color.YELLOW else Color.LIGHT_GRAY
        shapeRenderer.circle(x + 150f * value, y + 5f, 8f)

        shapeRenderer.end()
        game.batch.begin()
    }

    private fun handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            selectedOption = (selectedOption - 1 + settings.size) % settings.size
            game.audioManager.playSound("collect", 0.5f)
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            selectedOption = (selectedOption + 1) % settings.size
            game.audioManager.playSound("collect", 0.5f)
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            adjustSetting(-0.1f)
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            adjustSetting(0.1f)
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            activateSetting()
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(MainMenuScreen(game))
        }
    }

    private fun adjustSetting(delta: Float) {
        val setting = settings[selectedOption]

        when (setting.type) {
            SettingType.SLIDER -> {
                setting.value = (setting.value + delta).coerceIn(0f, 1f)
                applySetting(setting)
                game.audioManager.playSound("collect", 0.3f)
            }

            SettingType.TOGGLE -> {
                setting.value = if (setting.value > 0f) 0f else 1f
                applySetting(setting)
                game.audioManager.playSound("ability", 0.5f)
            }

            else -> {}
        }
    }

    private fun activateSetting() {
        val setting = settings[selectedOption]

        when (setting.name) {
            "Сбросить прогресс" -> {
                game.gameStateManager = GameStateManager(game)
                game.audioManager.playSound("hurt")
                Gdx.app.log("Settings", "Прогресс сброшен")
            }

            "Назад" -> {
                game.setScreen(MainMenuScreen(game))
            }
        }
    }

    private fun applySetting(setting: Setting) {
        when (setting.name) {
            "Громкость музыки" -> {
                game.audioManager.setMusicVolume(setting.value)
            }

            "Громкость звуков" -> {
                game.audioManager.setSoundVolume(setting.value)
            }

            "Музыка" -> {
                if ((setting.value > 0f) != game.audioManager.isMusicEnabled) {
                    game.audioManager.toggleMusic()
                }
            }

            "Звуки" -> {
                if ((setting.value > 0f) != game.audioManager.isSoundEnabled) {
                    game.audioManager.toggleSound()
                }
            }
        }
    }

    override fun dispose() {
        titleFont.dispose()
        menuFont.dispose()
    }
}