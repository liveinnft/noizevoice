package com.lionido.noizevoice.entities

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.lionido.noizevoice.NoizeGame
import com.lionido.noizevoice.managers.GameStateManager

class Player(private val game: NoizeGame) {

    // Физические параметры
    val position = Vector2(100f, 100f)
    val velocity = Vector2()
    val size = Vector2(64f, 96f)
    val hitbox = Rectangle()

    // Игровые параметры
    private val speed = 300f
    private val jumpStrength = 600f
    private var isGrounded = false
    private var canDoubleJump = false
    private var isInStealth = false

    // Анимации
    private lateinit var idleAnimation: Animation<TextureRegion>
    private lateinit var walkAnimation: Animation<TextureRegion>
    private lateinit var jumpAnimation: Animation<TextureRegion>
    private lateinit var attackAnimation: Animation<TextureRegion>
    private var animationTime = 0f
    private var currentState = PlayerState.IDLE
    private var facingRight = true

    enum class PlayerState {
        IDLE, WALKING, JUMPING, ATTACKING, USING_ABILITY, STEALTH
    }

    init {
        loadAnimations()
        updateHitbox()
    }

    private fun loadAnimations() {
        try {
            // Создание простых анимаций (пока без спрайтов)
            val dummyTexture = Texture(Gdx.files.internal("sprites/ivan_placeholder.png"))
            val region = TextureRegion(dummyTexture)

            idleAnimation = Animation(0.5f, region)
            walkAnimation = Animation(0.2f, region)
            jumpAnimation = Animation(0.3f, region)
            attackAnimation = Animation(0.15f, region)

        } catch (e: Exception) {
            Gdx.app.log("Player", "Анимации не загружены, используем заглушки")
        }
    }

    fun update(deltaTime: Float) {
        animationTime += deltaTime
        handleInput()
        updatePhysics(deltaTime)
        updateState()
        updateHitbox()

        // Обновление состояния игрока в менеджере
        game.gameStateManager.update(deltaTime)
    }

    private fun handleInput() {
        // Движение
        velocity.x = 0f

        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            velocity.x = -speed
            facingRight = false
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            velocity.x = speed
            facingRight = true
        }

        // Прыжок
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            jump()
        }

        // Атака
        if (Gdx.input.isKeyJustPressed(Input.Keys.X)) {
            attack()
        }

        // Способности
        if (Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
            useAbility(GameStateManager.Ability.MAKE_SOME_NOIZE)
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.C)) {
            useAbility(GameStateManager.Ability.VIDIHAY)
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.V)) {
            useAbility(GameStateManager.Ability.VSELENNAYA)
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.B)) {
            useAbility(GameStateManager.Ability.IGRA_SLOV)
        }

        // Ультимейт
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            useUltimate()
        }
    }

    private fun jump() {
        if (isGrounded) {
            velocity.y = jumpStrength
            isGrounded = false
            canDoubleJump = true
            game.audioManager.playSound("jump")
        } else if (canDoubleJump) {
            velocity.y = jumpStrength * 0.8f
            canDoubleJump = false
            game.audioManager.playSound("jump", 0.7f)
        }
    }

    private fun attack() {
        if (currentState != PlayerState.ATTACKING) {
            currentState = PlayerState.ATTACKING
            animationTime = 0f
            game.audioManager.playSound("attack")
        }
    }

    private fun useAbility(ability: GameStateManager.Ability) {
        if (!game.gameStateManager.hasAbility(ability)) {
            return
        }

        val energyCost = when (ability) {
            GameStateManager.Ability.MAKE_SOME_NOIZE -> 20
            GameStateManager.Ability.VIDIHAY -> 30
            GameStateManager.Ability.VSELENNAYA -> 40
            GameStateManager.Ability.IGRA_SLOV -> 25
            GameStateManager.Ability.ULTIMATE_COMBO -> 80
        }

        if (game.gameStateManager.useEnergy(energyCost)) {
            when (ability) {
                GameStateManager.Ability.MAKE_SOME_NOIZE -> {
                    // Звуковая атака
                    game.audioManager.playSound("ability")
                    Gdx.app.log("Player", "Использована способность: Make Some Noize")
                }
                GameStateManager.Ability.VIDIHAY -> {
                    // Стелс и восстановление
                    isInStealth = true
                    game.gameStateManager.heal(20)
                    Gdx.app.log("Player", "Использована способность: Выдыхай")
                }
                GameStateManager.Ability.VSELENNAYA -> {
                    // Телепортация
                    Gdx.app.log("Player", "Использована способность: Вселенная бесконечна?")
                }
                GameStateManager.Ability.IGRA_SLOV -> {
                    // Ритм-атака
                    Gdx.app.log("Player", "Использована способность: Игра слов")
                }
                else -> {}
            }

            currentState = PlayerState.USING_ABILITY
            animationTime = 0f
        }
    }

    private fun useUltimate() {
        if (game.gameStateManager.hasAbility(GameStateManager.Ability.ULTIMATE_COMBO)) {
            if (game.gameStateManager.useEnergy(80)) {
                // Мощная комбо-атака всеми собранными треками
                game.audioManager.playSound("ability", 1.5f)
                Gdx.app.log("Player", "УЛЬТИМЕЙТ! Все кассеты активированы!")
                currentState = PlayerState.USING_ABILITY
                animationTime = 0f
            }
        }
    }

    private fun updatePhysics(deltaTime: Float) {
        // Гравитация
        if (!isGrounded) {
            velocity.y -= 1800f * deltaTime
        }

        // Обновление позиции
        position.x += velocity.x * deltaTime
        position.y += velocity.y * deltaTime

        // Ограничения экрана
        position.x = position.x.coerceIn(0f, NoizeGame.GAME_WIDTH - size.x)

        // Проверка земли (упрощенная)
        if (position.y <= 100f) {
            position.y = 100f
            velocity.y = 0f
            isGrounded = true
        }
    }

    private fun updateState() {
        when (currentState) {
            PlayerState.IDLE -> {
                if (velocity.x != 0f) {
                    currentState = PlayerState.WALKING
                }
            }
            PlayerState.WALKING -> {
                if (velocity.x == 0f) {
                    currentState = PlayerState.IDLE
                }
                if (!isGrounded) {
                    currentState = PlayerState.JUMPING
                }
            }
            PlayerState.JUMPING -> {
                if (isGrounded && velocity.x == 0f) {
                    currentState = PlayerState.IDLE
                } else if (isGrounded && velocity.x != 0f) {
                    currentState = PlayerState.WALKING
                }