package io.zkz.mc.uhc.task

import io.zkz.mc.gametools.util.BukkitUtils.forEachPlayer
import io.zkz.mc.minigamemanager.task.MinigameTask
import org.bukkit.GameMode
import org.bukkit.entity.Player

class UpdateSpectatorInventoriesTask : MinigameTask(1, 1) {
    override fun run() {
        forEachPlayer {
            if (it.gameMode != GameMode.SPECTATOR || it.spectatorTarget !is Player) {
                return@forEachPlayer
            }
            it.inventory.armorContents = (it.spectatorTarget as Player).inventory.armorContents
            it.inventory.contents = (it.spectatorTarget as Player).inventory.contents
        }
    }
}
