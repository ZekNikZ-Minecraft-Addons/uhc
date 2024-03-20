package io.zkz.mc.uhc.task

import io.zkz.mc.gametools.injection.inject
import io.zkz.mc.gametools.sound.StandardSounds
import io.zkz.mc.gametools.sound.playSound
import io.zkz.mc.gametools.util.Chat
import io.zkz.mc.gametools.util.ChatType
import io.zkz.mc.gametools.util.mm
import io.zkz.mc.minigamemanager.task.MinigameTask
import io.zkz.mc.uhc.game.UhcService
import io.zkz.mc.uhc.settings.SettingsManager
import io.zkz.mc.uhc.settings.enums.TeamStatus
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.entity.EntityType
import org.bukkit.entity.Firework
import kotlin.random.Random
import kotlin.random.nextInt

class GameOverEffectsTask : MinigameTask(20, 20) {
    private val settingsManager by inject<SettingsManager>()

    private var fireworks = 5
    private val title: Component = mm("<legacy_gold><bold>GAME OVER")
    private val subtitle: Component = if (settingsManager.teamGame.value == TeamStatus.TEAM_GAME) {
        mm("<0> <legacy_aqua>wins!", UhcService.aliveTeams.first().displayName)
    } else {
        mm("<legacy_aqua><0> wins!", UhcService.onlineAlivePlayers.first())
    }

    init {
        Bukkit.getServer().showTitle(Title.title(title, subtitle))
        Chat.sendMessage(
            Bukkit.getServer(),
            ChatType.GAME_INFO,
            mm("<legacy_gold>Game over!</legacy_gold> <0>", subtitle),
        )
        playSound(Bukkit.getOnlinePlayers(), StandardSounds.GAME_OVER, 1f, 1f)
    }

    override fun run() {
        if (fireworks == 0) {
            this.cancel()
            return
        }
        UhcService.onlineAlivePlayers.forEach { p ->
            // Spawn the Firework, get the FireworkMeta.
            val entity = p.world.spawnEntity(p.location, EntityType.FIREWORK) as Firework
            val fireworkMeta = entity.fireworkMeta

            // Get the type
            val type = FireworkEffect.Type.entries.toTypedArray()[Random.nextInt(FireworkEffect.Type.entries.size)]

            // Get our random colours
            val baseColor = getColor(Random.nextInt(1..17))
            val fadeColor = getColor(Random.nextInt(1..17))

            // Create our effect with this
            val effect = FireworkEffect.builder()
                .flicker(Random.nextBoolean())
                .withColor(baseColor)
                .withFade(fadeColor)
                .with(type)
                .trail(Random.nextBoolean())
                .build()

            // Then apply the effect to the meta
            fireworkMeta.addEffect(effect)

            // Generate some random power and set it
            fireworkMeta.power = Random.nextInt(1..2)

            // Then apply this to our rocket
            entity.fireworkMeta = fireworkMeta
        }

        --fireworks
    }

    private fun getColor(i: Int): Color {
        return when (i) {
            1 -> Color.AQUA
            2 -> Color.BLACK
            3 -> Color.BLUE
            4 -> Color.FUCHSIA
            5 -> Color.GRAY
            6 -> Color.GREEN
            7 -> Color.LIME
            8 -> Color.MAROON
            9 -> Color.NAVY
            10 -> Color.OLIVE
            11 -> Color.ORANGE
            12 -> Color.PURPLE
            13 -> Color.RED
            14 -> Color.SILVER
            15 -> Color.TEAL
            16 -> Color.WHITE
            else -> Color.YELLOW
        }
    }
}
