package io.zkz.mc.uhc.game

import io.zkz.mc.gametools.hud.ActionBarService
import io.zkz.mc.gametools.injection.Injectable
import io.zkz.mc.gametools.scoreboard.GameScoreboard
import io.zkz.mc.gametools.scoreboard.ScoreboardService
import io.zkz.mc.gametools.service.PluginService
import io.zkz.mc.gametools.team.TeamService
import io.zkz.mc.gametools.team.event.TeamChangeEvent
import io.zkz.mc.gametools.util.BukkitUtils.forEachPlayer
import io.zkz.mc.minigamemanager.event.StateChangeEvent.Pre
import io.zkz.mc.minigamemanager.minigame.MinigameConfig
import io.zkz.mc.minigamemanager.minigame.MinigameService
import io.zkz.mc.uhc.UhcPlugin
import io.zkz.mc.uhc.settings.SettingsManager
import io.zkz.mc.uhc.settings.enums.TeamStatus
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent

@Injectable
class UhcMinigame(
    plugin: UhcPlugin,
    private val minigameService: MinigameService,
    private val teamService: TeamService,
    private val scoreboardService: ScoreboardService,
    private val actionBarService: ActionBarService,
    private val settingsManager: SettingsManager,
) : PluginService<UhcPlugin>(plugin) {
    override fun onEnable() {
        buildConfig()
        settingsManager.teamGame.addListener { buildConfig() }
        teamService.setupDefaultTeams()
    }

    private fun buildConfig() {
        minigameService.config = MinigameConfig(
            name = "UHC",
            firstGameSpecificState = { UhcGame },
            shouldAutomaticallyShowRules = false,
            shouldShowScoreSummary = false,
            isTeamGame = settingsManager.teamGame.value == TeamStatus.TEAM_GAME,
        )
    }

    @EventHandler
    private fun onPlayerJoin(event: PlayerJoinEvent) {
        // TODO: player setup
    }

    @EventHandler
    private fun onStateChange(event: Pre) {
        forEachPlayer {
            actionBarService.removeMessage(it.uniqueId, "wbWarning1")
            actionBarService.removeMessage(it.uniqueId, "wbWarning2")
        }
    }

    @EventHandler
    private fun onTeamChange(event: TeamChangeEvent) {
        scoreboardService.allScoreboards.forEach(GameScoreboard::redraw)
    }
}
