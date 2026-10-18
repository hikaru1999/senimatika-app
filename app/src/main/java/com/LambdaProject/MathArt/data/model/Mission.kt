package com.LambdaProject.MathArt.data.model

enum class MissionType {
    REACH_FINISH,
    DEFEAT_BOSS,
    COLLECT_CHEST,
    NO_POWER_UP,
    NO_BOSS_COMBAT;

    companion object {
        fun fromString(typeStr: String?): MissionType {
            return when (typeStr?.uppercase()) {
                "REACH_FINISH", "FINISH" -> REACH_FINISH
                "DEFEAT_BOSS", "BOSS" -> DEFEAT_BOSS
                "COLLECT_CHEST", "CHEST" -> COLLECT_CHEST
                "NO_POWER_UP" -> NO_POWER_UP
                "NO_BOSS_COMBAT" -> NO_BOSS_COMBAT
                else -> REACH_FINISH
            }
        }
    }
}

data class Mission(
    val id: String = "",
    val type: MissionType = MissionType.REACH_FINISH,
    val title: String = "",
    val description: String = "",
    val targetCount: Int = 1,
    val isRequired: Boolean = false,
    val bonusCoins: Int = 0,
    val bonusExp: Int = 0
) {
    companion object {
        fun fromMap(data: Map<String, Any>): Mission {
            val type = MissionType.fromString(data["type"] as? String)
            var rawTitle = data["title"] as? String ?: ""
            if (rawTitle.isBlank()) {
                rawTitle = when (type) {
                    MissionType.REACH_FINISH -> "Capai Bendera Finish"
                    MissionType.DEFEAT_BOSS -> "Kalahkan Boss"
                    MissionType.COLLECT_CHEST -> "Buka Peti Harta"
                    MissionType.NO_POWER_UP -> "Tanpa Power-Up"
                    MissionType.NO_BOSS_COMBAT -> "Tanpa Melawan Boss"
                }
            } else {
                rawTitle = rawTitle.replace("Capai Titik Akhir", "Capai Bendera Finish", ignoreCase = true)
                    .replace("titik akhir", "bendera finish", ignoreCase = true)
            }

            var rawDesc = data["description"] as? String ?: ""
            if (rawDesc.isNotBlank()) {
                rawDesc = rawDesc.replace("titik akhir", "bendera finish", ignoreCase = true)
                    .replace("titik finish", "bendera finish", ignoreCase = true)
            }

            return Mission(
                id = data["id"] as? String ?: "",
                type = type,
                title = rawTitle,
                description = rawDesc,
                targetCount = (data["targetCount"] as? Number)?.toInt() ?: 1,
                isRequired = data["isRequired"] as? Boolean ?: false,
                bonusCoins = (data["bonusCoins"] as? Number)?.toInt() ?: 0,
                bonusExp = (data["bonusExp"] as? Number)?.toInt() ?: 0
            )
        }
    }
}

data class MissionProgress(
    val mission: Mission,
    val currentCount: Int = 0,
    val isCompleted: Boolean = false,
    val isFailed: Boolean = false
)
