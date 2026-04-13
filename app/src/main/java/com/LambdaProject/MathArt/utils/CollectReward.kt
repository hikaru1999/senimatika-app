package com.LambdaProject.MathArt.utils

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.LambdaProject.MathArt.data.*

fun collectReward() {
    var currentReward by mutableStateOf<Reward?>(null)
    var currentChestPos by mutableStateOf<Pair<Int, Int>?>(null)
    var isChestOpen by mutableStateOf(false)
    var inventory by mutableStateOf(Inventory())


    val reward = currentReward ?: return
    val chestPos = currentChestPos
    val openedChests = mutableStateListOf<Pair<Int, Int>>()

    inventory = when (reward.type) {
        RewardType.COIN -> inventory.copy(
            coins = inventory.coins + reward.amount
        )

        RewardType.SCROLL -> inventory.copy(
            scrolls = inventory.scrolls + reward.content
        )

        RewardType.POWER_UP -> inventory.copy(
            powerUps = inventory.powerUps + reward.powerUp!!
        )
    }

    chestPos?.let { openedChests.add(it) }

    isChestOpen = false
    currentReward = null
}