package com.lionido.noizevoice.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Align
import com.lionido.noizevoice.NoizeGame
import com.lionido.noizevoice.entities.Player
import com.lionido.noizevoice.systems.ParticleSystem
import com.lionido.noizevoice.systems.RhythmBattleSystem

class BossBattleScreen(game: NoizeGame) : BaseScreen(game) {

    private lateinit var player: Player
    private lateinit var font: BitmapFont
    private lateinit var particleSystem: ParticleSystem
    private lateinit var rhythmSystem: RhythmBattleSystem

    // Барков - финальный босс
    private val bossPosition = Vector2(NoizeGame.GAME_WIDTH - 200f, 400f)
    private var bossHealth = 300
    private val bossMaxHealth = 300
    private var bossPhase = 1
    private var bossAttackTimer = 0f
    private var battlePhaseTimer = 0f

    // Атаки босса
    private val mediaWaves = mutableListOf<MediaWave>()
    private var isRhythmPhase = false

    data class MediaWave(
        val position: Vector2,
        val velocity: Vector2,
        val size: Float,
        var life: Float
    ) {
        fun update(deltaTime: Float) {
            position.add(velocity.x * deltaTime, velocity.y * deltaTime)
            life -= deltaTime
            velocity.scl(1.02f) // Ускорение
        }

        fun isDead(): Boolean = life <= 0f || position.x < -100f
    }

    override fun show() {
        font = BitmapFont()
        font.color = Color.WHITE
        font.data.setScale(1.5f)

        player = Player(game)
        player.position.set(100f, 400f)

        particleSystem = ParticleSystem()
        rhythmSystem = RhythmBattleSystem(game)

        // Эпическая музыка финальной битвы
        game.audioManager.playMusic("mercedes")

        // Начальный диалог
        startBossDialogue()

        Gdx.app.log("BossBattle", "Финальная битва с Барковым началась!")
    }

    private fun startBossDialogue() {
        // TODO: Интеграция с DialogueSystem
        Gdx.app.log("BossBattle", "Барков: 'Ты мог молчать и следовать правилам.'")
        Gdx.app.log("BossBattle", "Иван: 'Я буду звучать. Я буду слышимым.'")
    }

    override fun render(delta: Float) {
        updateBattle(delta)
        renderBattle()

        if (bossHealth <= 0) {
            winBattle()
        }

        if (game.gameStateManager.playerHealth <= 0) {
            loseBattle()
        }
    }

    private fun updateBattle(delta: Float) {
        battlePhaseTimer += delta
        bossAttackTimer += delta

        // Обновление игрока
        player.update(delta)

        // Обновление систем
        particleSystem.update(delta)

        // Фазы босса
        updateBossPhases(delta)

        // Атаки босса
        if (bossAttackTimer >= getBossAttackInterval()) {
            bossAttack()
            bossAttackTimer = 0f
        }

        // Обновление атак
        mediaWaves.forEach { it.update(delta) }
        mediaWaves.removeAll { it.isDead() }

        // Проверка коллизий
        checkBossCollisions()

        handleInput()
    }

    private fun updateBossPhases(delta: Float) {
        val healthPercent = bossHealth.toFloat() / bossMaxHealth

        when {
            healthPercent > 0.66f -> bossPhase = 1
            healthPercent > 0.33f -> bossPhase = 2
            else -> bossPhase = 3
        }

        // Ритм-фаза каждые 20 секунд
        if (battlePhaseTimer % 20f < 0.1f && !isRhythmPhase) {
            startRhythmPhase()
        }
    }

    private fun startRhythmPhase() {
        isRhythmPhase = true
        rhythmSystem.startBattle("mercedes")
        Gdx.app.log("BossBattle", "Начинается ритм-фаза!")
    }

    private fun bossAttack() {
        when (bossPhase) {
            1 -> basicMediaAttack()
            2 -> waveBarrage()
            3 -> ultimateAttack()
        }
    }

    private fun basicMediaAttack() {
        // Простые медиа-волны
        val wave = MediaWave(
            Vector2(bossPosition.x, bossPosition.y),
            Vector2(-200f, MathUtils.random(-50f, 50f)),
            30f,
            5f
        )
        mediaWaves.add(wave)

        particleSystem.createSoundWave(bossPosition.x, bossPosition.y, 0.8f)
    }

    private fun waveBarrage() {
        // Множественные атаки
        for (i in 0 until 3) {
            val wave = MediaWave(
                Vector2(bossPosition.x, bossPosition.y + i * 50f),
                Vector2(-250f, MathUtils.random(-100f, 100f)),
                25f,
                4f
            )
            mediaWaves.add(wave)
        }

        particleSystem.createSoundWave(bossPosition.x, bossPosition.y, 1.2f)
    }

    private fun ultimateAttack() {
        // Мощная атака третьей фазы
        for (i in 0 until 5) {
            val angle = i * 72f // 360/5
            val velocity = Vector2(
                MathUtils.cosDeg(angle) * 180f,
                MathUtils.sinDeg(angle) * 180f
            )

            val wave = MediaWave(
                Vector2(bossPosition.x, bossPosition.y),
                velocity,
                40f,
                6f
            )
            mediaWaves.add(wave)
        }

        particleSystem.createSoundWave(bossPosition.x, bossPosition.y, 2f)
    }

    private fun getBossAttackInterval(): Float {
        return when (bossPhase) {
            1 -> 3f
            2 -> 2f
            3 -> 1.5f
            else -> 3f
        }
    }

