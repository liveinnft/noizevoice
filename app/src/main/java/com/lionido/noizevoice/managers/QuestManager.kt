enum class QuestType {
    MAIN_STORY,
    SIDE_QUEST,
    COLLECTION,
    EXPLORATION,
    RHYTHM_CHALLENGE,
    BIOGRAPHY
}

enum class RewardType {
    HEALTH, ENERGY, CASSETTE, MEMORY, DIARY, ABILITY
}

init {
    initializeQuests()
}

private fun initializeQuests() {
    // Главные квесты
    addQuest(Quest(
        "main_retrieve_voice",
        "Верни свой голос",
        "Собери все украденные кассеты и восстанови свою музыку",
        QuestType.MAIN_STORY,
        mutableListOf(
            QuestObjective("collect_all_cassettes", "Собрать все кассеты", 6),
            QuestObjective("defeat_barkov", "Победить Анатолия Баркова", 1)
        ),
        listOf(
            QuestReward(RewardType.ABILITY, "ULTIMATE_COMBO")
        ),
        true
    ))

    // Побочные квесты
    addQuest(Quest(
        "side_street_performances",
        "Уличные выступления",
        "Найди и активируй все скрытые сцены в первом акте",
        QuestType.SIDE_QUEST,
        mutableListOf(
            QuestObjective("find_street_stages", "Найти уличные сцены", 3)
        ),
        listOf(
            QuestReward(RewardType.ENERGY, "bonus_energy", 50),
            QuestReward(RewardType.MEMORY, "street_performer_memory")
        )
    ))

    addQuest(Quest(
        "side_dorm_memories",
        "Воспоминания общежития",
        "Прочитай все дневники из студенческих лет",
        QuestType.BIOGRAPHY,
        mutableListOf(
            QuestObjective("read_dorm_diaries", "Прочитать дневники", 5)
        ),
        listOf(
            QuestReward(RewardType.MEMORY, "university_complete_memory"),
            QuestReward(RewardType.HEALTH, "bonus_health", 25)
        )
    ))

    addQuest(Quest(
        "collection_all_secrets",
        "Тайны прошлого",
        "Найди все скрытые секреты в игре",
        QuestType.COLLECTION,
        mutableListOf(
            QuestObjective("find_secrets", "Найти секреты", 15)
        ),
        listOf(
            QuestReward(RewardType.CASSETTE, "bonus_track"),
            QuestReward(RewardType.MEMORY, "complete_biography")
        )
    ))

    addQuest(Quest(
        "rhythm_master",
        "Мастер ритма",
        "Получи оценку 'S' в 5 ритм-битвах",
        QuestType.RHYTHM_CHALLENGE,
        mutableListOf(
            QuestObjective("perfect_rhythm_battles", "Идеальные ритм-битвы", 5)
        ),
        listOf(
            QuestReward(RewardType.ABILITY, "RHYTHM_MASTER"),
            QuestReward(RewardType.ENERGY, "permanent_energy", 20)
        )
    ))
}

private fun addQuest(quest: Quest) {
    activeQuests[quest.id] = quest
    Gdx.app.log("QuestManager", "Добавлен квест: ${quest.title}")
}

fun updateObjective(objectiveId: String, increment: Int = 1) {
    activeQuests.values.forEach { quest ->
        quest.objectives.forEach { objective ->
            if (objective.id == objectiveId && !objective.isCompleted) {
                objective.currentValue += increment

                if (objective.currentValue >= objective.targetValue) {
                    objective.isCompleted = true
                    game.audioManager.playSound("ability", 0.8f)
                    Gdx.app.log("QuestManager", "Цель выполнена: ${objective.description}")

                    checkQuestCompletion(quest)
                }
            }
        }
    }
}

private fun checkQuestCompletion(quest: Quest) {
    if (quest.objectives.all { it.isCompleted } && !quest.isCompleted) {
        quest.isCompleted = true
        completedQuests.add(quest.id)
        activeQuests.remove(quest.id)

        // Выдача наград
        quest.rewards.forEach { reward ->
            when (reward.type) {
                RewardType.HEALTH -> game.gameStateManager.heal(reward.amount)
                RewardType.ENERGY -> game.gameStateManager.restoreEnergy(reward.amount)
                RewardType.CASSETTE -> game.gameStateManager.collectCassette(reward.value)
                RewardType.MEMORY -> game.gameStateManager.unlockMemory(reward.value)
                RewardType.DIARY -> game.gameStateManager.readDiary(reward.value)
                RewardType.ABILITY -> {
                    val ability = GameStateManager.Ability.valueOf(reward.value)
                    game.gameStateManager.unlockAbility(ability)
                }
            }
        }

        game.audioManager.playSound("cassette_pickup")
        Gdx.app.log("QuestManager", "Квест завершен: ${quest.title}")
    }
}

// Специальные методы для отслеживания событий
fun onCassetteCollected(cassetteId: String) {
    updateObjective("collect_all_cassettes")
}

fun onEnemyDefeated(enemyType: Enemy.EnemyType) {
    if (enemyType == Enemy.EnemyType.CRITIC) {
        updateObjective("defeat_barkov")
    }
}

fun onSecretFound(secretId: String) {
    updateObjective("find_secrets")
    updateObjective("find_street_stages")
}

fun onDiaryRead(diaryId: String) {
    updateObjective("read_dorm_diaries")
}

fun onRhythmBattleCompleted(grade: String) {
    if (grade == "S") {
        updateObjective("perfect_rhythm_battles")
    }
}

fun getActiveQuests(): List<Quest> {
    return activeQuests.values.toList()
}

fun getCompletedQuests(): Set<String> {
    return completedQuests
}

fun getQuestProgress(questId: String): Float {
    val quest = activeQuests[questId] ?: return 1f
    val totalObjectives = quest.objectives.size
    val completedObjectives = quest.objectives.count { it.isCompleted }
    return completedObjectives.toFloat() / totalObjectives
}

fun hasActiveMainQuests(): Boolean {
    return activeQuests.values.any { it.isMainQuest && !it.isCompleted }
}
}package com.lionido.noizevoice.managers

import com.badlogic.gdx.Gdx
import com.lionido.noizevoice.NoizeGame

class QuestManager(private val game: NoizeGame) {

    private val activeQuests = mutableMapOf<String, Quest>()
    private val completedQuests = mutableSetOf<String>()

    data class Quest(
        val id: String,
        val title: String,
        val description: String,
        val type: QuestType,
        val objectives: MutableList<QuestObjective>,
        val rewards: List<QuestReward>,
        val isMainQuest: Boolean = false,
        var isCompleted: Boolean = false
    )

    data class QuestObjective(
        val id: String,
        val description: String,
        val targetValue: Int,
        var currentValue: Int = 0,
        var isCompleted: Boolean = false
    )

    data class QuestReward(
        val type: RewardType,
        val value: String,
        val amount: Int = 1
    )

    enum class QuestType {
        MAIN_