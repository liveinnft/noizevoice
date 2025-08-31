package com.lionido.noizevoice.managers

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Rectangle
import com.lionido.noizevoice.NoizeGame
import com.lionido.noizevoice.entities.Enemy
import com.lionido.noizevoice.entities.Cassette

class LevelManager(private val game: NoizeGame) {

    data class LevelData(
        val id: String,
        val name: String,
        val act: Int,
        val backgroundMusic: String,
        val platforms: List<Rectangle>,
        val enemies: List<EnemySpawn>,
        val cassettes: List<CassetteSpawn>,
        val secrets: List<SecretSpawn>,
        val dialogueId: String? = null,
        val nextLevel: String? = null
    )

    data class EnemySpawn(
        val x: Float,
        val y: Float,
        val type: Enemy.EnemyType
    )

    data class CassetteSpawn(
        val x: Float,
        val y: Float,
        val trackId: String
    )

    data class SecretSpawn(
        val x: Float,
        val y: Float,
        val secretId: String,
        val type: SecretType
    )

    enum class SecretType {
        DIARY, MEMORY, BONUS_TRACK, EASTER_EGG
    }

    private val levels = mapOf(
        // Акт 1: Улицы города
        "act1_level1" to LevelData(
            "act1_level1",
            "Первые шаги",
            1,
            "make_some_noize",
            listOf(
                Rectangle(0f, 0f, NoizeGame.GAME_WIDTH, 100f), // Земля
                Rectangle(300f, 200f, 200f, 50f),
                Rectangle(600f, 350f, 150f, 50f),
                Rectangle(900f, 500f, 200f, 50f)
            ),
            listOf(
                EnemySpawn(400f, 150f, Enemy.EnemyType.NOISE_MAKER),
                EnemySpawn(700f, 150f, Enemy.EnemyType.NOISE_MAKER)
            ),
            listOf(
                CassetteSpawn(250f, 250f, "make_some_noize")
            ),
            listOf(
                SecretSpawn(150f, 120f, "first_concert_memory", SecretType.MEMORY)
            ),
            "act1_street",
            "act1_level2"
        ),

        "act1_level2" to LevelData(
            "act1_level2",
            "Городские концерты",
            1,
            "make_some_noize",
            listOf(
                Rectangle(0f, 0f, NoizeGame.GAME_WIDTH, 100f),
                Rectangle(200f, 250f, 300f, 50f),
                Rectangle(800f, 400f, 250f, 50f)
            ),
            listOf(
                EnemySpawn(500f, 150f, Enemy.EnemyType.CRITIC),
                EnemySpawn(1000f, 150f, Enemy.EnemyType.NOISE_MAKER),
                EnemySpawn(300f, 300f, Enemy.EnemyType.MEDIA_SPAMMER)
            ),
            emptyList(),
            listOf(
                SecretSpawn(850f, 450f, "street_performance_diary", SecretType.DIARY)
            ),
            null,
            "act2_level1"
        ),

        // Акт 2: Общежитие РГГУ
        "act2_level1" to LevelData(
            "act2_level1",
            "Студенческие годы",
            2,
            "vselennaya",
            listOf(
                Rectangle(0f, 0f, NoizeGame.GAME_WIDTH, 100f),
                Rectangle(400f, 300f, 400f, 50f),
                Rectangle(100f, 500f, 200f, 50f),
                Rectangle(1200f, 200f, 300f, 50f)
            ),
            listOf(
                EnemySpawn(500f, 150f, Enemy.EnemyType.SILENCER),
                EnemySpawn(1300f, 250f, Enemy.EnemyType.CRITIC)
            ),
            listOf(
                CassetteSpawn(650f, 400f, "vselennaya")
            ),
            listOf(
                SecretSpawn(150f, 550f, "university_memories", SecretType.MEMORY),
                SecretSpawn(450f, 350f, "student_diary", SecretType.DIARY)
            ),
            "act2_dorm",
            "act3_level1"
        ),

        // Акт 3: Беззвучная зона
        "act3_level1" to LevelData(
            "act3_level1",
            "Мир без звука",
            3,
            "vidihay",
            listOf(
                Rectangle(0f, 0f, NoizeGame.GAME_WIDTH, 100f),
                Rectangle(300f, 250f, 200f, 50f),
                Rectangle(700f, 400f, 200f, 50f),
                Rectangle(1100f, 600f, 300f, 50f)
            ),
            listOf(
                EnemySpawn(400f, 150f, Enemy.EnemyType.CENSOR_DRONE),
                EnemySpawn(800f, 450f, Enemy.EnemyType.SILENCER),
                EnemySpawn(1200f, 650f, Enemy.EnemyType.FANATIC_PHANTOM)
            ),
            listOf(
                CassetteSpawn(450f, 200f, "vidihay")
            ),
            listOf(
                SecretSpawn(50f, 120f, "censorship_thoughts", SecretType.MEMORY)
            ),
            "act3_silence",
            "act4_level1"
        ),

        // Акт 4: Дорога (турне)
        "act4_level1" to LevelData(
            "act4_level1",
            "Дорога к сцене",
            4,
            "igra_slov",
            listOf(
                Rectangle(0f, 0f, NoizeGame.GAME_WIDTH, 100f),
                Rectangle(250f, 200f, 150f, 50f),
                Rectangle(500f, 350f, 150f, 50f),
                Rectangle(750f, 500f, 150f, 50f),
                Rectangle(1000f, 300f, 200f, 50f)
            ),
            listOf(
                EnemySpawn(300f, 150f, Enemy.EnemyType.MEDIA_SPAMMER),
                EnemySpawn(600f, 150f, Enemy.EnemyType.NOISE_MAKER),
                EnemySpawn(1100f, 350f, Enemy.EnemyType.CRITIC)
            ),
            listOf(
                CassetteSpawn(550f, 400f, "igra_slov")
            ),
            listOf(
                SecretSpawn(800f, 550f, "tour_diary", SecretType.DIARY),
                SecretSpawn(1150f, 350f, "fan_meeting", SecretType.MEMORY)
            ),
            null,
            "act5_level1"
        ),

        // Акт 5: Узел системы
        "act5_level1" to LevelData(
            "act5_level1",
            "В сердце системы",
            5,
            "mercedes",
            listOf(
                Rectangle(0f, 0f, NoizeGame.GAME_WIDTH, 100f),
                Rectangle(400f, 300f, 300f, 50f),
                Rectangle(900f, 500f, 300f, 50f),
                Rectangle(200f, 700f, 400f, 50f)
            ),
            listOf(
                EnemySpawn(500f, 150f, Enemy.EnemyType.CENSOR_DRONE),
                EnemySpawn(1000f, 550f, Enemy.EnemyType.MEDIA_SPAMMER),
                EnemySpawn(300f, 750f, Enemy.EnemyType.FANATIC_PHANTOM)
            ),
            listOf(
                CassetteSpawn(550f, 350f, "mercedes")
            ),
            listOf(
                SecretSpawn(1100f, 550f, "system_core", SecretType.EASTER_EGG)
            ),
            "act5_system",
            "final_boss"
        ),

        // Финальный босс
        "final_boss" to LevelData(
            "final_boss",
            "Противостояние с Барковым",
            6,
            "mercedes",
            listOf(
                Rectangle(0f, 0f, NoizeGame.GAME_WIDTH, 100f),
                Rectangle(400f, 300f, 800f, 100f) // Арена босса
            ),
            listOf(
                EnemySpawn(NoizeGame.GAME_WIDTH - 200f, 400f, Enemy.EnemyType.CRITIC) // Барков как особый враг
            ),
            listOf(
                CassetteSpawn(600f, 400f, "moe_more")
            ),
            emptyList(),
            "final_barkov",
            "epilogue"
        ),

        // Эпилог
        "epilogue" to LevelData(
            "epilogue",
            "Моё море",
            7,
            "moe_more",
            listOf(
                Rectangle(0f, 0f, NoizeGame.GAME_WIDTH, 100f) // Пляж
            ),
            emptyList(),
            emptyList(),
            listOf(
                SecretSpawn(500f, 150f, "final_reflection", SecretType.MEMORY)
            ),
            "epilogue_sea",
            null
        )
    )

