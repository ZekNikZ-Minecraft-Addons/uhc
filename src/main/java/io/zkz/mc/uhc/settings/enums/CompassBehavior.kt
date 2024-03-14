package io.zkz.mc.uhc.settings.enums

import io.zkz.mc.gametools.settings.IGameSettingOption
import io.zkz.mc.gametools.util.ISB
import io.zkz.mc.gametools.util.mm
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

enum class CompassBehavior(
    override val label: Component,
    override val description: Component?,
    override val icon: ItemStack,
) : IGameSettingOption {
    NORMAL(
        mm("Default compass behavior"),
        mm("Compasses do not track players"),
        ISB.fromMaterial(Material.BARRIER),
    ),
    TRACK_ENEMIES(
        mm("Compasses track nearest enemy"),
        mm("Compasses track nearest non-teammate"),
        ISB.fromMaterial(Material.CREEPER_HEAD),
    ),
    TRACK_PLAYERS(
        mm("Compasses track nearest player"),
        mm("Compasses track nearest player"),
        ISB.fromMaterial(Material.PLAYER_HEAD),
    ),
}
