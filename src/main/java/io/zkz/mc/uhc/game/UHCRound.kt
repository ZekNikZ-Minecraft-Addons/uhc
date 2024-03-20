package io.zkz.mc.uhc.game

import io.zkz.mc.minigamemanager.minigame.MinigameService
import io.zkz.mc.minigamemanager.minigame.Round

class UHCRound : Round() {
    val currentWorldBorderSpeed: Double = 0.0
    val currentWorldBorderTarget: Double = 0.0
}

val MinigameService.uhcRound
    get() = getCurrentRoundTyped<UHCRound>()
