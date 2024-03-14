package io.zkz.mc.uhc.task

import io.zkz.mc.gametools.hud.ActionBarService
import io.zkz.mc.gametools.injection.inject
import io.zkz.mc.gametools.util.BukkitUtils.forEachPlayer
import io.zkz.mc.gametools.util.mm
import io.zkz.mc.minigamemanager.task.MinigameTask
import org.bukkit.entity.Player

class CoordinatesHudTask : MinigameTask(1, 1) {
    private val actionBarService by inject<ActionBarService>()

    override fun run() {
        forEachPlayer { player: Player ->
            actionBarService.addMessage(
                player.uniqueId,
                "coordshud",
                mm(
                    "<legacy_gold>XYZ:</legacy_gold> <0> <1> <2>   <legacy_gold><3> (<4>)</legacy_gold>",
                    player.location.blockX,
                    player.location.blockY,
                    player.location.blockZ,
                    getCardinalDirection(player),
                    getBlockDirection(player),
                ),
            )
        }
    }

    companion object {
        private fun getCardinalDirection(player: Player): String? {
            var rotation = (player.location.yaw - 180) % 360
            if (rotation < 0) {
                rotation += 360
            }
            return if (0 <= rotation && rotation < 45.0) {
                "N"
            } else if (45.0 <= rotation && rotation < 135.0) {
                "E"
            } else if (135.0 <= rotation && rotation < 225.0) {
                "S"
            } else if (225.0 <= rotation && rotation < 315.0) {
                "W"
            } else if (315.0 <= rotation && rotation < 360.0) {
                "N"
            } else {
                null
            }
        }

        private fun getBlockDirection(player: Player): String? {
            var rotation = (player.location.yaw - 180) % 360
            if (rotation < 0) {
                rotation += 360
            }
            return if (0 <= rotation && rotation < 45.0) {
                "-Z"
            } else if (45.0 <= rotation && rotation < 135.0) {
                "+X"
            } else if (135.0 <= rotation && rotation < 225.0) {
                "+Z"
            } else if (225.0 <= rotation && rotation < 315.0) {
                "-X"
            } else if (315.0 <= rotation && rotation < 360.0) {
                "-Z"
            } else {
                null
            }
        }
    }
}
