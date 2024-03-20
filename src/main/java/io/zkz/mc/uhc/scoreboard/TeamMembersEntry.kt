package io.zkz.mc.uhc.scoreboard

import io.zkz.mc.gametools.injection.InjectionComponent
import io.zkz.mc.gametools.injection.inject
import io.zkz.mc.gametools.scoreboard.entry.ScoreboardEntry
import io.zkz.mc.gametools.team.GameTeam
import io.zkz.mc.gametools.util.mm
import io.zkz.mc.minigamemanager.minigame.MinigameService
import net.kyori.adventure.text.Component
import kotlin.math.ceil

class TeamMembersEntry(
    private val team: GameTeam,
) : ScoreboardEntry(), InjectionComponent {
    private val minigameService by inject<MinigameService>()

    override fun render(pos: Int) {
        var i = 0
        team.onlineMembers
            .sortedBy { it.name }
            .forEach {
                if (minigameService.currentRound.isAlive(it)) {
                    scoreboard.setLine(
                        pos + i,
                        mm(
                            "<0> - <legacy_red><1> \u2764</legacy_red>",
                            it.displayName(),
                            Component.text(ceil(it.health + it.absorptionAmount) / 2.0),
                        ),
                    )
                } else {
                    scoreboard.setLine(
                        pos + i,
                        mm("<0> - <legacy_dark_red>\u2620</legacy_dark_red>", it.displayName()),
                    )
                }
                ++i
            }
    }

    override val rowCount: Int
        get() = team.members.size
}
