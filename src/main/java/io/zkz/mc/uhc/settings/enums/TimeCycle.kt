package io.zkz.mc.uhc.settings.enums

import io.zkz.mc.gametools.settings.IGameSettingOption
import io.zkz.mc.gametools.util.ISB
import io.zkz.mc.gametools.util.mm
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

enum class TimeCycle(
    override val label: Component,
    override val description: Component?,
    override val icon: ItemStack,
) : IGameSettingOption {
    NORMAL(
        mm("Normal day/night cycle"),
        mm("Vanilla day/night cycle."),
        ISB.fromMaterial(Material.CLOCK),
    ),
    DAY_ONLY(
        mm("Day only"),
        mm("Permanent daytime."),
        ISB.fromMaterial(Material.SUNFLOWER),
    ),
    NIGHT_ONLY(
        mm("Night only"),
        mm("Permanent nighttime."),
        ISB.fromMaterial(Material.COBWEB),
    ),
}
