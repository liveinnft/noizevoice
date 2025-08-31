package com.lionido.noizevoice.entities

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.lionido.noizevoice.NoizeGame

class Enemy(
    private val game: NoizeGame,
    startX: Float,
    startY: Float,
    private val type: EnemyType
) {

    val position = Vector2(startX, startY)
    val velocity = Vector2()
    val hitbox = Rectangle()

    var health = type.maxHealth
    var isDefeated = false
    private var attackCooldown = 0f
    private var moveTimer = 0f
    private var isAttacking = false

    enum class EnemyType(
        val maxHealth: Int,
        val damage: Int,
        val speed: Float,
        val attackRange: Float,
        val displayName: String,
        val color: Color
    ) {
        NOISE_MAKER(30, 10, 100f, 150f, "Шумовик", Color.ORANGE),
        SILENCER(50, 15, 80f, 200f, "Глушитель", Color.PURPLE),
        CRITIC(40, 20, 120f, 300f, "Критик", Color.RED),
        MEDIA_SPAMMER(60, 12, 90f, 250f, "Медиа-спамер", Color.YELLOW),
        CENSOR_DRONE(80, 25, 70f, 180f, "Цензурный дрон", Color.GRAY),
        FANATIC_PHANTOM(35, 18, 150f, 120f, "Фанатский фантом", Color.PINK)
    }

    init {
        updateHitbox()
    }

    fun update(deltaTime: Float) {
        if (isDefeated) return

        moveTimer += deltaTime
        attackCooldown -= deltaTime

        // ИИ поведение в зависимости от типа
        when (type) {
            EnemyType.NOISE_MAKER -> {
                // Простое патрулирование
                if (moveTimer > 2f) {
                    velocity.x = -velocity.x
                    moveTimer = 0f
                }
                if (velocity.x == 0f) velocity.x = type.speed
            }

            EnemyType.SILENCER -> {
                // Преследование игрока
                // TODO: Получить позицию игрока из GameplayScreen
                // Пока используем простое патрулирование
                basicAI()
            }

            EnemyType.CRITIC -> {
                // Дальние атаки
                // TODO: Получить позицию игрока из GameplayScreen
                // Пока используем простое патрулирование
                basicAI()

                if (attackCooldown <= 0f) {
                    rangedAttack()
                }
            }

            else -> {
                // Базовое поведение для новых типов врагов
                basicAI()
            }
        }

        // Обновление позиции
        position.add(velocity.x * deltaTime, velocity.y * deltaTime)

        // Гравитация для наземных врагов
        if (type != EnemyType.CENSOR_DRONE) {
            velocity.y -= 1000f * deltaTime
            if (position.y <= 100f) {
                position.y = 100f
                velocity.y = 0f
            }
        }

        updateHitbox()
    }

    private fun basicAI() {
        // Простое патрулирование
        if (moveTimer > 3f) {
            velocity.x = if (velocity.x > 0) -type.speed else type.speed
            moveTimer = 0f
        }
    }

    private fun attack() {
        isAttacking = true
        attackCooldown = 1.5f
        game.audioManager.playSound("hurt", 0.7f)

        // TODO: Логика атаки (урон игроку, эффекты)
    }

    private fun rangedAttack() {
        attackCooldown = 2f
        game.audioManager.playSound("attack", 0.5f)

        // TODO: Создание снаряда или дальней атаки
    }

    fun takeDamage(damage: Int) {
        health -= damage

        if (health <= 0) {
            defeat()
        }
    }

    private fun defeat() {
        isDefeated = true
        game.audioManager.playSound("collect")

        // Дроп предметов в зависимости от типа
        when (type) {
            EnemyType.CRITIC -> {
                game.gameStateManager.unlockMemory("critic_defeat_${System.currentTimeMillis()}")
            }
            EnemyType.SILENCER -> {
                game.gameStateManager.restoreEnergy(20)
            }
            else -> {
                game.gameStateManager.heal(5)
            }
        }
    }

    fun render(batch: SpriteBatch) {
        if (isDefeated) return

        // Временный рендер через ShapeRenderer (пока нет спрайтов)
        // TODO: Заменить на спрайты
        batch.end()

        val shapeRenderer = ShapeRenderer()
        shapeRenderer.projectionMatrix = batch.projectionMatrix
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // Тело врага
        shapeRenderer.color = type.color
        shapeRenderer.rect(hitbox.x, hitbox.y, hitbox.width, hitbox.height)

        // Полоска здоровья
        shapeRenderer.color = Color.RED
        shapeRenderer.rect(hitbox.x, hitbox.y + hitbox.height + 5f, hitbox.width, 5f)
        shapeRenderer.color = Color.GREEN
        val healthPercent = health.toFloat() / type.maxHealth
        shapeRenderer.rect(hitbox.x, hitbox.y + hitbox.height + 5f, hitbox.width * healthPercent, 5f)

        shapeRenderer.end()
        batch.begin()
    }

    private fun updateHitbox() {
        val size = when (type) {
            EnemyType.NOISE_MAKER -> Vector2(48f, 64f)
            EnemyType.SILENCER -> Vector2(56f, 72f)
            EnemyType.CRITIC -> Vector2(52f, 68f)
            EnemyType.MEDIA_SPAMMER -> Vector2(60f, 76f)
            EnemyType.CENSOR_DRONE -> Vector2(64f, 32f)
            EnemyType.FANATIC_PHANTOM -> Vector2(44f, 60f)
        }

        hitbox.set(position.x, position.y, size.x, size.y)
    }
}