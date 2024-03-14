package io.zkz.mc.uhc

import io.zkz.mc.gametools.GTPlugin
import io.zkz.mc.gametools.resourcepack.IProvidesResourcePackParts
import io.zkz.mc.gametools.resourcepack.ResourcePackBuilder
import io.zkz.mc.uhc.overrides.RecipeOverrides

class UhcPlugin : GTPlugin<UhcPlugin>(), IProvidesResourcePackParts {
    override fun buildResourcePack(builder: ResourcePackBuilder) {
        builder.apply {
            withNegativeSpaceCharacters()
        }
    }

    override fun onEnable() {
        super.onEnable()

        RecipeOverrides.override(this)
    }
}
