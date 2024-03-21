package io.zkz.mc.uhc.game

import io.zkz.mc.gametools.injection.Injectable
import io.zkz.mc.gametools.scoreboard.ScoreboardService
import io.zkz.mc.gametools.service.PluginService
import io.zkz.mc.minigamemanager.minigame.MinigameService
import io.zkz.mc.minigamemanager.state.DefaultStates
import io.zkz.mc.uhc.UhcPlugin

@Injectable
class UhcService(
    plugin: UhcPlugin,
    private val scoreboardService: ScoreboardService,
    private val minigameService: MinigameService,
) : PluginService<UhcPlugin>(plugin) {
    override fun onEnable() {
        listOf(
            DefaultStates.WAITING_FOR_PLAYERS,
            DefaultStates.RULES,
            DefaultStates.WAITING_TO_BEGIN,
        ).forEach { state ->
            state.handlePlayerJoin {
                minigameService.uhcRound.setupLobbyPlayer(it)
            }
        }

        DefaultStates.PRE_ROUND.handlePlayerJoin {
            minigameService.uhcRound.setupPreRoundPlayer(it)
        }

        listOf(
            UhcStates.WB_CLOSING_1,
            UhcStates.WB_CLOSING_2,
            UhcStates.PARLAY,
            UhcStates.SUDDEN_DEATH,
        ).forEach { state ->
            state.handlePlayerJoin {
                minigameService.uhcRound.setupInGamePlayer(it)
            }
        }

        listOf(
            DefaultStates.POST_ROUND,
            DefaultStates.POST_GAME,
        ).forEach { state ->
            state.handlePlayerJoin {
                minigameService.uhcRound.setupPostGamePlayer(it)
            }
        }
    }
}
