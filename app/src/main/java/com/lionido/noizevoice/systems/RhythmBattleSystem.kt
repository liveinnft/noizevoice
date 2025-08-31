// Дополнительные ноты для сложных моментов трека
if (currentTrack == "igra_slov" && battleTimer > 10f && MathUtils.random() < 0.3f) {
    // Быстрые ноты для "Игры слов"
    val lane2 = MathUtils.random(0, 3)
    notes.add(RhythmNote(200f + lane2 * 100f, NoizeGame.GAME_HEIGHT + 100f, lane2, 400f))
}
}

private fun handleRhythmInput() {
    for (i in hitKeys.indices) {
        if (Gdx.input.isKeyJustPressed(hitKeys[i])) {
            checkNoteHit(i)
        }
    }
}

private fun checkNoteHit(lane: Int) {
    val notesInLane = notes.filter { it.lane == lane && !it.isHit }
    if (notesInLane.isEmpty()) {
        // Промах
        combo = 0
        return
    }

    val closestNote = notesInLane.minByOrNull { kotlin.math.abs(it.y - hitZoneY) }
    closestNote?.let { note ->
        when {
            note.isInPerfectZone(hitZoneY, perfectZone) -> {
                hitPerfect(note)
            }
            note.isInGoodZone(hitZoneY, goodZone) -> {
                hitGood(note)
            }
            else -> {
                combo = 0
            }
        }
    }
}

private fun hitPerfect(note: RhythmNote) {
    note.isHit = true
    score += 100 + (combo * 10)
    combo++
    game.audioManager.playSound("rhythm_hit", 1f)
    Gdx.app.log("RhythmBattle", "PERFECT! Комбо: $combo")
}

private fun hitGood(note: RhythmNote) {
    note.isHit = true
    score += 50 + (combo * 5)
    combo++
    game.audioManager.playSound("rhythm_hit", 0.7f)
    Gdx.app.log("RhythmBattle", "GOOD! Комбо: $combo")
}

fun render(batch: SpriteBatch) {
    if (!isActive) return

    batch.end()

    val shapeRenderer = ShapeRenderer()
    shapeRenderer.projectionMatrix = batch.projectionMatrix

    // Рендер зоны попадания
    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

    // Линии для дорожек
    for (i in 0..3) {
        val x = 200f + i * 100f
        shapeRenderer.color = Color.DARK_GRAY
        shapeRenderer.rect(x - 25f, 0f, 50f, NoizeGame.GAME_HEIGHT)
    }

    // Зона попадания
    shapeRenderer.color = Color(1f, 1f, 1f, 0.3f)
    shapeRenderer.rect(175f, hitZoneY - goodZone, 350f, goodZone * 2)

    shapeRenderer.color = Color(0f, 1f, 0f, 0.5f)
    shapeRenderer.rect(175f, hitZoneY - perfectZone, 350f, perfectZone * 2)

    // Рендер нот
    notes.forEach { note ->
        if (!note.isHit) {
            shapeRenderer.color = when (note.lane) {
                0 -> Color.RED
                1 -> Color.BLUE
                2 -> Color.GREEN
                3 -> Color.YELLOW
                else -> Color.WHITE
            }
            shapeRenderer.circle(note.x, note.y, 20f)
        }
    }

    shapeRenderer.end()
    batch.begin()

    // HUD ритм-битвы
    font.color = Color.WHITE
    font.draw(batch, "Счет: $score", 20f, NoizeGame.GAME_HEIGHT - 20f)
    font.draw(batch, "Комбо: $combo", 20f, NoizeGame.GAME_HEIGHT - 60f)

    // Клавиши управления
    font.color = Color.LIGHT_GRAY
    font.draw(batch, "A    S    D    F", 150f, 100f, 400f, Align.center, false)
}

