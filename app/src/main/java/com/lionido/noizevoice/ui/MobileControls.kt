package com.lionido.noizevoice.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Circle
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.lionido.noizevoice.NoizeGame

class MobileControls(private val game: NoizeGame) {

    private lateinit var font: BitmapFont

    // Виртуальный джойстик
    private val joystickBase = Circle(150f, 150f, 80f)
    private val joystickKnob = Circle(150f, 150f, 30f)
    private var joystickPressed = false
    private var joystickTouchId = -1

    // Кнопки действий
    private val attackButton = Circle(NoizeGame.GAME_WIDTH - 120f, 150f, 50f)
    private val jumpButton = Circle(NoizeGame.GAME_WIDTH - 220f, 150f, 50f)

    // Кнопки способностей
    private val abilityButtons = listOf(
        AbilityButton("Z", Circle(NoizeGame.GAME_WIDTH - 120f, 280f, 40f), "MAKE_SOME_NOIZE"),
        AbilityButton("C", Circle(NoizeGame.GAME_WIDTH - 200f, 280f, 40f), "VIDIHAY"),
        AbilityButton("V", Circle(NoizeGame.GAME_WIDTH - 280f, 280f, 40f), "VSELENNAYA"),
        AbilityButton("B", Circle(NoizeGame.GAME_WIDTH - 360f, 280f, 40f), "IGRA_SLOV")
    )

    // Кнопка ультимейта
    private val ultimateButton = Circle(NoizeGame.GAME_WIDTH - 170f, 380f, 60f)

    // Кнопки интерфейса
    private val pauseButton = Rectangle(NoizeGame.GAME_WIDTH - 100f, NoizeGame.GAME_HEIGHT - 80f, 80f, 60f)

    data class AbilityButton(
        val label: String,
        val hitbox: Circle,
        val abilityId: String
    )

    // Состояние кнопок
    var isMovingLeft = false
    var isMovingRight = false
    var isJumpPressed = false
    var isAttackPressed = false
    private val pressedAbilities = mutableSetOf<String>()
    var isUltimatePressed = false
    var isPausePressed = false

    init {
        font = BitmapFont()
        font.color = Color.WHITE
        font.data.setScale(1.2f)
    }

    fun update() {
        updateTouchInput()
        resetButtonStates()
    }

    private fun updateTouchInput() {
        // Сброс состояний
        isMovingLeft = false
        isMovingRight = false
        isJumpPressed = false
        isAttackPressed = false
        pressedAbilities.clear()
        isUltimatePressed = false
        isPausePressed = false

        // Обработка множественных касаний
        for (i in 0 until 5) {
            if (Gdx.input.isTouched(i)) {
                val touchX = Gdx.input.getX(i) * NoizeGame.GAME_WIDTH / Gdx.graphics.width
                val touchY = (Gdx.graphics.height - Gdx.input.getY(i)) * NoizeGame.GAME_HEIGHT / Gdx.graphics.height

                checkButtonPress(touchX, touchY, i)
            }
        }
    }

    private fun checkButtonPress(x: Float, y: Float, touchId: Int) {
        val touchPoint = Vector2(x, y)

        // Джойстик
        if (joystickBase.contains(touchPoint)) {
            if (!joystickPressed || joystickTouchId == touchId) {
                joystickPressed = true
                joystickTouchId = touchId

                // Вычисление направления
                val direction = Vector2(x - joystickBase.x, y - joystickBase.y)
                if (direction.len() > joystickBase.radius) {
                    direction.setLength(joystickBase.radius)
                }

                joystickKnob.setPosition(
                    joystickBase.x + direction.x,
                    joystickBase.y + direction.y
                )

                // Определение движения
                if (direction.x < -20f) isMovingLeft = true
                if (direction.x > 20f) isMovingRight = true
            }
        } else if (joystickTouchId == touchId) {
            // Отпускание джойстика
            joystickPressed = false
            joystickTouchId = -1
            joystickKnob.setPosition(joystickBase.x, joystickBase.y)
        }

        // Кнопки действий
        if (jumpButton.contains(touchPoint)) {
            isJumpPressed = true
        }

        if (attackButton.contains(touchPoint)) {
            isAttackPressed = true
        }

        // Кнопки способностей
        abilityButtons.forEach { button ->
            if (button.hitbox.contains(touchPoint)) {
                pressedAbilities.add(button.abilityId)
            }
        }

        // Ультимейт
        if (ultimateButton.contains(touchPoint)) {
            isUltimatePressed = true
        }

        // Пауза
        if (pauseButton.contains(x, y)) {
            isPausePressed = true
        }
    }

