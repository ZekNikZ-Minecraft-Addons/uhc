package io.zkz.mc.uhc.game

import io.zkz.mc.gametools.injection.Injectable
import io.zkz.mc.minigamemanager.state.StateRegistry
import io.zkz.mc.minigamemanager.state.impl.DelegatedMinigameState

@Injectable
object UhcStates : StateRegistry() {
    val WB_CLOSING_1 = register(DelegatedMinigameState("uhc:wb_closing_1"))
    val WB_CLOSING_2 = register(DelegatedMinigameState("uhc:wb_closing_2"))
    val PARLAY = register(DelegatedMinigameState("uhc:parlay"))
    val SUDDEN_DEATH = register(DelegatedMinigameState("uhc:sudden_death"))
}
