package io.zkz.mc.uhc.task

import io.zkz.mc.gametools.injection.inject
import io.zkz.mc.gametools.util.WorldSyncUtils
import io.zkz.mc.minigamemanager.task.MinigameTask
import io.zkz.mc.uhc.game.UhcStates
import io.zkz.mc.uhc.game.uhcRound
import io.zkz.mc.uhc.settings.SettingsManager

class WorldBorderStateCheckerTask : MinigameTask(1, 1) {
    private val settingsManager by inject<SettingsManager>()

    private var lastWorldborder = -1

    override fun run() {
        val currentState = minigameService.currentState

        // TODO: proper cancellation check
        // if (currentState != MinigameState.IN_GAME && currentState != MinigameState.IN_GAME_2) {
        if (!currentState.isInGame) {
            this.cancel()
            return
        }

        val currentWorldborder = WorldSyncUtils.worldBorderSize.toInt()
        if (currentWorldborder != lastWorldborder) {
            if (currentWorldborder <= settingsManager.worldBorderDistance2.value) {
                this.cancel()
                minigameService.setState(UhcStates.WB_CLOSING_2)
            } else if (currentWorldborder <= settingsManager.worldBorderDistance3.value) {
                this.cancel()
                minigameService.uhcRound.onWorldBorderStoppedMoving()
            }
            lastWorldborder = currentWorldborder
        }
    }
}
