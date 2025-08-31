package com.lionido.noizevoice.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.math.Rectangle
import com.lionido.noizevoice.NoizeGame
import com.lionido.noizevoice.entities.Player
import com.lionido.noizevoice.entities.Enemy
import com.lionido.noizevoice.entities.Cassette
import com.lionido.noizevoice.managers.GameStateManager

class GameplayScreen(game: NoizeGame, private val levelId: String) : BaseScreen(game) {

    private lateinit var player: Player
    private lateinit var hudFont: BitmapFont
    private val enemies = mutableListOf<Enemy>()
    private val cassettes = mutableListOf<Cassette>()
    private val platforms = mutableListOf<Rectangle>()

    private var isPaused = false
    private var dialogueActive = false
    private var currentDialogue = ""

    override fun show() {
        // Инициализация HUD
        hudFont = BitmapFont()
        hudFont.color = Color.WHITE
        hudFont.data.setScale(1.2f)

        // Создание игрока
        player = Player(game)

        // Загрузка уровня
        loadLevel(levelId)

        // Музыка в зависимости от уровня
        val music = when {
            levelId.contains("act1") -> "make_some_noize"
            levelId.contains("act2") -> "vselennaya"
            levelId.contains("act3") -> "vidihay"
            levelId.contains("act4") -> "igra_slov"
            levelId.contains("act5") -> "mercedes"
            else -> "make_some_noize"
        }
        game.audioManager.playMusic(music)

        Gdx.app.log("Gameplay", "Уровень $levelId загружен")
    }

    private fun loadLevel(levelId: String) {
        // Создание платформ (базовая геометрия уровня)
        platforms.add(Rectangle(0f, 0f, NoizeGame.GAME_WIDTH, 100f)) // Земля
        platforms.add(Rectangle(300f, 200f, 200f, 50f)) // Платформа
        platforms.add(Rectangle(600f, 350f, 150f, 50f)) // Высокая платформа

        // Размещение врагов в зависимости от акта
        when {
            levelId.contains("act1") -> {
                enemies.add(Enemy(game, 400f, 150f, Enemy.EnemyType.NOISE_MAKER))
                enemies.add(Enemy(game, 700f, 150f, Enemy.EnemyType.NOISE_MAKER))
            }
            levelId.contains("act2") -> {
                enemies.add(Enemy(game, 500f, 150f, Enemy.EnemyType.SILENCER))
            }
            levelId.contains("act3") -> {
                enemies.add(Enemy(game, 300f, 150f, Enemy.EnemyType.CRITIC))
                enemies.add(Enemy(game, 800f, 150f, Enemy.EnemyType.SILENCER))
            }
        }

        // Размещение кассет
        when {
            levelId.contains("act1") -> {
                cassettes.add(Cassette(game, 250f, 250f, "make_some_noize"))
            }
            levelId.contains("act2") -> {
                cassettes.add(Cassette(game, 650f, 400f, "vselennaya"))
            }
            levelId.contains("act3") -> {
                cassettes.add(Cassette(game, 450f, 200f, "vidihay"))
            }
        }
    }

    override fun render(delta: Float) {
        if (!isPaused) {
            updateGame(delta)
        }

        renderGame()
        renderHUD()

        handleInput()
    }

    private fun updateGame(delta: Float) {
        // Обновление игрока
        player.update(delta)

        // Обновление врагов
        enemies.forEach { it.update(delta) }

        // Проверка коллизий
        checkCollisions()

        // Проверка завершения уровня
        if (cassettes.isEmpty() && enemies.all { it.isDefeated }) {
            completeLevel()
        }
    }

    private fun checkCollisions() {
        // Коллизии игрока с врагами
        enemies.forEach { enemy ->
            if (!enemy.isDefeated && player.hitbox.overlaps(enemy.hitbox)) {
                if (player.isAttacking) {
                    enemy.takeDamage(25)
                    game.audioManager.playSound("attack")
                } else {
                    game.gameStateManager.takeDamage(10)
                    game.audioManager.playSound("hurt")
                }
            }
        }

        // Коллизии игрока с кассетами
        cassettes.removeAll { cassette ->
            if (player.hitbox.overlaps(cassette.hitbox)) {
                game.gameStateManager.collectCassette(cassette.trackId)
                showDialogue("Собрана кассета: ${cassette.displayName}")
                true
            } else {
                false
            }
        }
    }