    private fun checkBossCollisions() {
        // Коллизии медиа-волн с игроком
        mediaWaves.forEach { wave ->
            val distance = wave.position.dst(player.position)
            if (distance < wave.size + 32f) {
                game.gameStateManager.takeDamage(15)
                particleSystem.createEnergyBurst(player.position.x, player.position.y)
                mediaWaves.remove(wave)
                return@forEach
            }
        }

        // Атаки игрока по боссу
        if (player.isAttacking) {
            val distance = player.position.dst(bossPosition)
            if (distance < 100f) {
                bossHealth -= 10
                particleSystem.createSoundWave(bossPosition.x, bossPosition.y, 0.5f)
                game.audioManager.playSound("attack")
            }
        }
    }

    private fun renderBattle() {
        game.batch.begin()

        // Фон битвы (ночной мегаполис)
        renderBattleBackground()

        // Рендер игрока
        player.render(game.batch)

        game.batch.end()

        // Рендер босса и эффектов
        renderBoss()
        renderMediaWaves()

        // Частицы
        particleSystem.render(game.batch)

        // Ритм-битва (если активна)
        if (isRhythmPhase) {
            rhythmSystem.render(game.batch)
        }

        game.batch.begin()

        // HUD битвы
        renderBossHUD()

        game.batch.end()
    }

    private fun renderBattleBackground() {
        // Темный фон с городскими огнями
        // TODO: Добавить спрайты мегаполиса
    }

    private fun renderBoss() {
        val shapeRenderer = ShapeRenderer()
        shapeRenderer.projectionMatrix = game.batch.projectionMatrix
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // Барков (большой противник)
        val bossColor = when (bossPhase) {
            1 -> Color.PURPLE
            2 -> Color.RED
            3 -> Color.BLACK
            else -> Color.GRAY
        }

        shapeRenderer.color = bossColor
        shapeRenderer.rect(bossPosition.x - 50f, bossPosition.y - 75f, 100f, 150f)

        // Глаза босса
        shapeRenderer.color = Color.RED
        shapeRenderer.circle(bossPosition.x - 20f, bossPosition.y + 30f, 8f)
        shapeRenderer.circle(bossPosition.x + 20f, bossPosition.y + 30f, 8f)

        shapeRenderer.end()
    }

    private fun renderMediaWaves() {
        val shapeRenderer = ShapeRenderer()
        shapeRenderer.projectionMatrix = game.batch.projectionMatrix
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        mediaWaves.forEach { wave ->
            shapeRenderer.color = Color(1f, 0.3f, 0.3f, wave.life / 5f)
            shapeRenderer.circle(wave.position.x, wave.position.y, wave.size)
        }

        shapeRenderer.end()
    }

    private fun renderBossHUD() {
        // Здоровье игрока
        font.color = if (game.gameStateManager.playerHealth > 30) Color.GREEN else Color.RED
        font.draw(game.batch, "Иван: ${game.gameStateManager.playerHealth}/100", 20f, NoizeGame.GAME_HEIGHT - 20f)

        // Здоровье босса
        font.color = if (bossHealth > 100) Color.PURPLE else Color.RED
        font.draw(game.batch, "Барков: $bossHealth/$bossMaxHealth", 20f, NoizeGame.GAME_HEIGHT - 60f)

        // Фаза босса
        font.color = Color.YELLOW
        font.draw(game.batch, "Фаза: $bossPhase/3", 20f, NoizeGame.GAME_HEIGHT - 100f)

        // Полоска здоровья босса
        val healthPercent = bossHealth.toFloat() / bossMaxHealth
        drawHealthBar(game.batch, NoizeGame.GAME_WIDTH / 2f - 200f, NoizeGame.GAME_HEIGHT - 40f, 400f, 20f, healthPercent, Color.PURPLE)

        if (isRhythmPhase) {
            font.color = Color.CYAN
            font.draw(
                game.batch,
                "РИТМ-БИТВА! Бей в такт!",
                0f,
                NoizeGame.GAME_HEIGHT - 140f,
                NoizeGame.GAME_WIDTH,
                Align.center,
                false
            )
        }
    }

    private fun drawHealthBar(batch: SpriteBatch, x: Float, y: Float, width: Float, height: Float, percent: Float, color: Color) {
        batch.end()

        val shapeRenderer = ShapeRenderer()
        shapeRenderer.projectionMatrix = batch.projectionMatrix
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // Фон полоски
        shapeRenderer.color = Color.DARK_GRAY
        shapeRenderer.rect(x, y, width, height)

        // Заполнение
        shapeRenderer.color = color
        shapeRenderer.rect(x, y, width * percent, height)

        shapeRenderer.end()
        batch.begin()
    }

    private fun handleInput() {
        // Специальные атаки для босс-битвы
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q) &&
            game.gameStateManager.hasAbility(com.lionido.noizevoice.managers.GameStateManager.Ability.ULTIMATE_COMBO)) {

            ultimateAttackOnBoss()
        }
    }

    private fun ultimateAttackOnBoss() {
        if (game.gameStateManager.useEnergy(80)) {
            bossHealth -= 50
            particleSystem.createSoundWave(bossPosition.x, bossPosition.y, 3f)
            particleSystem.createMusicNotes(bossPosition.x, bossPosition.y, 10)
            game.audioManager.playSound("ability", 1.5f)

            Gdx.app.log("BossBattle", "УЛЬТИМЕЙТ по боссу! Урон: 50")
        }
    }

    private fun winBattle() {
        // Победа над Барковым
        game.gameStateManager.completeLevel("final_boss")
        game.audioManager.stopMusic()

        // Переход к эпилогу
        game.setScreen(EpilogueScreen(game))
    }

    private fun loseBattle() {
        // Поражение - перезапуск битвы
        bossHealth = bossMaxHealth
        game.gameStateManager.playerHealth = 100
        game.gameStateManager.playerEnergy = 100

        Gdx.app.log("BossBattle", "Битва перезапущена")
    }

    override fun dispose() {
        font.dispose()
    }
}