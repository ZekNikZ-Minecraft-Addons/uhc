package io.zkz.mc.uhc.settings

import io.zkz.mc.gametools.injection.InjectionComponent
import io.zkz.mc.gametools.injection.get

interface UhcSettingsMixin : InjectionComponent {
    private val settingsManager get() = get<SettingsManager>()

    val timeCycle get() = settingsManager.timeCycle.value
    val weatherCycle get() = settingsManager.weatherCycle.value
    val spawnPhantoms get() = settingsManager.spawnPhantoms.value
    val spawnHostileMobs get() = settingsManager.spawnHostileMobs.value
    val difficulty get() = settingsManager.difficulty.value

    val compassBehavior get() = settingsManager.compassBehavior.value
    val throwableFireballs get() = settingsManager.throwableFireballs.value
    val regenerationPotions get() = settingsManager.regenerationPotions.value
    val goldenHeads get() = settingsManager.goldenHeads.value

    val teamGame get() = settingsManager.teamGame.value
    val teamsSpawnTogether get() = settingsManager.teamsSpawnTogether.value
    val spectatorInventories get() = settingsManager.spectatorInventories.value

    val worldBorderDistance1 get() = settingsManager.worldBorderDistance1.value
    val worldBorderDistance2 get() = settingsManager.worldBorderDistance2.value
    val worldBorderDistance3 get() = settingsManager.worldBorderDistance3.value
    val worldBorderTime1 get() = settingsManager.worldBorderTime1.value
    val worldBorderTime2 get() = settingsManager.worldBorderTime2.value
    val suddenDeathEnabled get() = settingsManager.suddenDeathEnabled.value
    val parlayTime get() = settingsManager.parlayTime.value
    val suddenDeathTime get() = settingsManager.suddenDeathTime.value

    val permadayTime get() = settingsManager.permadayTime.value
    val peacefulTime get() = settingsManager.peacefulTime.value
    val shieldlessTime get() = settingsManager.shieldlessTime.value
}
