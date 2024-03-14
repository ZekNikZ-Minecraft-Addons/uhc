package io.zkz.mc.uhc.overrides

import io.zkz.mc.gametools.util.ISB
import io.zkz.mc.gametools.util.mm
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.plugin.java.JavaPlugin

object RecipeOverrides {
    fun override(plugin: JavaPlugin) {
        // Remove enchanted golden apple recipe
        var key = removeRecipe(plugin, ISB.fromMaterial(Material.ENCHANTED_GOLDEN_APPLE))
        if (key == null) {
            key = Material.ENCHANTED_GOLDEN_APPLE.key
        }

        // Enchanted head recipe
        plugin.server.addRecipe(
            ShapedRecipe(
                key,
                ISB.fromMaterial(Material.ENCHANTED_GOLDEN_APPLE) {
                    name(mm("<light_purple>Golden Head"))
                    lore(mm("Baked in the blood of your foes"))
                },
            ).shape("###", "#*#", "###")
                .setIngredient('#', Material.GOLD_INGOT)
                .setIngredient('*', Material.PLAYER_HEAD),
        )
    }

    private fun removeRecipe(plugin: JavaPlugin, result: ItemStack): NamespacedKey? {
        val it: MutableIterator<Recipe> = plugin.server.recipeIterator()
        var recipe: Recipe
        while (it.hasNext()) {
            recipe = it.next()
            if (recipe.result.type === result.type) {
                it.remove()
                val res = recipe.result
                if (res is Keyed) {
                    return res.key
                }
                return recipe.result.type.key
            }
        }
        return null
    }

    fun removeShieldRecipe(plugin: JavaPlugin) {
        removeRecipe(plugin, ISB.fromMaterial(Material.SHIELD))
    }
}
