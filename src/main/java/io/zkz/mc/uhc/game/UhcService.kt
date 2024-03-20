package io.zkz.mc.uhc.game

import io.zkz.mc.gametools.injection.Injectable
import io.zkz.mc.gametools.scoreboard.ScoreboardService
import io.zkz.mc.gametools.service.PluginService
import io.zkz.mc.minigamemanager.minigame.MinigameService
import io.zkz.mc.uhc.UhcPlugin

@Injectable
class UhcService(
    plugin: UhcPlugin,
    private val scoreboardService: ScoreboardService,
    private val minigameService: MinigameService,
) : PluginService<UhcPlugin>(plugin)
