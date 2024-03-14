package io.zkz.mc.uhc.task

import io.zkz.mc.gametools.injection.inject
import io.zkz.mc.gametools.scoreboard.GameScoreboard
import io.zkz.mc.gametools.scoreboard.ScoreboardService
import io.zkz.mc.gametools.util.WorldSyncUtils
import io.zkz.mc.minigamemanager.task.MinigameTask

class WorldborderScoreboardUpdateTask : MinigameTask(1, 1) {
    private val scoreboardService by inject<ScoreboardService>()

    private var lastWorldborder = -1

    override fun run() {
        val worldborderSize = WorldSyncUtils.worldBorderSize.toInt()
        if (lastWorldborder != worldborderSize) {
            lastWorldborder = worldborderSize
            scoreboardService.allScoreboards.forEach(GameScoreboard::redraw)
        }
    }
}
