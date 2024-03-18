package io.zkz.mc.uhc.scoreboard

import io.zkz.mc.gametools.injection.InjectionComponent
import io.zkz.mc.gametools.injection.inject
import io.zkz.mc.gametools.scoreboard.GameScoreboard
import io.zkz.mc.gametools.scoreboard.ScoreboardService
import io.zkz.mc.gametools.scoreboard.entry.ComputableValueEntry
import io.zkz.mc.gametools.team.GameTeam
import io.zkz.mc.gametools.util.WorldSyncUtils
import io.zkz.mc.gametools.util.mm
import io.zkz.mc.minigamemanager.minigame.MinigameService
import io.zkz.mc.minigamemanager.scoreboard.MinigameScoreboard
import io.zkz.mc.minigamemanager.scoreboard.impl.StandardMinigameScoreboard
import io.zkz.mc.uhc.game.UhcGame
import io.zkz.mc.uhc.settings.SettingsManager
import io.zkz.mc.uhc.settings.enums.TeamStatus
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.RenderType

object UhcMinigameScoreboard : MinigameScoreboard, InjectionComponent {
    private val scoreboardService by inject<ScoreboardService>()
    private val minigameService by inject<MinigameService>()
    private val settingsManager by inject<SettingsManager>()

    override fun apply(player: Player, teamOfPlayer: GameTeam?) {
        val scoreboard: GameScoreboard = scoreboardService.createNewScoreboard(mm("<legacy_gold><bold>UHC"))

        // Timer
        StandardMinigameScoreboard.addGlobalMinigameTimer(scoreboard)
        scoreboard.addEntry(
            ComputableValueEntry("<legacy_red><bold>In-game time:</bold></legacy_red> <value>") {
                val time = WorldSyncUtils.time
                val adjustedTime = (time + 6000) % 24000
                val hours = adjustedTime / 1000
                val minutes = adjustedTime % 1000 * 60 / 1000
                String.format("%02d:%02d", hours, minutes)
            },
        )

        // World border
        scoreboard.addEntry(
            "worldborder",
            ComputableValueEntry("<legacy_aqua><bold>Worldborder:</bold></legacy_aqua> \u00B1<value>") {
                WorldSyncUtils.worldBorderSize.toInt() / 2
            },
        )

        // Alive people
        scoreboard.addSpace()
        if (settingsManager.teamGame.value == TeamStatus.TEAM_GAME) {
            scoreboard.addEntry(
                ComputableValueEntry("<legacy_green><bold>Alive teams:</bold></legacy_green> <value>") {
                    UhcGame.aliveTeams.size
                },
            )
        }
        scoreboard.addEntry(
            ComputableValueEntry("<legacy_green><bold>Alive players:</bold></legacy_green> <value>") {
                UhcGame.alivePlayers.size
            },
        )

        // Team members
        if (teamOfPlayer != null && settingsManager.teamGame.value == TeamStatus.TEAM_GAME) {
            scoreboard.addSpace()
            scoreboard.addEntry(mm("<legacy_aqua><bold>Team members:"))
            scoreboard.addEntry(TeamMembersEntry(teamOfPlayer))
        }

        // Tab list
        scoreboard.setTabListObjective("hp", Criteria.HEALTH, mm("HP"), RenderType.HEARTS)

        if (teamOfPlayer != null) {
            scoreboardService.setTeamScoreboard(teamOfPlayer.id, scoreboard)
        } else {
            scoreboardService.setGlobalScoreboard(scoreboard)
        }
    }
}