    fun loadLevel(levelId: String): LevelData? {
        val level = levels[levelId]
        if (level != null) {
            Gdx.app.log("LevelManager", "Загружается уровень: ${level.name} (Акт ${level.act})")
        } else {
            Gdx.app.log("LevelManager", "Уровень $levelId не найден!")
        }
        return level
    }

    fun getAllLevels(): Map<String, LevelData> = levels

    fun getLevelsInAct(act: Int): List<LevelData> {
        return levels.values.filter { it.act == act }
    }

    fun getNextLevel(currentLevelId: String): String? {
        return levels[currentLevelId]?.nextLevel
    }

    fun isLevelUnlocked(levelId: String): Boolean {
        val level = levels[levelId] ?: return false

        // Первый уровень всегда доступен
        if (levelId == "act1_level1") return true

        // Проверяем, завершен ли предыдущий уровень
        val previousLevels = levels.values.filter { it.nextLevel == levelId }
        return previousLevels.any { game.gameStateManager.completedLevels.contains(it.id) }
    }

    fun getCurrentAct(): Int {
        val completedLevels = game.gameStateManager.completedLevels
        return when {
            completedLevels.any { it.startsWith("act5") } -> 6
            completedLevels.any { it.startsWith("act4") } -> 5
            completedLevels.any { it.startsWith("act3") } -> 4
            completedLevels.any { it.startsWith("act2") } -> 3
            completedLevels.any { it.startsWith("act1") } -> 2
            else -> 1
        }
    }
}