private fun endBattle() {
    isActive = false

    // Награды за битву
    val grade = when {
        score >= 5000 -> "S"
        score >= 3000 -> "A"
        score >= 2000 -> "B"
        score >= 1000 -> "C"
        else -> "D"
    }

    Gdx.app.log("RhythmBattle", "Битва завершена! Оценка: $grade, Счет: $score")

    // Бонусы в зависимости от результата
    when (grade) {
        "S" -> {
            game.gameStateManager.heal(50)
            game.gameStateManager.restoreEnergy(50)
        }
        "A" -> {
            game.gameStateManager.heal(30)
            game.gameStateManager.restoreEnergy(30)
        }
        "B" -> {
            game.gameStateManager.heal(20)
            game.gameStateManager.restoreEnergy(20)
        }
        else -> {
            game.gameStateManager.heal(10)
        }
    }
}

fun isActive(): Boolean = isActive

fun dispose() {
    font.dispose()
}
}package com.lionido.noizevoice.systems

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Align
import com.lionido.noizevoice.NoizeGame

class RhythmBattleSystem(private val game: NoizeGame) {

    private lateinit var font: BitmapFont
    private val notes = mutableListOf<RhythmNote>()
    private var score = 0
    private var combo = 0
    private var isActive = false
    private var battleTimer = 0f
    private var noteSpawnTimer = 0f
    private var currentTrack = ""

    // Зоны попадания для нот
    private val hitZoneY = 200f
    private val perfectZone = 50f
    private val goodZone = 80f
    private val hitKeys = listOf(Input.Keys.A, Input.Keys.S, Input.Keys.D, Input.Keys.F)

    data class RhythmNote(
        var x: Float,
        var y: Float,
        val lane: Int, // 0-3 для клавиш A,S,D,F
        val speed: Float = 300f
    ) {
        var isHit = false

        fun update(deltaTime: Float) {
            y -= speed * deltaTime
        }

        fun isInPerfectZone(zoneY: Float, perfectRange: Float): Boolean {
            return kotlin.math.abs(y - zoneY) <= perfectRange
        }

        fun isInGoodZone(zoneY: Float, goodRange: Float): Boolean {
            return kotlin.math.abs(y - zoneY) <= goodRange
        }

        fun isOffScreen(): Boolean {
            return y < -50f
        }
    }

    init {
        font = BitmapFont()
        font.color = Color.WHITE
        font.data.setScale(1.5f)
    }

    fun startBattle(trackName: String) {
        isActive = true
        currentTrack = trackName
        battleTimer = 0f
        score = 0
        combo = 0
        notes.clear()

        game.audioManager.startRhythmBattle(trackName)
        Gdx.app.log("RhythmBattle", "Начинается ритм-битва с треком: $trackName")
    }

    fun update(deltaTime: Float) {
        if (!isActive) return

        battleTimer += deltaTime
        noteSpawnTimer += deltaTime

        // Спавн нот в ритме музыки
        spawnNotes()

        // Обновление нот
        notes.forEach { it.update(deltaTime) }

        // Удаление нот за экраном
        val iterator = notes.iterator()
        while (iterator.hasNext()) {
            val note = iterator.next()
            if (note.isOffScreen() && !note.isHit) {
                iterator.remove()
                combo = 0 // Сброс комбо при пропуске
            }
        }

        // Обработка ввода
        handleRhythmInput()

        // Проверка завершения битвы
        if (!game.audioManager.isMusicPlaying() && notes.isEmpty()) {
            endBattle()
        }
    }

    private fun spawnNotes() {
        // Спавн нот в зависимости от трека
        val bpm = when (currentTrack) {
            "make_some_noize" -> 140f
            "vidihay" -> 90f
            "vselennaya" -> 120f
            "igra_slov" -> 160f
            "mercedes" -> 130f
            else -> 120f
        }

        val noteInterval = 60f / bpm // Интервал между нотами в секундах

        if (noteSpawnTimer >= noteInterval) {
            val lane = MathUtils.random(0, 3)
            val x = 200f + lane * 100f
            notes.add(RhythmNote(x, NoizeGame.GAME_HEIGHT + 50f, lane))
            noteSpawnTimer = 0f
        }

// Дополн