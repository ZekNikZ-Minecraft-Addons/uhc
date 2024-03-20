package io.zkz.mc.uhc.game

import io.zkz.mc.gametools.injection.Injectable
import io.zkz.mc.gametools.scoreboard.ScoreboardService
import io.zkz.mc.gametools.service.PluginService
import io.zkz.mc.minigamemanager.minigame.MinigameService
import io.zkz.mc.minigamemanager.scoreboard.MinigameScoreboard
import io.zkz.mc.uhc.UhcPlugin
import io.zkz.mc.uhc.scoreboard.UhcMinigameScoreboard
import org.bukkit.entity.Player

@Injectable
class UhcService(
    plugin: UhcPlugin,
    private val scoreboardService: ScoreboardService,
    private val minigameService: MinigameService,
) : PluginService<UhcPlugin>(plugin) {
    val currentWorldBorderSpeed: Double = TODO()
    val currentWorldBorderTarget: Double = TODO()
}
