package io.zkz.mc.uhc.settings.enums

import io.zkz.mc.gametools.settings.IGameSettingOption
import io.zkz.mc.gametools.util.ISB
import io.zkz.mc.gametools.util.mm
import net.kyori.adventure.text.Component
import org.bukkit.Difficulty
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

enum class GameDifficulty(
    val difficulty: Difficulty,
    override val label: Component,
    override val icon: ItemStack,
) : IGameSettingOption {
    PEACEFUL(
        Difficulty.PEACEFUL,
        mm("Peaceful"),
        ISB.fromMaterial(Material.WOODEN_SWORD),
    ),
    EASY(
        Difficulty.EASY,
        mm("Easy"),
        ISB.fromMaterial(Material.STONE_SWORD),
    ),
    NORMAL(
        Difficulty.NORMAL,
        mm("Normal"),
        ISB.fromMaterial(Material.IRON_SWORD),
    ),
    HARD(
        Difficulty.HARD,
        mm("Hard"),
        ISB.fromMaterial(Material.DIAMOND_SWORD),
    ),
    ;

    override val description = null
}
