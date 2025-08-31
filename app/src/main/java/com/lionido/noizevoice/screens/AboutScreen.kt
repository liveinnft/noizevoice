package com.lionido.noizevoice.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.utils.Align
import com.lionido.noizevoice.NoizeGame

class AboutScreen(game: NoizeGame) : BaseScreen(game) {

    private lateinit var titleFont: BitmapFont
    private lateinit var textFont: BitmapFont
    private var scrollY = 0f

    private val aboutText = """
        NOIZE: Битва за голос
        
        2D пиксельная игра, основанная на биографии
        и творчестве Ивана Алексеева (Noize MC)
        
        ЖАНРЫ:
        • Платформер-головоломка
        • Ритм-экшен
        • Визуальная новелла
        
        ОСОБЕННОСТИ:
        • Интеграция реальных треков Noize MC
        • Биографические кат-сцены
        • Уникальная система ритм-битв
        • Коллекционные кассеты
        • Множество способностей через музыку
        
        ИСТОРИЯ:
        Иван теряет свои песни и голос.
        Путешествие через акты его жизни:
        от первых концертов до противостояния
        с музыкальной индустрией.
        
        УПРАВЛЕНИЕ:
        WASD / Стрелки - движение
        X - атака
        Z - Make Some Noize
        C - Выдыхай (стелс)
        V - Вселенная бесконечна? (телепорт)
        B - Игра слов (ритм-атака)
        Q - Ультимейт (все кассеты)
        
        РАЗРАБОТКА:
        Движок: LibGDX
        Платформа: Android 8.0+
        Стиль: Пиксель-арт
        
        Игра создана с уважением к творчеству
        и биографии Ивана Алексеева (Noize MC)
        
        Версия: 1.0
        2025
    """.trimIndent()

    override fun show() {
        titleFont = BitmapFont()
        titleFont.color = Color.CYAN
        titleFont.data.setScale(2f)

        textFont = BitmapFont()
        textFont.color = Color.WHITE
        textFont.data.setScale(1.2f)

        // Спокойная фоновая музыка
        game.audioManager.playMusic("moe_more")
    }

    override fun render(delta: Float) {
        handleInput()

        game.batch.begin()

        // Заголовок
        titleFont.draw(
            game.batch,
            "О игре",
            0f,
            NoizeGame.GAME_HEIGHT - 50f + scrollY,
            NoizeGame.GAME_WIDTH,
            Align.center,
            false
        )

        // Основной текст
        textFont.color = Color.WHITE
        textFont.draw(
            game.batch,
            aboutText,
            100f,
            NoizeGame.GAME_HEIGHT - 150f + scrollY,
            NoizeGame.GAME_WIDTH - 200f,
            Align.left,
            true
        )

        // Управление скроллингом
        textFont.color = Color.LIGHT_GRAY
        textFont.draw(
            game.batch,
            "Стрелки ВВЕРХ/ВНИЗ - прокрутка, ESC - назад",
            0f,
            50f,
            NoizeGame.GAME_WIDTH,
            Align.center,
            false
        )

        game.batch.end()
    }

    private fun handleInput() {
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            scrollY += 200f * Gdx.graphics.deltaTime
            scrollY = scrollY.coerceAtMost(0f)
        }

        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            scrollY -= 200f * Gdx.graphics.deltaTime
            scrollY = scrollY.coerceAtLeast(-500f)
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