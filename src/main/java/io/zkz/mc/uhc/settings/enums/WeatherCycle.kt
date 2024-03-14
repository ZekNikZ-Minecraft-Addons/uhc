package io.zkz.mc.uhc.settings.enums

import io.zkz.mc.gametools.settings.IGameSettingOption
import io.zkz.mc.gametools.util.ISB
import io.zkz.mc.gametools.util.mm
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

enum class WeatherCycle(
    override val label: Component,
    override val description: Component?,
    override val icon: ItemStack,
) : IGameSettingOption {
    CLEAR_ONLY(
        mm("No weather"),
        mm("No weather cycle."),
        ISB.fromMaterial(Material.LIGHT_GRAY_DYE),
    ),
    NORMAL(
        mm("Normal weather cycle"),
        mm("Vanilla weather cycle."),
        ISB.fromMaterial(Material.BLUE_DYE),
    ),
    RAIN_ONLY(
        mm("Always raining"),
        mm("Permanent rain."),
        ISB.fromMaterial(Material.RED_DYE),
    ),
    STORM_ONLY(
        mm("Always storming"),
        mm("Permanent lightning storm."),
        ISB.fromMaterial(Material.ORANGE_DYE),
    ),
}
