package io.zkz.mc.uhc.game

import io.zkz.mc.gametools.injection.inject
import io.zkz.mc.gametools.scoreboard.ScoreboardService
import io.zkz.mc.minigamemanager.minigame.MinigameService
import io.zkz.mc.minigamemanager.scoreboard.MinigameScoreboard
import io.zkz.mc.minigamemanager.minigame.AliveDeadTrackerMixin
import io.zkz.mc.minigamemanager.minigame.ITracksAliveDead
import io.zkz.mc.uhc.scoreboard.UhcMinigameScoreboard
import org.bukkit.entity.Player

object UhcGame : ParentMinigameState("uhc"), ITracksAliveDead by AliveDeadTrackerMixin() {
    private val scoreboardService by inject<ScoreboardService>()
    private val minigameService by inject<MinigameService>()

    val currentWorldBorderSpeed: Double = TODO()
    val currentWorldBorderTarget: Double = TODO()

    override fun onEnter() {
        // TODO: add tasks
    }

    override fun buildScoreboard(): MinigameScoreboard {
        return UhcMinigameScoreboard
    }

    override fun onPlayerDeath(player: Player) {
        setDead(player)
    }
}
