package com.lionido.noizevoice.managers

import com.lionido.noizevoice.NoizeGame

class GameStateManager(private val game: NoizeGame) {

    // Состояние игрока
    var playerHealth = 100
    var playerEnergy = 100
    var collectedCassettes = mutableSetOf<String>()
    var unlockedAbilities = mutableSetOf<String>()
    var completedLevels = mutableSetOf<String>()
    var foundSecrets = mutableSetOf<String>()
    var playTime = 0f

    // Прогресс по актам
    var currentAct = 0
    var currentLevel = 0
    var isGameCompleted = false

    // Биографические элементы
    var unlockedMemories = mutableSetOf<String>()
    var readDiaries = mutableSetOf<String>()
    var watchedCutscenes = mutableSetOf<String>()

    // Треки и способности
    enum class Ability {
        MAKE_SOME_NOIZE,    // Основная атака звуком
        VIDIHAY,            // Стелс и восстановление
        VSELENNAYA,         // Телепортация и головоломки
        IGRA_SLOV,          // Ритм-атаки и диалоги
        ULTIMATE_COMBO      // Комбо всех кассет
    }

    enum class Track(val displayName: String, val fileName: String) {
        MAKE_SOME_NOIZE("Make Some Noize", "make_some_noize"),
        VIDIHAY("Выдыхай", "vidihay"),
        VSELENNAYA("Вселенная бесконечна?", "vselennaya"),
        IGRA_SLOV("Игра слов", "igra_slov"),
        MOE_MORE("Моё море", "moe_more"),
        MERCEDES("Mercedes S666", "mercedes")
    }

    fun collectCassette(cassetteId: String) {
        if (collectedCassettes.add(cassetteId)) {
            game.audioManager.playSound("cassette_pickup")
            Gdx.app.log("GameState", "Собрана кассета: $cassetteId")

            // Разблокировка способностей по кассетам
            when (cassetteId) {
                "make_some_noize" -> unlockAbility(Ability.MAKE_SOME_NOIZE)
                "vidihay" -> unlockAbility(Ability.VIDIHAY)
                "vselennaya" -> unlockAbility(Ability.VSELENNAYA)
                "igra_slov" -> unlockAbility(Ability.IGRA_SLOV)
            }

            // Ультимейт разблокируется при сборе всех кассет
            if (collectedCassettes.size >= 4) {
                unlockAbility(Ability.ULTIMATE_COMBO)
            }
        }
    }

    fun unlockAbility(ability: Ability) {
        if (unlockedAbilities.add(ability.name)) {
            game.audioManager.playSound("ability")
            Gdx.app.log("GameState", "Разблокирована способность: ${ability.name}")
        }
    }

    fun hasAbility(ability: Ability): Boolean {
        return unlockedAbilities.contains(ability.name)
    }

    fun completeLevel(levelId: String) {
        if (completedLevels.add(levelId)) {
            Gdx.app.log("GameState", "Уровень завершен: $levelId")
        }
    }

    fun unlockMemory(memoryId: String) {
        if (unlockedMemories.add(memoryId)) {
            Gdx.app.log("GameState", "Разблокировано воспоминание: $memoryId")
        }
    }

    fun readDiary(diaryId: String) {
        if (readDiaries.add(diaryId)) {
            Gdx.app.log("GameState", "Прочитан дневник: $diaryId")
        }
    }

    fun watchCutscene(cutsceneId: String) {
        if (watchedCutscenes.add(cutsceneId)) {
            Gdx.app.log("GameState", "Просмотрена кат-сцена: $cutsceneId")
        }
    }

    fun takeDamage(damage: Int) {
        playerHealth = (playerHealth - damage).coerceAtLeast(0)
        if (playerHealth <= 0) {
            // Логика смерти игрока
            game.audioManager.playSound("hurt")
        }
    }

    fun heal(amount: Int) {
        playerHealth = (playerHealth + amount).coerceAtMost(100)
    }

    fun useEnergy(amount: Int): Boolean {
        return if (playerEnergy >= amount) {
            playerEnergy -= amount
            true
        } else {
            false
        }
    }

    fun restoreEnergy(amount: Int) {
        playerEnergy = (playerEnergy + amount).coerceAtMost(100)
    }

    fun getProgress(): Float {
        val totalLevels = 30 // Примерное количество уровней
        return completedLevels.size.toFloat() / totalLevels
    }

    fun getCassetteProgress(): String {
        return "${collectedCassettes.size}/6 кассет"
    }

    fun getMemoryProgress(): String {
        return "${unlockedMemories.size} воспоминаний"
    }

    fun update(deltaTime: Float) {
        playTime += deltaTime

        // Постепенное восстановление энергии
        if (playerEnergy < 100) {
            restoreEnergy(1)
        }
    }

    // Сохранение и загрузка (для будущей реализации)
    fun saveGame() {
        // TODO: Сохранение в SharedPreferences
    }

    fun loadGame() {
        // TODO: Загрузка из SharedPreferences
    }
}