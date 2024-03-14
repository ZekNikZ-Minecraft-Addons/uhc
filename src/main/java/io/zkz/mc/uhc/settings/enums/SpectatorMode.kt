package io.zkz.mc.uhc.settings.enums

import io.zkz.mc.gametools.settings.IGameSettingOption
import io.zkz.mc.gametools.util.ISB
import io.zkz.mc.gametools.util.mm
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

enum class SpectatorMode(
    override val label: Component,
    override val description: Component?,
    override val icon: ItemStack,
) : IGameSettingOption {
    NORMAL(
        mm("Default behavior"),
        mm("Spectator mode acts like in Vanilla"),
        ISB.fromMaterial(Material.LEATHER_HELMET),
    ),
    SPECTATORS_SEE_INVENTORIES(
        mm("Spectators can see inventories"),
        mm("Spectators can view player's inventories while spectating them"),
        ISB.fromMaterial(Material.DIAMOND_HELMET),
    ),
    TEAMS_SEE_INVENTORIES(
        mm("Team members can see inventories"),
        mm("Spectators can view alive teammates' inventories while spectating them"),
        ISB.fromMaterial(Material.GOLDEN_HELMET),
    ),
    ;
}
