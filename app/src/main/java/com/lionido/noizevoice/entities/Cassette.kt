package com.lionido.noizevoice.entities

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.lionido.noizevoice.NoizeGame

class Cassette(
    private val game: NoizeGame,
    x: Float,
    y: Float,
    val trackId: String
) {

    val position = Vector2(x, y)
    val hitbox = Rectangle()
    val displayName: String
    private var bobTimer = 0f
    private val originalY = y
    private var isCollected = false

    init {
        displayName = when (trackId) {
            "make_some_noize" -> "Make Some Noize"
            "vidihay" -> "Выдыхай"
            "vselennaya" -> "Вселенная бесконечна?"
            "igra_slov" -> "Игра слов"
            "moe_more" -> "Моё море"
            "mercedes" -> "Mercedes S666"
            else -> "Неизвестный трек"
        }

        updateHitbox()
    }

    fun update(deltaTime: Float) {
        if (isCollected) return

        // Эффект покачивания
        bobTimer += deltaTime * 2f
        position.y = originalY + kotlin.math.sin(bobTimer.toDouble()).toFloat() * 10f

        updateHitbox()
    }

    fun render(batch: SpriteBatch) {
        if (isCollected) return

        // Временный рендер (пока нет спрайтов)
        batch.end()

        val shapeRenderer = ShapeRenderer()
        shapeRenderer.projectionMatrix = batch.projectionMatrix
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // Основа кассеты
        shapeRenderer.color = getTrackColor()
        shapeRenderer.rect(hitbox.x, hitbox.y, hitbox.width, hitbox.height)

        // Блестящий эффект
        shapeRenderer.color = Color.WHITE
        shapeRenderer.rect(hitbox.x + 5f, hitbox.y + hitbox.height - 8f, hitbox.width - 10f, 3f)

        // Отверстия кассеты
        shapeRenderer.color = Color.BLACK
        shapeRenderer.circle(hitbox.x + 8f, hitbox.y + hitbox.height/2f, 6f)
        shapeRenderer.circle(hitbox.x + hitbox.width - 8f, hitbox.y + hitbox.height/2f, 6f)

        shapeRenderer.end()
        batch.begin()
    }

    private fun getTrackColor(): Color {
        return when (trackId) {
            "make_some_noize" -> Color.CYAN
            "vidihay" -> Color.GREEN
            "vselennaya" -> Color.PURPLE
            "igra_slov" -> Color.YELLOW
            "moe_more" -> Color.BLUE
            "mercedes" -> Color.RED
            else -> Color.GRAY
        }
    }

    fun collect() {
        isCollected = true
        game.gameStateManager.collectCassette(trackId)
    }

    private fun updateHitbox() {
        hitbox.set(position.x, position.y, 40f, 25f)
    }
}