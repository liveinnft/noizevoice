package com.lionido.noizevoice.managers

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences

class SaveSystem(private val gameStateManager: GameStateManager) {

    private val prefs: Preferences = Gdx.app.getPreferences("noize-game-save")

    fun saveGame() {
        try {
            // Основное состояние игрока
            prefs.putInteger("playerHealth", gameStateManager.playerHealth)
            prefs.putInteger("playerEnergy", gameStateManager.playerEnergy)
            prefs.putInteger("currentAct", gameStateManager.currentAct)
            prefs.putInteger("currentLevel", gameStateManager.currentLevel)
            prefs.putFloat("playTime", gameStateManager.playTime)
            prefs.putBoolean("gameCompleted", gameStateManager.isGameCompleted)

            // Собранные кассеты
            val cassettesString = gameStateManager.collectedCassettes.joinToString(",")
            prefs.putString("collectedCassettes", cassettesString)

            // Разблокированные способности
            val abilitiesString = gameStateManager.unlockedAbilities.joinToString(",")
            prefs.putString("unlockedAbilities", abilitiesString)

            // Завершенные уровни
            val levelsString = gameStateManager.completedLevels.joinToString(",")
            prefs.putString("completedLevels", levelsString)

            // Найденные секреты
            val secretsString = gameStateManager.foundSecrets.joinToString(",")
            prefs.putString("foundSecrets", secretsString)

            // Биографические элементы
            val memoriesString = gameStateManager.unlockedMemories.joinToString(",")
            prefs.putString("unlockedMemories", memoriesString)

            val diariesString = gameStateManager.readDiaries.joinToString(",")
            prefs.putString("readDiaries", diariesString)

            val cutscenesString = gameStateManager.watchedCutscenes.joinToString(",")
            prefs.putString("watchedCutscenes", cutscenesString)

            // Сохранение времени
            prefs.putLong("saveTime", System.currentTimeMillis())

            prefs.flush()

            Gdx.app.log("SaveSystem", "Игра сохранена успешно")

        } catch (e: Exception) {
            Gdx.app.error("SaveSystem", "Ошибка сохранения: ${e.message}")
        }
    }

    fun loadGame(): Boolean {
        try {
            if (!hasSaveData()) {
                Gdx.app.log("SaveSystem", "Сохранений не найдено")
                return false
            }

            // Загрузка основного состояния
            gameStateManager.playerHealth = prefs.getInteger("playerHealth", 100)
            gameStateManager.playerEnergy = prefs.getInteger("playerEnergy", 100)
            gameStateManager.currentAct = prefs.getInteger("currentAct", 0)
            gameStateManager.currentLevel = prefs.getInteger("currentLevel", 0)
            gameStateManager.playTime = prefs.getFloat("playTime", 0f)
            gameStateManager.isGameCompleted = prefs.getBoolean("gameCompleted", false)

            // Загрузка коллекций
            loadStringSet("collectedCassettes", gameStateManager.collectedCassettes)
            loadStringSet("unlockedAbilities", gameStateManager.unlockedAbilities)
            loadStringSet("completedLevels", gameStateManager.completedLevels)
            loadStringSet("foundSecrets", gameStateManager.foundSecrets)
            loadStringSet("unlockedMemories", gameStateManager.unlockedMemories)
            loadStringSet("readDiaries", gameStateManager.readDiaries)
            loadStringSet("watchedCutscenes", gameStateManager.watchedCutscenes)

            val saveTime = prefs.getLong("saveTime", 0L)
            Gdx.app.log("SaveSystem", "Игра загружена (сохранение от ${java.util.Date(saveTime)})")

            return true

        } catch (e: Exception) {
            Gdx.app.error("SaveSystem", "Ошибка загрузки: ${e.message}")
            return false
        }
    }

    private fun loadStringSet(key: String, targetSet: MutableSet<String>) {
        val dataString = prefs.getString(key, "")
        if (dataString.isNotEmpty()) {
            targetSet.addAll(dataString.split(","))
        }
    }

    fun hasSaveData(): Boolean {
        return prefs.contains("saveTime")
    }

    fun deleteSave() {
        prefs.clear()
        prefs.flush()
        Gdx.app.log("SaveSystem", "Сохранение удалено")
    }

    fun getSaveInfo(): String {
        if (!hasSaveData()) return "Нет сохранений"

        val saveTime = prefs.getLong("saveTime", 0L)
        val playTime = prefs.getFloat("playTime", 0f)
        val cassettes = prefs.getString("collectedCassettes", "").split(",").size
        val completedLevels = prefs.getString("completedLevels", "").split(",").size

        return """
            Сохранение от: ${java.util.Date(saveTime)}
            Время игры: ${(playTime / 60).toInt()} мин
            Кассет: $cassettes/6
            Уровней: $completedLevels
        """.trimIndent()
    }

    // Автосохранение
    fun autoSave() {
        if (gameStateManager.playTime > 0) {
            saveGame()
        }
    }
}