    private fun renderGame() {
        game.batch.begin()

        // Рендер фона
        drawBackground()

        // Рендер платформ
        // TODO: Добавить спрайты платформ

        // Рендер кассет
        cassettes.forEach { it.render(game.batch) }

        // Рендер врагов
        enemies.forEach { it.render(game.batch) }

        // Рендер игрока
        player.render(game.batch)

        // Диалог
        if (dialogueActive) {
            renderDialogue()
        }

        game.batch.end()
    }

    private fun drawBackground() {
        // Фон в зависимости от акта
        when {
            levelId.contains("act1") -> {
                // Городские улицы - граффити, билборды
            }
            levelId.contains("act2") -> {
                // Общежитие РГГУ
            }
            levelId.contains("act3") -> {
                // Беззвучная зона - серый мир
            }
        }
    }

    private fun renderHUD() {
        game.batch.begin()

        // Здоровье
        hudFont.color = if (game.gameStateManager.playerHealth > 30) Color.GREEN else Color.RED
        hudFont.draw(game.batch, "HP: ${game.gameStateManager.playerHealth}", 20f, NoizeGame.GAME_HEIGHT - 20f)

        // Энергия
        hudFont.color = if (game.gameStateManager.playerEnergy > 20) Color.BLUE else Color.ORANGE
        hudFont.draw(game.batch, "Энергия: ${game.gameStateManager.playerEnergy}", 20f, NoizeGame.GAME_HEIGHT - 60f)

        // Кассеты
        hudFont.color = Color.YELLOW
        hudFont.draw(game.batch, game.gameStateManager.getCassetteProgress(), 20f, NoizeGame.GAME_HEIGHT - 100f)

        // Способности
        hudFont.color = Color.CYAN
        val abilities = mutableListOf<String>()
        if (game.gameStateManager.hasAbility(GameStateManager.Ability.MAKE_SOME_NOIZE)) abilities.add("Z-Шум")
        if (game.gameStateManager.hasAbility(GameStateManager.Ability.VIDIHAY)) abilities.add("C-Дыхание")
        if (game.gameStateManager.hasAbility(GameStateManager.Ability.VSELENNAYA)) abilities.add("V-Телепорт")
        if (game.gameStateManager.hasAbility(GameStateManager.Ability.IGRA_SLOV)) abilities.add("B-Слова")
        if (game.gameStateManager.hasAbility(GameStateManager.Ability.ULTIMATE_COMBO)) abilities.add("Q-УЛЬТРА")

        if (abilities.isNotEmpty()) {
            hudFont.draw(game.batch, abilities.joinToString(" | "), 20f, NoizeGame.GAME_HEIGHT - 140f)
        }

        // Управление
        hudFont.color = Color.LIGHT_GRAY
        hudFont.draw(game.batch, "WASD/Стрелки-движение, X-атака, ESC-пауза", 20f, 40f)

        game.batch.end()
    }

    private fun renderDialogue() {
        // Диалоговое окно
        hudFont.color = Color.WHITE
        hudFont.draw(
            game.batch,
            currentDialogue,
            50f,
            NoizeGame.GAME_HEIGHT * 0.2f,
            NoizeGame.GAME_WIDTH - 100f,
            Align.center,
            true
        )
    }

    private fun showDialogue(text: String) {
        currentDialogue = text
        dialogueActive = true
        // Диалог исчезает через 3 секунды
        Gdx.app.postRunnable {
            Thread.sleep(3000)
            dialogueActive = false
        }
    }

    private fun handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            isPaused = !isPaused
            if (isPaused) {
                game.audioManager.pauseMusic()
                showPauseMenu()
            } else {
                game.audioManager.resumeMusic()
            }
        }
    }

    private fun showPauseMenu() {
        // TODO: Реализовать меню паузы
        Gdx.app.log("Gameplay", "Игра поставлена на паузу")
    }

    private fun completeLevel() {
        game.gameStateManager.completeLevel(levelId)

        // Переход к следующему уровню или кат-сцене
        val nextLevel = when (levelId) {
            "act1_level1" -> "act1_level2"
            "act1_level2" -> "act2_level1"
            "act2_level1" -> "act3_level1"
            else -> "main_menu"
        }

        if (nextLevel == "main_menu") {
            game.setScreen(MainMenuScreen(game))
        } else {
            game.setScreen(GameplayScreen(game, nextLevel))
        }
    }

    override fun dispose() {
        hudFont.dispose()
    }
}