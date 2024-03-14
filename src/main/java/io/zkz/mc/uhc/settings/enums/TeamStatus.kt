package io.zkz.mc.uhc.settings.enums

import io.zkz.mc.gametools.settings.IGameSettingOption
import io.zkz.mc.gametools.util.ISB
import io.zkz.mc.gametools.util.mm
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

enum class TeamStatus(
    override val label: Component,
    override val description: Component?,
    override val icon: ItemStack,
) : IGameSettingOption {
    TEAM_GAME(
        mm("Team mode"),
        mm("Players will work together as teams to win."),
        ISB.fromMaterial(Material.IRON_CHESTPLATE),
    ),
    INDIVIDUAL_GAME(
        mm("Individual mode"),
        mm("Players will each be competing for themselves."),
        ISB.fromMaterial(Material.IRON_AXE),
    ),
}