    private fun resetButtonStates() {
        if (!joystickPressed) {
            joystickKnob.setPosition(joystickBase.x, joystickBase.y)
        }
    }

    fun render(batch: SpriteBatch) {
        batch.end()

        val shapeRenderer = ShapeRenderer()
        shapeRenderer.projectionMatrix = batch.projectionMatrix
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // Джойстик
        renderJoystick(shapeRenderer)

        // Кнопки действий
        renderActionButtons(shapeRenderer)

        // Кнопки способностей
        renderAbilityButtons(shapeRenderer)

        shapeRenderer.end()
        batch.begin()

        // Подписи кнопок
        renderButtonLabels(batch)
    }

    private fun renderJoystick(renderer: ShapeRenderer) {
        // Основа джойстика
        renderer.color = Color(0.5f, 0.5f, 0.5f, 0.6f)
        renderer.circle(joystickBase.x, joystickBase.y, joystickBase.radius)

        // Ручка джойстика
        renderer.color = if (joystickPressed) Color.CYAN else Color.WHITE
        renderer.circle(joystickKnob.x, joystickKnob.y, joystickKnob.radius)
    }

    private fun renderActionButtons(renderer: ShapeRenderer) {
        // Кнопка прыжка
        renderer.color = if (isJumpPressed) Color.GREEN else Color(0f, 0.8f, 0f, 0.7f)
        renderer.circle(jumpButton.x, jumpButton.y, jumpButton.radius)

        // Кнопка атаки
        renderer.color = if (isAttackPressed) Color.RED else Color(0.8f, 0f, 0f, 0.7f)
        renderer.circle(attackButton.x, attackButton.y, attackButton.radius)
    }

    private fun renderAbilityButtons(renderer: ShapeRenderer) {
        abilityButtons.forEach { button ->
            val isPressed = pressedAbilities.contains(button.abilityId)
            val isUnlocked = game.gameStateManager.unlockedAbilities.contains(button.abilityId)

            renderer.color = when {
                !isUnlocked -> Color(0.3f, 0.3f, 0.3f, 0.5f)
                isPressed -> Color.YELLOW
                else -> Color(0.7f, 0.7f, 0f, 0.7f)
            }

            renderer.circle(button.hitbox.x, button.hitbox.y, button.hitbox.radius)
        }

        // Кнопка ультимейта
        val hasUltimate = game.gameStateManager.hasAbility(com.lionido.noizevoice.managers.GameStateManager.Ability.ULTIMATE_COMBO)
        renderer.color = when {
            !hasUltimate -> Color(0.3f, 0.3f, 0.3f, 0.5f)
            isUltimatePressed -> Color.GOLD
            else -> Color(1f, 0.5f, 0f, 0.8f)
        }
        renderer.circle(ultimateButton.x, ultimateButton.y, ultimateButton.radius)
    }

    private fun renderButtonLabels(batch: SpriteBatch) {
        // Подписи кнопок действий
        font.color = Color.WHITE
        font.draw(batch, "JUMP", jumpButton.x - 25f, jumpButton.y + 5f)
        font.draw(batch, "ATTACK", attackButton.x - 30f, attackButton.y + 5f)

        // Подписи способностей
        abilityButtons.forEach { button ->
            val isUnlocked = game.gameStateManager.unlockedAbilities.contains(button.abilityId)
            font.color = if (isUnlocked) Color.WHITE else Color.GRAY
            font.draw(batch, button.label, button.hitbox.x - 10f, button.hitbox.y + 5f)
        }

        // Подпись ультимейта
        val hasUltimate = game.gameStateManager.hasAbility(com.lionido.noizevoice.managers.GameStateManager.Ability.ULTIMATE_COMBO)
        font.color = if (hasUltimate) Color.GOLD else Color.GRAY
        font.draw(batch, "ULT", ultimateButton.x - 15f, ultimateButton.y + 5f)

        // Кнопка паузы
        font.color = Color.WHITE
        font.draw(batch, "PAUSE", pauseButton.x + 10f, pauseButton.y + 35f)
    }

    fun dispose() {
        font.dispose()
    }
}