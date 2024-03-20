package io.zkz.mc.uhc.task

import io.zkz.mc.gametools.hud.ActionBarService
import io.zkz.mc.gametools.injection.inject
import io.zkz.mc.gametools.sound.StandardSounds
import io.zkz.mc.gametools.util.BukkitUtils.forEachPlayer
import io.zkz.mc.gametools.util.WorldSyncUtils
import io.zkz.mc.gametools.util.mm
import io.zkz.mc.minigamemanager.task.MinigameTask
import io.zkz.mc.uhc.game.UhcService
import org.bukkit.entity.Player
import kotlin.math.abs
import kotlin.math.max

class WorldborderWarningTask : MinigameTask(20, 20) {
    private val actionBarService by inject<ActionBarService>()

    private var i = 0

    override fun run() {
        ++i

        // TODO: proper cancellation
        // if (currentState !== MinigameState.IN_GAME && currentState !== MinigameState.IN_GAME_2 && currentState !== MinigameState.IN_GAME_3) {
        if (!minigameService.currentState.isInGame) {
            this.cancel()
            return
        }

        forEachPlayer { player: Player ->
            val worldBorderRadius: Double = WorldSyncUtils.worldBorderSize / 2.0
            val worldBorderSpeed: Double = UhcService.currentWorldBorderSpeed
            val x = player.location.x
            val z = player.location.z
            val playerRadius = max(abs(x), abs(z))

            if (playerRadius <= UhcService.currentWorldBorderTarget / 2) {
                actionBarService.removeMessage(player.uniqueId, "wbWarning1")
                actionBarService.removeMessage(player.uniqueId, "wbWarning2")
                return@forEachPlayer
            }

            val leeway = 20
            if (worldBorderRadius - playerRadius <= worldBorderSpeed * (60 + leeway)) {
                actionBarService.addMessage(
                    player.uniqueId,
                    "wbWarning1",
                    mm("<legacy_red>The world border will pass you in less than 1 minute!"),
                )
                actionBarService.addMessage(
                    player.uniqueId,
                    "wbWarning2",
                    mm("<legacy_dark_red>The world border will pass you in less than 1 minute!"),
                )
                if (i % 2 == 0) {
                    playWarningSoundToPlayer(player)
                }
            } else if (worldBorderRadius - playerRadius <= worldBorderSpeed * (180 + leeway)) {
                actionBarService.addMessage(
                    player.uniqueId,
                    "wbWarning1",
                    mm("<legacy_red>The world border will pass you in less than 3 minutes!"),
                )
                actionBarService.addMessage(
                    player.uniqueId,
                    "wbWarning2",
                    mm("<legacy_dark_red>The world border will pass you in less than 3 minutes!"),
                )
                if (i % 3 == 0) {
                    playWarningSoundToPlayer(player)
                }
            } else if (worldBorderRadius - playerRadius <= worldBorderSpeed * (300 + leeway)) {
                actionBarService.addMessage(
                    player.uniqueId,
                    "wbWarning1",
                    mm("<legacy_red>The world border will pass you in less than 5 minutes!"),
                )
                actionBarService.addMessage(
                    player.uniqueId,
                    "wbWarning2",
                    mm("<legacy_dark_red>The world border will pass you in less than 5 minutes!"),
                )
                if (i % 4 == 0) {
                    playWarningSoundToPlayer(player)
                }
            } else {
                actionBarService.removeMessage(player.uniqueId, "wbWarning1")
                actionBarService.removeMessage(player.uniqueId, "wbWarning2")
            }
        }
    }

    companion object {
        private fun playWarningSoundToPlayer(player: Player) {
            player.playSound(player.location, StandardSounds.ALERT_INFO, 0.5f, 0.5f)
        }
    }
}
